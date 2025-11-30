package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.View
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentExperienceBinding
import com.example.tiltok_xsb.utils.SwipeGestureHelper


class ExperienceFragment : BaseBindingFragment<FragmentExperienceBinding>({FragmentExperienceBinding.inflate(it)}), IScrollToTop {

    private var swipeGestureHelper: SwipeGestureHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeGesture()
    }

    private fun setupSwipeGesture() {
        swipeGestureHelper = SwipeGestureHelper(
            context = requireContext(),
            onSwipeLeft = {
                // 向左滑动，切换到同城页（position = 2）
                (parentFragment as? MainFragment)?.switchTab(2)
            },
            onSwipeRight = {
                // 向右滑动，切换到团购页（position = 0）
                (parentFragment as? MainFragment)?.switchTab(0)
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