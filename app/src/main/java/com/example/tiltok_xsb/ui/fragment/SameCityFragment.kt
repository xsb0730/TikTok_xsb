package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentSameCityBinding
import com.example.tiltok_xsb.utils.SwipeGestureHelper

class SameCityFragment : BaseBindingFragment<FragmentSameCityBinding>({FragmentSameCityBinding.inflate(it)}), IScrollToTop {

    private var swipeGestureHelper: SwipeGestureHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeGesture()
    }

    private fun setupSwipeGesture() {
        swipeGestureHelper = SwipeGestureHelper(
            context = requireContext(),
            onSwipeLeft = {
                // 向左滑动，切换到推荐页
                (parentFragment as? MainFragment)?.switchTab(1)
            },
            onSwipeRight = {
                Toast.makeText(context, "已经是第一页了", Toast.LENGTH_SHORT).show()
            }
        )
        swipeGestureHelper?.attachToRecyclerView(binding.recyclerView)
    }

    override fun scrollToTop() {}

    override fun onDestroyView() {
        super.onDestroyView()
        swipeGestureHelper = null
    }
}