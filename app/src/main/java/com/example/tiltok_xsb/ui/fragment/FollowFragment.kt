package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.View
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentFollowBinding
import com.example.tiltok_xsb.utils.SwipeGestureHelper

class FollowFragment : BaseBindingFragment<FragmentFollowBinding>({FragmentFollowBinding.inflate(it)}), IScrollToTop {

    private var swipeGestureHelper: SwipeGestureHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeGesture()
    }

    private fun setupSwipeGesture() {
        swipeGestureHelper = SwipeGestureHelper(
            context = requireContext(),
            onSwipeLeft = {
                // 向左滑动，切换到商场页（position = 4）
                (parentFragment as? MainFragment)?.switchTab(4)
            },
            onSwipeRight = {
                // 向右滑动，切换到同城页（position = 2）
                (parentFragment as? MainFragment)?.switchTab(2)
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