package com.nota.hyundai_door.ui

import android.graphics.*
import com.nota.nota_sdk.task.vision.face.Face

object BitmapUtils {

    fun Bitmap.flip(): Bitmap {
        val sideInversion = Matrix()

        sideInversion.setScale(-1f, 1f)  // 좌우반전
        return Bitmap.createBitmap(this, 0, 0,
            width, height, sideInversion, false)
    }

     fun Bitmap.overlayAlignLandmark(box: Face) : Bitmap {

        val bmOverlay = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(bmOverlay)
        val paint = Paint()
        val rectF = RectF(box.rectf)

        rectF.top *= height
        rectF.left *= width
        rectF.bottom *= height
        rectF.right *= width

        val drawPoints = ArrayList<PointF>(box.landmarks)
        drawPoints.forEach {
            it.x *= width
            it.y *= height
        }

        val colors = arrayOf(Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED)

        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
        canvas.drawBitmap(this, Matrix(), null)
        canvas.drawRect( rectF, paint )

        for( i in box.landmarks.indices){
            paint.color = colors[i % 6]
            canvas.drawCircle(drawPoints[i].x, drawPoints[i].y, 10f, paint)
        }

        return bmOverlay
    }
}