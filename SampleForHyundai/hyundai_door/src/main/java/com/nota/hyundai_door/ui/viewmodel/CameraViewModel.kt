package com.nota.hyundai_door.ui.viewmodel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nota.nota_sdk.task.vision.FacialProcess
import com.nota.hyundai_door.SingleLiveEvent
import com.nota.hyundai_door.data.RegistrationRepository
import com.nota.hyundai_door.data.User
import com.nota.hyundai_door.ui.adapter.DebugImageAdapter
import com.nota.hyundai_door.ui.adapter.DebugImageAdapter.Companion.toItemList
import com.nota.nota_sdk.task.vision.FacialProcess.sortByGoodFacialQuality
import com.nota.nota_sdk.task.vision.face.FacialFeature
import java.util.*
import kotlin.collections.ArrayList

class CameraViewModel: ViewModel() {

    companion object {
        const val TAG = "CameraViewModel"
        const val RECOGNITION_CANDIDATE_COUNT = 5
        const val REGISTRATION_CANDIDATE_COUNT = 10
        const val ENTER_DISPLAY_DELAY = 2000L
        const val FACE_QUALITY_THRESHOLD = 50
    }

    enum class State {
        IDLE, REGISTRATION, RECOGNITION, ENTER
    }

    val currentState = MutableLiveData(State.IDLE)
    val cameraSelector = MutableLiveData(Camera.CameraInfo.CAMERA_FACING_BACK)
    val toastMessage = SingleLiveEvent<String>()
    val showConfirmDialogEvent = SingleLiveEvent<FacialProcess.FaceDetectResult>()
    val isDetectMask = MutableLiveData(false)
    val isDetectSpoof = MutableLiveData(false)
    val isCheckBlurScore = MutableLiveData(false)
    val txtLog = MutableLiveData("")
    val imgLog = MutableLiveData<Bitmap>()

    // 시각적 디버깅을 위한 RecyclerAdapter
    val debugImageAdapter = DebugImageAdapter()

    // 등록 시 체크할 얼굴 리스트
    private val candidateFacialDataList = ArrayList<FacialProcess.FaceDetectResult>()
    // 입장 시 비교할 유저 리스트 (DB 에서 업데이트)
    private var registrationUserList : ArrayList<User>? = null

    private var bestRegistrationFeature : FacialFeature? = null

    private val enterShowingDelayHandler =
        @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                // Change to IDLE state
                currentState.value = State.IDLE
            }
        }

    // 카메라 프레임이 들어올 때 마다 실행
    fun getCameraFrame(bitmap: Bitmap) {

        synchronized(this) {

            val inferenceOption =
                FacialProcess.Option(
                    isCheckBlurScore = isCheckBlurScore.value!!,
                    isDetectMask = isDetectMask.value!!,
                    isDetectSpoof = isDetectSpoof.value!!
                )

            FacialProcess.detectFace(bitmap, inferenceOption) { result ->

                result?.run {

                    when (currentState.value) {

                        /** 등록 State 진행 로직
                        * @see registrationProcess()
                        * 1. 등록 버튼 누를 시 REGISTRATION 상태로 변경
                        * 2. 등록 상태 일 시 매 프레임 마다 등록 후보 Facial Data 수집
                        * 3. REGISTRATION_CANDIDATE_COUNT 만큼 데이터 수집
                        * 4. 수집된 후보군 Facial Data 중 가장 나은 Facial Data 를 등록 이미지로 선택
                        *  */
                        State.REGISTRATION -> {

                            this.isMaskDetected?.let {
                                if (it) {
                                    return@run
                                }
                            }

                            this.isSpoof?.let {
                                if (it) {
                                    return@run
                                }
                            }

                            this.blurScore?.let {
                                if (it < FACE_QUALITY_THRESHOLD) {
                                    return@run
                                }
                            }

                            if(candidateFacialDataList.size >= REGISTRATION_CANDIDATE_COUNT){
                                registrationProcess()
                            } else {
                                candidateFacialDataList.add(this)
                                val sorted = candidateFacialDataList.sortByGoodFacialQuality()
                                val itemList = sorted.toItemList()
                                debugImageAdapter.replace(itemList)
                            }
                        }

                        /** 입장 State 진행 로직
                        * @see recognitionProcess()
                        * 1. 입장 버튼 누를 시 RECOGNITION 상태로 변경
                        * 2. RECOGNITION 상태일 때, 매 프레임마다 후보군 Facial Data 수집
                        * 3. 수집된 후보군 중 가장 나은 Facial Data 를 인식할 이미지로 선정
                        * 4. 선택된 Faical Data 를 DB에 등록된 데이터들과 비교
                        * 5. 유사도가 THRESHOLD 값 이상일 때, 같은 사람으로 인식하여 입장 처리
                        *  */
                        State.RECOGNITION -> {

                            if(registrationUserList == null){
                                registrationUserList = RegistrationRepository.getInstance().getUserList()
                            }

                            imgLog.value = detectedFaceBitmap

                            if (registrationUserList?.size == 0) {
                                toastMessage.value = "There are no registered faces."
                                currentState.value = State.IDLE
                                return@run
                            }

                            this.isMaskDetected?.let {
                                if (it) {
                                    toastMessage.value = "A mask has been detected."
                                    return@run
                                }
                            }

                            this.isSpoof?.let {
                                if (it) {
                                    toastMessage.value = "Spoofing has been detected."
                                    return@run
                                }
                            }

                            if(candidateFacialDataList.size >= RECOGNITION_CANDIDATE_COUNT){
                                recognitionProcess()
                            }else {
                                candidateFacialDataList.add(this)
                                val sorted = candidateFacialDataList.sortByGoodFacialQuality()
                                val itemList = sorted.toItemList()
                                debugImageAdapter.replace(itemList)
                            }

                        }

                    }

                    // Inference log 출력
                    Log.d(TAG, "inferenceTime : $inferenceTimeLog")
                }

                bitmap.recycle()
            }

        }

    }

    fun confirmImage(userID: String, userName: String){

        // 얼굴 데이터 저장
        bestRegistrationFeature?.let {
            RegistrationRepository.getInstance()
                .addUser(User(id = userID, name = userName, it))
            toastMessage.value = "Registration Success."
        } ?: run {
            toastMessage.value = "Registration Failed : Something is wrong."
        }

        bestRegistrationFeature = null
        registrationUserList = RegistrationRepository.getInstance().getUserList()

    }

    fun onClickSwitchCamera() : View.OnClickListener {
        return View.OnClickListener {
            if(cameraSelector.value == Camera.CameraInfo.CAMERA_FACING_FRONT){
                cameraSelector.value = Camera.CameraInfo.CAMERA_FACING_BACK
            }else{
                cameraSelector.value = Camera.CameraInfo.CAMERA_FACING_FRONT
            }
        }
    }

    fun startRegisterUser() : View.OnClickListener {
        return View.OnClickListener {
            candidateFacialDataList.clear()
            debugImageAdapter.clear()
            currentState.value = State.REGISTRATION
        }
    }

    fun registrationProcess(){
        currentState.value = State.IDLE

        // 얼굴 크기 큰 순서대로 정렬
        val sortResult = candidateFacialDataList.sortByGoodFacialQuality()

        if(sortResult.isNotEmpty()){
            // 제일 좋은 품질의 데이터로 등록 진행

            FacialProcess.featureExtract(sortResult[0].detectedFaceBitmap){
                bestRegistrationFeature = it.facialFeature
                showConfirmDialogEvent.value = sortResult[0]
            }

        } else {
            toastMessage.value = "Face not found."
        }

    }


    fun recognitionProcess() {
        // Change to PROCESSING state
        currentState.value = State.IDLE

        // 얼굴 데이터 후보군에서 품질이 제일 좋은 순서대로 정렬
        val sortResult = candidateFacialDataList.sortByGoodFacialQuality()

        if (sortResult.isEmpty()) {
            toastMessage.value = "Authentication Fail : No saved Facial Data"
            return
        }

        // 제일 품질이 좋은 얼굴 데이터에서 얼굴 특징 추출
        FacialProcess.featureExtract(sortResult[0].detectedFaceBitmap) { result ->
            txtLog.value = ""

            registrationUserList?.forEachIndexed { index, user ->

                val similarity =
                    user.facialData.getSimilarity(result.facialFeature)

                txtLog.value += "similarity with ${user.name} : $similarity\n"

                if (user.facialData.isIdentical(result.facialFeature)) {
                    // 입장 처리
                    toastMessage.value = "Authentication Success \nid : $index / similarity : $similarity"
                    currentState.value = State.ENTER
                    enterShowingDelayHandler.sendEmptyMessageDelayed(0, ENTER_DISPLAY_DELAY)
                    return@featureExtract
                }
            }

            toastMessage.value = "Authentication Fail : No matching Facial Data"
        }
    }


    fun startUserVerification() : View.OnClickListener {
        return View.OnClickListener {
            candidateFacialDataList.clear()
            debugImageAdapter.clear()
            currentState.value = State.RECOGNITION
        }
    }

}