package com.example.tiltok_xsb.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseBindingFragment<VB : ViewBinding>(
    val block: (LayoutInflater) -> VB
) : Fragment() {


    //创建内部存储的 binding 实例（可空），用于在生命周期内管理 binding 的创建与销毁
        private var _binding:VB?=null

        //对外暴露的非空 binding 实例，提供安全访问
        protected val binding:VB
            get()= requireNotNull(_binding){"The property of binding has been destroyed."}

        //重写 Fragment 视图创建方法，初始化 ViewBinding 并返回根视图
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding=block(inflater)
            return binding.root
        }

        // 视图销毁时释放 binding，防止内存泄漏
        override fun onDestroyView() {
            super.onDestroyView()
            _binding=null
        }
}
