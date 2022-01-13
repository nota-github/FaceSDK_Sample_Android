package com.nota.hyundai_lobby

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.nota.hyundai_lobby.databinding.ActivityCameraBinding
import com.nota.hyundai_lobby.http.HttpRepository
import com.nota.hyundai_lobby.ui.*
import com.nota.hyundai_lobby.ui.view.CameraView
import com.nota.hyundai_lobby.ui.viewmodel.CameraViewModel
import com.nota.nota_sdk.NotaFaceSDK

class CameraActivity : AppCompatActivity(),
    BitmapCallback {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var viewModel: CameraViewModel
    private var cameraView: CameraView? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        NotaFaceSDK.initialize(this)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        HttpRepository.createInstance()

        if (allPermissionsGranted()) {
            bindCameraView(Camera.CameraInfo.CAMERA_FACING_BACK)
            adaptOnActivity()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            bindCameraView(Camera.CameraInfo.CAMERA_FACING_BACK)
            adaptOnActivity()
        } else {
            Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPause() {
        super.onPause()
        if (allPermissionsGranted()) {
            cameraView?.pauseCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            cameraView?.resumeCamera()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    private fun adaptOnActivity() {
        viewModel.updateUserList()

        viewModel.toastMessage.observe(this) {
            if (it != "") {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.cameraSelector.observe(this) {
            bindCameraView(it)
        }

        viewModel.showConfirmDialogEvent.observe(this) {

            if(viewModel.startDate.value == null || viewModel.endDate.value == null) {
                val dialog = MemberConfirmDialog(this, it.detectedFaceBitmap)
                dialog.setOnDismissListener {
                    if (dialog.isConfirm()) {
                        viewModel.confirmMemberRegiImage(
                            dialog.getName(),
                            dialog.getDong(),
                            dialog.getHo()
                        )
                    }
                }
                dialog.show()
            } else {
                val dialog = VisitorConfirmDialog(this, it.detectedFaceBitmap)
                dialog.setOnDismissListener {
                    if (dialog.isConfirm()) {
                        viewModel.confirmVisitorRegiImage(
                            dialog.getName(),
                            dialog.getDong(),
                            dialog.getHo(),
                            dialog.getInfo()
                        )
                    }
                }
                dialog.show()
            }
        }

        viewModel.showGuideDialogEvent.observe(this) {
            val dialog = GuideDialog(this)
            dialog.setOnDismissListener {
                viewModel.startRegisterUser()
            }
            dialog.show()
        }

        viewModel.showDatePickerDialogEvent.observe(this) {
            Toast.makeText(this, "방문을 시작하실 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()

            val startDatePicker = DatePickerDialog(this)
            val endDatePicker = DatePickerDialog(this)

            startDatePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                viewModel.startDate.value = "$year-${String.format("%02d", month+1)}-${String.format("%02d", dayOfMonth)}"
                Toast.makeText(this, "방문을 종료하실 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                endDatePicker.show()
            }

            endDatePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                viewModel.endDate.value = "$year-${String.format("%02d", month+1)}-${String.format("%02d", dayOfMonth)}"
                viewModel.showGuideDialogEvent.call()
            }

            startDatePicker.show()
        }

        viewModel.currentState.observe(this) {

            when(it){
                CameraViewModel.State.REGISTRATION, CameraViewModel.State.RECOGNITION-> {
                    cameraView?.setStateLock(false)
                }

                else -> {
                    cameraView?.setStateLock(true)
                }
            }

        }

        viewModel.showIntruderLogManagementDialogEvent.observe(this) {
            IntruderLogManagementDialog(this).show()
        }

        viewModel.showEnterLogManagementDialogEvent.observe(this) {
            EnterLogManagementDialog(this).show()
        }

        viewModel.showUserManagementDialogEvent.observe(this) {
            UserManagementDialog(this).show()
        }

    }

    private fun bindCameraView(cameraFacing: Int) {

        cameraView?.pauseCamera()
        cameraView = CameraView(this, this, cameraFacing)
        val container: FrameLayout =
            findViewById<View>(R.id.camera_preview) as FrameLayout
        try {
            // Add Bitmap Listener
            container.addView(cameraView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** @see CameraView
     *  @see BitmapCallback
     * Receive camera frame as bitmap **/
    override fun toBitmap(bitmap: Bitmap, orientation: Int) {
        viewModel.getCameraFrame(bitmap)
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}