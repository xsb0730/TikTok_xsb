package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentGroupBuyBinding
import com.example.tiltok_xsb.utils.SwipeGestureHelper

class GroupBuyFragment : BaseBindingFragment<FragmentGroupBuyBinding>({FragmentGroupBuyBinding.inflate(it)}), IScrollToTop {

    private var swipeGestureHelper: SwipeGestureHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSwipeGesture()
    }

    private fun setupSwipeGesture() {
        swipeGestureHelper = SwipeGestureHelper(
            context = requireContext(),
            onSwipeLeft = {
                // 向左滑动，切换到经验页（position = 1）
                (parentFragment as? MainFragment)?.switchTab(1)
            },
            onSwipeRight = {
                // 团购页（position = 0）已是第一页
                Toast.makeText(context, "已经是第一页了", Toast.LENGTH_SHORT).show()
            }
        )
        swipeGestureHelper?.attachToRecyclerView(binding.recyclerView)
    }

    override fun scrollToTop() {
        // 空白页面无需处理
    }

    override fun onDestroyView() {
        super.onDestroyView()
        swipeGestureHelper = null
    }
}