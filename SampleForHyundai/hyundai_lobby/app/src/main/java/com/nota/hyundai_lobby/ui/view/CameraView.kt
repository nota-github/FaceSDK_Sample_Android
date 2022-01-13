package com.nota.hyundai_lobby.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.nota.hyundai_lobby.ui.BitmapCallback
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Camera1 API 로 구현
 * 카메라 Preview Frame 을 Bitmap 으로 Callback 수행
 * @see BitmapCallback
 ** */
@SuppressLint("ViewConstructor")
class CameraView(context: Context, var bitmapListener: BitmapCallback, private val cameraFacing: Int = Camera.CameraInfo.CAMERA_FACING_FRONT) : SurfaceView(context),
    SurfaceHolder.Callback, Camera.PreviewCallback {

    private var camera: Camera? = null

    // 비지니스 로직에 따라 불필요한 CPU 리소스 소모를 막기 위해 Bitmap Callback 수행 정지
    private var stateLock = AtomicBoolean(false)
    // 카메라 프레임이 연속으로 들어와서 프로세스가 중첩 실행되는 것을 방지
    private var duplicateLock = AtomicBoolean(false)

    companion object {
        const val PREVIEW_FORMAT = ImageFormat.NV21
    }

    init {
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (holder.surface == null) {
            return
        }
        try {
            camera?.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            camera?.setPreviewDisplay(holder)

            val params = camera?.parameters
            params?.previewFormat = PREVIEW_FORMAT
            val previewSize = params?.supportedPreviewSizes
            params?.setPreviewSize(previewSize?.get(0)!!.width, previewSize.get(0)!!.height)
            camera?.parameters = params

            camera?.setPreviewCallback(this)
            camera?.startPreview()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun surfaceCreated(holder: SurfaceHolder) {
        camera?.stopPreview()
        camera?.release()

        val cameraId: Int = getCameraIndex(cameraFacing)
        if (cameraId != -1) {
            try {
                camera = Camera.open(cameraId)
                val windowManager: WindowManager =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val rotation: Int = windowManager.getDefaultDisplay().rotation
                var windowRotation = 0

                when (rotation) {
                    Surface.ROTATION_0 -> windowRotation = 0
                    Surface.ROTATION_90 -> windowRotation = 90
                    Surface.ROTATION_180 -> windowRotation = 180
                    Surface.ROTATION_270 -> windowRotation = 270
                }
                val cameraInfo =
                    Camera.CameraInfo()
                Camera.getCameraInfo(cameraId, cameraInfo)

                val cameraRotation: Int
                val orientation: Int

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraRotation = (cameraInfo.orientation + windowRotation) % 360
                    orientation = (360 - cameraRotation) % 360
                } else {
                    cameraRotation = (cameraInfo.orientation - windowRotation + 360) % 360
                    orientation = cameraRotation
                }

                camera?.setDisplayOrientation(orientation)
                camera?.setPreviewDisplay(holder)

                val params = camera?.parameters
                params?.previewFormat = PREVIEW_FORMAT
                val previewSize = params?.supportedPreviewSizes
                params?.setPreviewSize(previewSize?.get(0)!!.width, previewSize.get(0)!!.height)
                camera?.parameters = params
                camera?.setPreviewCallback(this)
                camera?.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getCameraIndex(facing: Int): Int {
        val cameraInfo = Camera.CameraInfo()
        for (index in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(index, cameraInfo)
            if (cameraInfo.facing == facing) {
                return index
            }
        }
        return -1
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if(stateLock.get()) return
        if(duplicateLock.get()) return
        duplicateLock.set(true)

        try {

            val image = YuvImage(
                data,
                camera!!.parameters!!.previewFormat,
                camera.parameters!!.previewSize.width,
                camera.parameters!!.previewSize.height,
                null
            )
            val bos = ByteArrayOutputStream()

            // Detect 연산의 Input Ratio(1:1)를 맞추기 위해 1920x1080 해상도의 Frame 을 1080x1080 으로 Crop
            // ( Bitmap 처리 속도 및 Detect Process Inference 속도와 정확도에 영향을 끼침 )
            val cropWidthLeft = camera.parameters!!.previewSize.width / 2 - 540
            val cropWidthRight = camera.parameters!!.previewSize.width / 2 + 540

            image.compressToJpeg(
                Rect(
                    cropWidthLeft,
                    0,
                    cropWidthRight,
                    camera.parameters!!.previewSize.height
                ), 80,
                bos
            )
            val imageBytes = bos.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            bitmapListener.toBitmap(bitmap, 0)
            duplicateLock.set(false)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }

    }

    fun pauseCamera() {
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.lock()
    }

    fun resumeCamera() {
        camera?.unlock()
        camera?.startPreview()
    }

    fun setStateLock(lock: Boolean){
        stateLock.set(lock)
    }

}