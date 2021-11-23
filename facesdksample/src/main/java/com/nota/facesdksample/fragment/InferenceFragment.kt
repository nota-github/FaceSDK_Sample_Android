package com.nota.facesdksample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nota.facesdksample.R
import com.nota.facesdksample.databinding.LayoutInferenceBinding
import com.nota.facesdksample.viewmodel.InferenceViewModel

class InferenceFragment : Fragment() {

    private lateinit var binding: LayoutInferenceBinding
    private lateinit var viewModel: InferenceViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_inference, container, false
        )
        viewModel = ViewModelProvider(this).get(InferenceViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loadFiles(requireContext().assets.list("faces"))

        viewModel.registerFacialFeature.observe(viewLifecycleOwner){
            binding.imageOverlay.addFacialFeature(it)
        }

        return binding.root
    }
}