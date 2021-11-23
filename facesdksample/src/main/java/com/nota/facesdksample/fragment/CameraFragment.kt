package com.nota.facesdksample.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nota.facesdksample.R
import com.nota.facesdksample.databinding.LayoutCameraBinding
import com.nota.facesdksample.view.CameraXView
import com.nota.facesdksample.viewmodel.CameraViewModel

class CameraFragment : Fragment() {

    private lateinit var binding: LayoutCameraBinding
    private lateinit var viewModel: CameraViewModel
    private var cameraXView: CameraXView? = null

    private val bitmapCallback :(bitmap: Bitmap) -> Unit = { bitmap ->
        viewModel.processImage(bitmap)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(inflater,
            R.layout.layout_camera, container, false)
        viewModel = ViewModelProvider(this).get(CameraViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.cameraSelector.observe(viewLifecycleOwner){
            initCamera(it)
        }

        viewModel.registerFacialFeature.observe(viewLifecycleOwner){
            binding.cameraOverlay.addFacialFeature(it)
        }

        return binding.root
    }

    private fun initCamera(cameraFacing: CameraSelector) {
        cameraXView = CameraXView(requireContext(), cameraFacing, bitmapCallback, viewLifecycleOwner)
        binding.cameraContainer.removeAllViews()
        binding.cameraContainer.addView(cameraXView)
    }

}