package com.example.tiltok_xsb.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tiltok_xsb.databinding.ItemVideoPlayBinding
import com.bumptech.glide.Glide
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.data.model.VideoBean

class VideoPlayAdapter(private val videoList:List<VideoBean>):RecyclerView.Adapter<VideoPlayAdapter.VideoViewHolder>() {

    private var currentPlayingPosition=-1
    private val videoHolders= mutableMapOf<Int,VideoViewHolder>()

    inner class  VideoViewHolder(val binding: ItemVideoPlayBinding):RecyclerView.ViewHolder(binding.root){

        private var isVideoPlaying=false
        private var recordAnimator:ObjectAnimator?=null

        fun bind(video: VideoBean, position: Int) {
            with(binding){
                //设置封面
                Glide.with(ivCover)
                    .load(video.coverRes)
                    .placeholder(R.drawable.default_cover)
                    .into(ivCover)

                //设置头像
                Glide.with(ivAvatar)
                    .load(video.userBean?.headId)
                    .placeholder(R.drawable.default_avatar)
                    .into(ivAvatar)

                //设置唱片封面
                Glide.with(ivHeadAnim)
                    .load(video.userBean?.headId)
                    .placeholder(R.drawable.default_avatar)
                    .into(ivHeadAnim)

                //设置文字信息

                //设置点赞状态

                //设置关注状态

                //点赞点击事件

                //评论点击事件

                //收藏点击事件

                //分享点击事件

                //头像点击事件

                //关注按钮点击事件

                //唱片点击事件（暂停/播放）

                //双击点赞

                //设置视频

                //视频加载失败

                //视频播放完成
            }
        }

        //保存ViewHolder

    }

    //播放点赞动画

    //切换播放/暂停

    //开始播放视频

    //暂停视频

    //释放视频资源

    //开始唱片旋转动画

    //暂停唱片旋转动画

    //停止唱片旋转动画

    //页面选中时调用
    fun onPageSelected(position: Int){

    }

    //恢复当前视频
    fun resumeCurrentVideo(){

    }

    //暂停当前视频
    fun pauseCurrentVideo(){

    }

    //释放所有视频资源
    fun releaseAllVideos(){

    }

    //格式化数字
    private fun formatCount(count:Int):String{
        return when {
            count >= 10000 -> "${count / 10000}.${(count % 10000) / 1000}w"
            count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
            else -> count.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding=ItemVideoPlayBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  VideoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position],position)
    }
}