package com.nota.facesdksample.viewmodel

import android.graphics.Bitmap
import android.os.Process
import android.view.View
import android.widget.RadioGroup
import androidx.annotation.WorkerThread
import androidx.camera.core.CameraSelector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nota.facesdksample.R
import com.nota.facesdksample.ThreadStrategy
import com.nota.nota_sdk.task.FacialProcess
import com.nota.nota_sdk.task.vision.face.FacialFeature
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.round


class CameraViewModel : ViewModel(){

    enum class State {
        IDLE, REGISTRATION
    }

    private val inferenceLock = AtomicBoolean(false)
    private val threadStrategy = MutableLiveData(ThreadStrategy.UI)
    private val currentState = MutableLiveData(State.IDLE)
    val registerFacialFeature = MutableLiveData<FacialFeature>()
    val featureExtractMode = MutableLiveData(false)
    val cameraSelector = MutableLiveData(CameraSelector.DEFAULT_FRONT_CAMERA)
    val facialData = MutableLiveData<List<FacialProcess.Result>>()
    val fpsLogTxt = MutableLiveData("")
    val fdLogTxt = MutableLiveData("")
    val frLogTxt = MutableLiveData("")

    fun onSwitchState(state: State) : View.OnClickListener{
        return View.OnClickListener {
            currentState.value = state
        }
    }

    val onSwitchCamera = View.OnClickListener {
        if(cameraSelector.value == CameraSelector.DEFAULT_FRONT_CAMERA){
            cameraSelector.value = CameraSelector.DEFAULT_BACK_CAMERA
        }else{
            cameraSelector.value = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    val strategyChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rb_ui ->
                    threadStrategy.value = ThreadStrategy.UI

                R.id.rb_default->
                    threadStrategy.value = ThreadStrategy.DEFAULT

                R.id.rb_high->
                    threadStrategy.value = ThreadStrategy.HIGH_PRIORITY
            }
        }

    /* Implement processing strategies */
    fun processImage(bitmap: Bitmap){

        if(!inferenceLock.get()) {
            inferenceLock.set(true)

            when (threadStrategy.value!!) {
                ThreadStrategy.UI -> {
                    recognitionFace(bitmap)
                }
                ThreadStrategy.DEFAULT -> {
                    Thread {
                        recognitionFace(bitmap)
                    }.start()
                }
                ThreadStrategy.HIGH_PRIORITY -> {
                    val thread = Thread {
                        Process.setThreadPriority(Process.myTid(), -20)
                        recognitionFace(bitmap)
                        Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_DEFAULT)
                    }
                    thread.priority = Thread.MAX_PRIORITY
                    thread.start()
                }
            }
        }

    }

    /* Execute face recognition process and bind data
    *
    * If you run face recognition on the UI Thread,
    * you can get the best results in terms of performance,
    * but there is a problem in drawing the UI.
    * Therefore,
    * it is recommended to handle the UI drawing and face recognition separately,
    * or to do the work in a thread.*/
    @WorkerThread
    private fun recognitionFace(bitmap: Bitmap){

        FacialProcess.inference(bitmap, featureExtractMode.value!!) { results->
            facialData.postValue(results)
            results.forEach{ result->

                var totalInferenceTime = 0L

                result.facialFeature?.let { feature->
                    if(currentState.value == State.REGISTRATION) {
                        currentState.postValue(State.IDLE)
                        registerFacialFeature.postValue(feature)
                    }
                }

                fdLogTxt.postValue("faceDetector : ${result.log.fdInferenceTime}ms")
                totalInferenceTime += result.log.fdInferenceTime
                result.log.frInferenceTime?.let { frInfTime->
                    frLogTxt.postValue("featureExtractor : ${frInfTime}ms")
                    totalInferenceTime += frInfTime
                } ?: run {
                    frLogTxt.postValue("")
                }

                fpsLogTxt.postValue("FPS : ${round(1000f/totalInferenceTime).toInt()}")

            }
            inferenceLock.set(false)
        }
    }

}