package com.nota.facesdksample.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.os.Process
import android.view.View
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.nota.facesdksample.R
import com.nota.facesdksample.ThreadStrategy
import com.nota.nota_sdk.ai.support.image.BitmapUtil.cropFace
import com.nota.nota_sdk.task.vision.FacialProcess
import com.nota.nota_sdk.task.vision.face.FacialFeature
import java.util.concurrent.atomic.AtomicBoolean

class InferenceViewModel(application: Application) : AndroidViewModel(application){

    private val fileList = ArrayList<String>()
    private var currentBitmapIndex = 0
    private val threadStrategy = MutableLiveData(ThreadStrategy.UI)
    private val inferenceLock = AtomicBoolean(false)
    val currentBitmap = MutableLiveData<Bitmap>()
    val facialData = MutableLiveData<List<FacialProcess.Result>>()
    val registerFacialFeature = MutableLiveData<FacialFeature>()
    val infTimeLogTxt = MutableLiveData("")

    /* Load sample images */
    fun loadFiles(files: Array<String>?){
        files?.let { fileList.addAll(it) }
        selectCropImage(currentBitmapIndex)
        processImage(currentBitmap.value!!)
    }

    private fun selectCropImage(index: Int){

        // load bitmap from assets
        val bitmap = BitmapFactory.decodeStream(getApplication<Application>().assets.open("faces/"+fileList[index]))

        // crop the bitmap into a square
        if(bitmap.width > bitmap.height){
            val startX = (bitmap.width-bitmap.height)/2f
            val endX = bitmap.width - startX
            currentBitmap.value = bitmap.cropFace(RectF(startX, 0f, endX, bitmap.height.toFloat()),0f)
        }else if(bitmap.width < bitmap.height){
            val startY =(bitmap.height-bitmap.width)/2f
            val endY = bitmap.height - startY
            currentBitmap.value = bitmap.cropFace(RectF(0f, startY, bitmap.width.toFloat(), endY),0f)
        }else{
            currentBitmap.value = bitmap
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


    fun onClickNext():View.OnClickListener{
        return View.OnClickListener {
            if(inferenceLock.get())
                return@OnClickListener

            if(currentBitmapIndex < fileList.size-1){
                currentBitmapIndex++
            }else{
                currentBitmapIndex = 0
            }
            selectCropImage(currentBitmapIndex)
            processImage(currentBitmap.value!!)
        }
    }

    fun onClickPrev():View.OnClickListener{
        return View.OnClickListener {
            if(inferenceLock.get())
                return@OnClickListener

            if(currentBitmapIndex != 0){
                currentBitmapIndex--
            }else{
                currentBitmapIndex = fileList.size-1
            }
            selectCropImage(currentBitmapIndex)
            processImage(currentBitmap.value!!)
        }
    }

    /* Process image for facial result */
    private fun processImage(bitmap: Bitmap){
        inferenceLock.set(true)

        val inference :(bitmap: Bitmap)->Unit = { it->
            FacialProcess.inference(it, FacialProcess.Option()) { results ->

                var totalInferenceTime = 0L

                facialData.postValue(results)
                results.forEach { result ->
                    totalInferenceTime += result.log.fdInferenceTime
                    result.log.frInferenceTime?.let { frInfTime ->
                        totalInferenceTime += frInfTime
                    }

                    result.facialFeature?.let { feature->
                        registerFacialFeature.postValue(feature)
                    }

                    infTimeLogTxt.postValue("Inference Time : $totalInferenceTime")
                }
                inferenceLock.set(false)
            }
        }

        when (threadStrategy.value!!) {
            ThreadStrategy.UI -> {
                inference(bitmap)
            }
            ThreadStrategy.DEFAULT -> {
                Thread {
                    inference(bitmap)
                }.start()
            }
            ThreadStrategy.HIGH_PRIORITY -> {
                val thread = Thread {
                    Process.setThreadPriority(Process.myTid(), -20)
                    inference(bitmap)
                    Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_DEFAULT)
                }
                thread.priority = Thread.MAX_PRIORITY
                thread.start()
            }
        }

    }


    companion object {
        @JvmStatic
        @BindingAdapter("loadImageGlide")
        fun loadImageGlide(imageView: ImageView, bitmap: Bitmap){
            Glide.with(imageView).load(bitmap).into(imageView)
        }
    }

}