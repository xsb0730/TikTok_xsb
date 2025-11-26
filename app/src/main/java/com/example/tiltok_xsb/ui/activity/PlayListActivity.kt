package com.example.tiltok_xsb.ui.activity

import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.base.BaseBindingActivity
import com.example.tiltok_xsb.databinding.ActivityPlayListBinding
import com.example.tiltok_xsb.ui.fragment.RecommendFragment


/**
 * 播放列表页,承载视频播放相关的 Fragment（RecommendFragment）+ 传递初始化播放位置
 */
class PlayListActivity : BaseBindingActivity<ActivityPlayListBinding>({ActivityPlayListBinding.inflate(it)}) {

    override fun init() {
        supportFragmentManager.beginTransaction().add(R.id.framelayout,RecommendFragment()).commit()
    }

    companion object {
        var initPos = 0
    }
}


