package com.example.tiltok_xsb.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tiltok_xsb.base.BaseAdapter
import com.example.tiltok_xsb.databinding.ItemVideoBinding
import com.example.tiltok_xsb.data.model.VideoBean

@UnstableApi
class VideoAdapter(val context: Context, val recyclerView: RecyclerView):BaseAdapter<VideoAdapter.VideoViewHolder, VideoBean>(VideoDiff()) {

    //创建ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(ItemVideoBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    //绑定ViewHolder
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {

    }

    //构建共用缓存文件



    inner class VideoViewHolder(val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}
