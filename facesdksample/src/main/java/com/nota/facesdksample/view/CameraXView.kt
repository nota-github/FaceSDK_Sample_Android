package com.nota.facesdksample.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Size
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/* Use the CameraX API to extract Bitmap data from the camera. */
@SuppressLint("ViewConstructor")
class CameraXView(context: Context, private val cameraPacing: CameraSelector,
                  private val bitmapCallback:(bitmap: Bitmap)->Unit, private val lifecycleOwner: LifecycleOwner) : FrameLayout(context) {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val previewView = PreviewView(context)

    // Initialize CameraX
    init {
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        addView(previewView)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener( {

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector: CameraSelector = cameraPacing

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(previewView.width, previewView.height))
                .setTargetRotation(display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                cameraExecutor,
                BitmapAnalyzer(bitmapCallback)
            )

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis)

            } catch(exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /* CameraX API Analyzer - frame image -> Bitmap return */
    class BitmapAnalyzer(private val bitmapCallback:(bitmap: Bitmap)->Unit) : ImageAnalysis.Analyzer {

        @ExperimentalGetImage
        override fun analyze(image: ImageProxy) {
            image.image?.let {
                val bitmap = rotateImage(it.toBitmap(), image.imageInfo.rotationDegrees)
                bitmapCallback(bitmap)
            }
            image.close()
        }

        // Image Obj to Bitmap Convert
        private fun Image.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer // Y
            val uBuffer = planes[1].buffer // U
            val vBuffer = planes[2].buffer // V

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            //U and V are swapped
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        private fun rotateImage(img: Bitmap, degree: Int): Bitmap {
            val matrix = Matrix()
            matrix.setScale(-1f,1f)
            matrix.postRotate(360-degree.toFloat())
            val rotatedImg =
                Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, false)
            img.recycle()
            return rotatedImg
        }
    }



}