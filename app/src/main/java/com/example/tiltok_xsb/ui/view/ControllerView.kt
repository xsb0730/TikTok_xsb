package com.example.tiltok_xsb.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.example.tiltok_xsb.data.model.VideoBean

class ControllerView @JvmOverloads constructor(context:Context,attrs:AttributeSet?=null):RelativeLayout(context,attrs), View.OnClickListener{
    private var listener:OnVideoControllerListener?=null
    private var videoData: VideoBean?=null

    override fun onClick(v: View?) {
        if(listener==null){
            return
            }
        when(v?.id){

        }
    }

    init {
        init()
    }

    private fun init(){

    }
}