package com.nota.facesdksample.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.times
import androidx.databinding.BindingAdapter
import com.nota.nota_sdk.task.FacialProcess
import com.nota.nota_sdk.task.vision.face.FacialFeature

/* Display face data on the screen. */
class OverlayView : View {

    private var facialData: List<FacialProcess.Result> = emptyList()
    private val featureList = ArrayList<FacialFeature>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    fun addFacialFeature(feature: FacialFeature) {
        // duplicate FacialFeatures are excluded
        featureList.forEach{
            if(it.isIdentical(feature))
                return
        }
        featureList.add(feature)
    }

    fun drawFacialData(facialData: List<FacialProcess.Result>) {
        this.facialData = facialData
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        facialData.forEach {

            val rectPaint = Paint()
            rectPaint.color = Color.GREEN
            rectPaint.style = Paint.Style.STROKE
            rectPaint.strokeWidth = 3f

            canvas?.drawRect(it.face.rectf.times(width), rectPaint)

            val landmarkPaint = Paint()
            landmarkPaint.color = Color.MAGENTA
            landmarkPaint.style = Paint.Style.FILL

            // draw landmarks
            it.face.landmarks.forEach { point ->
                canvas?.drawCircle(point.x * width, point.y * height, 10f, landmarkPaint)
            }

            // draw rect, id text
            it.facialFeature?.let { feature ->
                featureList.forEachIndexed { index, facialFeature ->
                    if (facialFeature.isIdentical(feature)) {
                        val idPaint = Paint()
                        idPaint.color = Color.GREEN
                        idPaint.style = Paint.Style.FILL
                        idPaint.textSize = 50f
                        canvas?.drawText(
                            "id:${index}",
                            it.face.rectf.centerX().times(width) - 50f,
                            it.face.rectf.bottom.times(height) + 50f,
                            idPaint
                        )
                    }
                }
            }

            postInvalidate()
        }

        if (facialData.isEmpty()) {
            postInvalidate()
        }

    }

    companion object {

        @JvmStatic
        @BindingAdapter("drawFacialData")
        fun drawFacialData(view: OverlayView, facialData: List<FacialProcess.Result>?) {
            facialData?.let {
                view.drawFacialData(facialData)
                view.postInvalidate()
            } ?: run{ // If a null value is entered, the drawn paint is erased.
                view.drawFacialData(emptyList())
                view.postInvalidate()
            }

        }

    }

}