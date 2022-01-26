package com.nota.hyundai_lobby.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.graphics.times
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nota.hyundai_lobby.SingleLiveEvent
import com.nota.hyundai_lobby.data.User
import com.nota.hyundai_lobby.http.HttpRepository
import com.nota.hyundai_lobby.ui.adapter.DebugImageAdapter
import com.nota.hyundai_lobby.ui.adapter.DebugImageAdapter.Companion.toItemList
import com.nota.nota_sdk.task.vision.FacialProcess
import com.nota.nota_sdk.task.vision.FacialProcess.sortByBlurity
import com.nota.nota_sdk.task.vision.FacialProcess.sortByGoodFacialQuality
import java.util.*
import kotlin.collections.ArrayList

class CameraViewModel(application: Application): AndroidViewModel(application) {

    companion object {
        const val TAG = "CameraViewModel"
        const val RECOGNITION_CANDIDATE_COUNT = 5
        const val REGISTRATION_CANDIDATE_COUNT = 10
        const val ENTER_DISPLAY_DELAY = 2000L
        const val BLUR_SCORE_THRESHOLD = 300
    }

    enum class State {
        IDLE, REGISTRATION, RECOGNITION, ENTER
    }

    val currentState = MutableLiveData(State.IDLE)
    val cameraSelector = MutableLiveData(Camera.CameraInfo.CAMERA_FACING_BACK)
    val toastMessage = SingleLiveEvent<String>()
    val showConfirmDialogEvent = SingleLiveEvent<FacialProcess.FaceDetectResult>()
    val showUserManagementDialogEvent = SingleLiveEvent<Unit>()
    val showEnterLogManagementDialogEvent = SingleLiveEvent<Unit>()
    val showIntruderLogManagementDialogEvent = SingleLiveEvent<Unit>()
    val showDatePickerDialogEvent = SingleLiveEvent<Unit>()
    val showGuideDialogEvent = SingleLiveEvent<Unit>()
    val isDetectMask = MutableLiveData(false)
    val isDetectSpoof = MutableLiveData(false)
    val isCheckBlurScore = MutableLiveData(false)
    val imgLog = MutableLiveData<Bitmap>()
    val startDate = MutableLiveData<String>()
    val endDate = MutableLiveData<String>()

    // 시각적 디버깅을 위한 RecyclerAdapter
    val debugImageAdapter = DebugImageAdapter()

    // 등록 시 체크할 얼굴 리스트
    private val candidateFacialDataList = ArrayList<FacialProcess.FaceDetectResult>()
    // 입장 시 비교할 유저 리스트 (DB 에서 업데이트)
    private var registrationUserList : ArrayList<User>? = null

    private var bestRegistrationBitmap : Bitmap? = null

    @SuppressLint("HardwareIds")
    private val gateId = {
        Settings.Secure.getString(
            getApplication<Application>().contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    // 등록, 입장 시 시간초과 타이머 체크를 위한 핸들러
    private val timerHandler = Handler {
        currentState.value = State.IDLE
        toastMessage.value = "시간(5초)이 초과하였습니다."
        return@Handler true
    }

    // 입장 시 Welcome 메세지 표시를 위한 핸들러
    private val enterShowingDelayHandler =
        @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                // Change to IDLE state
                currentState.value = State.IDLE
            }
        }

    fun updateUserList(){
        HttpRepository.getInstance().getUsers { userList ->
            registrationUserList = userList
        }
    }

    // 카메라 프레임이 들어올 때 마다 실행
    fun getCameraFrame(bitmap: Bitmap) {

        synchronized(this) {

            val inferenceOption =
                FacialProcess.Option(
                    isCheckBlurScore = isCheckBlurScore.value!!,
                    isDetectMask = isDetectMask.value!!,
                    isDetectSpoof = isDetectSpoof.value!!,
                    isCropOnly = true
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

                            timerHandler.removeCallbacksAndMessages(null)
                            timerHandler.sendEmptyMessageDelayed(0, 5000)

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
                                if (it < BLUR_SCORE_THRESHOLD) {
                                    return@run
                                }
                            }

                            if(candidateFacialDataList.size >= REGISTRATION_CANDIDATE_COUNT){
                                registrationProcess()
                            } else {
                                // 얼굴 가로 사이즈가 30% 이상일 시에만 리스트에 추가
                                if(this.face!!.rectf.width() > 0.3) {
                                    candidateFacialDataList.add(this)
                                    val sorted = candidateFacialDataList.sortByGoodFacialQuality()
                                    val itemList = sorted.toItemList()
                                    debugImageAdapter.replace(itemList)
                                }
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

                            timerHandler.removeCallbacksAndMessages(null)
                            timerHandler.sendEmptyMessageDelayed(0, 5000)

                            imgLog.value = detectedFaceBitmap

                            this.isMaskDetected?.let {
                                if (it) {
//                                    toastMessage.value = "마스크가 감지되었습니다."
                                    return@run
                                }
                            }

                            this.isSpoof?.let {
                                if (it) {
//                                    toastMessage.value = "침입 시도(스푸핑)가 감지되었습니다."
                                    return@run
                                }
                            }

                            this.blurScore?.let {
                                if (it < BLUR_SCORE_THRESHOLD) {
                                    return@run
                                }
                            }

                            if(candidateFacialDataList.size >= RECOGNITION_CANDIDATE_COUNT){
                                recognitionProcess()
                            }else {
                                // 얼굴 가로 사이즈가 30% 이상일 시에만 리스트에 추가
                                if(this.face!!.rectf.width() > 0.3) {
                                    candidateFacialDataList.add(this)
                                    val sorted = candidateFacialDataList.sortByGoodFacialQuality()
                                    val itemList = sorted.toItemList()
                                    debugImageAdapter.replace(itemList)
                                }
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

    fun confirmMemberRegiImage(userName: String, dong: String, ho: String){

        // 얼굴 데이터 저장
        bestRegistrationBitmap?.let {

            HttpRepository.getInstance()
                .regUser(User(name = userName, dong = dong, ho = ho), face = it, onSuccess = {
                    toastMessage.value = "관리자 승인 시 등록됩니다."
                }, onFailure = {
                    toastMessage.value = "네트워크 연결에 실패하였습니다."
                })

        } ?: run {
            toastMessage.value = "등록실패: 알수 없는 에러가 발생하였습니다."
        }

        bestRegistrationBitmap = null
        HttpRepository.getInstance().getUsers {
            registrationUserList = it
        }

    }

    fun confirmVisitorRegiImage(name: String, dong: String, ho: String, info: String){

        // 얼굴 데이터 저장
        bestRegistrationBitmap?.let {

            HttpRepository.getInstance()
                .regVisitor(User(name = name, dong = dong, ho = ho),
                    startDate = startDate.value!!, endDate = endDate.value!!, info = info , face = it, onSuccess = {
                    toastMessage.value = "관리자 승인 시 등록됩니다."
                }, onFailure = {
                    toastMessage.value = "네트워크 연결에 실패하였습니다."
                })

        } ?: run {
            toastMessage.value = "등록실패: 알수 없는 에러가 발생하였습니다."
        }

        bestRegistrationBitmap = null
        HttpRepository.getInstance().getUsers {
            registrationUserList = it
        }

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

    fun startRegisterUser() {
        candidateFacialDataList.clear()
        debugImageAdapter.clear()
        currentState.value = State.REGISTRATION

        // 5초간 등록 안될 시 대기상태
        timerHandler.removeCallbacksAndMessages(null)
        timerHandler.sendEmptyMessageDelayed(0, 5000)
    }

    fun clickRegisterUser() : View.OnClickListener {
        return View.OnClickListener {
            startDate.value = null
            endDate.value = null

            if(currentState.value!! == State.IDLE){
                showGuideDialogEvent.call()
            }else if(currentState.value!! == State.REGISTRATION){
                candidateFacialDataList.clear()
                debugImageAdapter.clear()
                currentState.value = State.IDLE
                timerHandler.removeCallbacksAndMessages(null)
            }
        }
    }

    fun clickVisitorRegisterUser() : View.OnClickListener {
        return View.OnClickListener {
            if(currentState.value!! == State.IDLE){
                showDatePickerDialogEvent.call()
            }else if(currentState.value!! == State.REGISTRATION){
                candidateFacialDataList.clear()
                debugImageAdapter.clear()
                currentState.value = State.IDLE
                timerHandler.removeCallbacksAndMessages(null)
            }
        }
    }

    fun registrationProcess(){

        timerHandler.removeCallbacksAndMessages(null)

        currentState.value = State.IDLE

        // 좋은 퀄리티의 얼굴 TOP 3 중 Blur 점수가 가장 낮은 것으로 등록 진행
        val sortResult = candidateFacialDataList.subList(0, 3).sortByBlurity()
        debugImageAdapter.replace(sortResult.toItemList())

        if(sortResult.isNotEmpty()){
            // 제일 좋은 품질의 데이터로 등록 진행
            bestRegistrationBitmap = sortResult[0].detectedFaceBitmap
            showConfirmDialogEvent.value = sortResult[0]
        } else {
            toastMessage.value = "얼굴을 찾을 수 없습니다."
        }

    }


    fun recognitionProcess() {

        // 유저 리스트 업데이트
        updateUserList()

        // 대기 상태로 변경
        currentState.value = State.IDLE

        // 좋은 퀄리티의 얼굴 TOP 3 중 Blur 점수가 가장 낮은 것으로 등록 진행
        val sortResult = candidateFacialDataList.subList(0, 3).sortByBlurity()
        debugImageAdapter.replace(sortResult.toItemList())

        if (sortResult.isEmpty()) {
            toastMessage.value = "인증실패: 등록된 사용자가 없습니다."
            timerHandler.removeCallbacksAndMessages(null)
            return
        }

        HttpRepository.getInstance().auth(gateId = gateId(), sortResult[0]){ isSuccess ->
            if(isSuccess){
                toastMessage.value = "인증 성공"
                currentState.value = State.ENTER
                enterShowingDelayHandler.sendEmptyMessageDelayed(0, ENTER_DISPLAY_DELAY)
                timerHandler.removeCallbacksAndMessages(null)
            } else {
                toastMessage.value = "인증 실패: 사용자가 아닙니다."
                timerHandler.removeCallbacksAndMessages(null)
            }
        }

    }


    fun clickEnterButton() : View.OnClickListener {
        return View.OnClickListener {
            candidateFacialDataList.clear()
            debugImageAdapter.clear()

            if(currentState.value == State.IDLE) {
                currentState.value = State.RECOGNITION

                // 5초간 인증 없을 시 대기상태
                timerHandler.removeCallbacksAndMessages(null)
                timerHandler.sendEmptyMessageDelayed(0, 5000)

            }else if(currentState.value == State.RECOGNITION){
                currentState.value = State.IDLE
                timerHandler.removeCallbacksAndMessages(null)
            }
        }
    }


    fun showUserManagementDialog(): View.OnClickListener {
        return View.OnClickListener {
            showUserManagementDialogEvent.value = Unit
        }
    }

    fun showEnterManagementLogDialog(): View.OnClickListener {
        return View.OnClickListener {
            showEnterLogManagementDialogEvent.value = Unit
        }
    }

    fun showIntruderManagementLogDialog(): View.OnClickListener {
        return View.OnClickListener {
            showIntruderLogManagementDialogEvent.value = Unit
        }
    }


}