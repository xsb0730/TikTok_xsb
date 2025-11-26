package com.example.tiltok_xsb.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.tiltok_xsb.data.model.VideoBean

class VideoDiff: DiffUtil.ItemCallback<VideoBean>(){
    override fun areItemsTheSame(oldItem: VideoBean, newItem: VideoBean): Boolean {
        return (oldItem.userBean!!.userId==newItem.userBean!!.userId)
    }

    override fun areContentsTheSame(oldItem: VideoBean, newItem: VideoBean): Boolean {
        return (oldItem.videoRes==newItem.videoRes&&oldItem.userBean!!.userId==newItem.userBean!!.userId)
    }
}