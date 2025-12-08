package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.View
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentSameCityBinding

class SameCityFragment : BaseBindingFragment<FragmentSameCityBinding>({FragmentSameCityBinding.inflate(it)}), IScrollToTop {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun scrollToTop() {}

    override fun onDestroyView() {
        super.onDestroyView()
    }
}