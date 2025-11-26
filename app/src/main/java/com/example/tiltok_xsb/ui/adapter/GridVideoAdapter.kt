package com.example.tiltok_xsb.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.databinding.ItemGridvideoBinding
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.base.BaseAdapter
import com.example.tiltok_xsb.ui.adapter.GridVideoAdapter.GridVideoViewHolder


class GridVideoAdapter(private val context: Context,
                       private val onItemClick: (VideoBean, Int) -> Unit,
                       private val onAvatarClick: (VideoBean, Int) -> Unit,
                       private val onLikeClick: (VideoBean, Int) -> Unit) : BaseAdapter<GridVideoViewHolder, VideoBean>(VideoDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridVideoViewHolder {
        return GridVideoViewHolder(ItemGridvideoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    //绑定数据到ViewHolder
    override fun onBindViewHolder(holder: GridVideoViewHolder, position: Int) {
        val video=mList.getOrNull(holder.bindingAdapterPosition)?:return

        with(holder.binding){
            //加载视频封面
            loadVideoCover(video,holder)

            //加载作者头像
            loadAuthorAvatar(video,holder)

            //设置文字信息
            tvContent.text=video.content
            ivAuthorName.text=video.userBean?.nickName?:"抖音用户xxx"
            ivLikeCount.text=formatLikeCount(video.likeCount)

            //点击整个卡片跳转

            //点击头像跳转

            //点击点赞区域

        }


    }

    //加载视频封面（第一帧）
    private fun loadVideoCover(video: VideoBean, holder: GridVideoViewHolder){
        Glide.with(context)
            .asBitmap()
            .load(video.videoRes)
            .apply(
                RequestOptions()
                .frame(0)
                .placeholder(R.drawable.cover1)//占位图
                .error(R.drawable.cover1)//加载失败时的错误图
            )
            .into(holder.binding.ivCover)
    }

    //加载作者头像
    private fun loadAuthorAvatar(video: VideoBean, holder: GridVideoViewHolder){
        Glide.with(context)
            //优先显示视频所属用户的自定义头像，若用户信息不存在、头像为空或无效，则显示默认头像
            .load(video.userBean?.headId?:R.drawable.default_avatar)
            .placeholder(R.drawable.cover1)
            .error(R.drawable.cover1)
            .circleCrop()                   //将加载的头像裁剪为圆形
            .into(holder.binding.ivAvatar)//将加载的头像显示到ImageView中
    }

    //格式化点赞数量
    private fun formatLikeCount(count:Int):String{
        return when {
            count < 1000 -> count.toString()
            count < 10000 -> String.format("%.1fk", count / 1000.0)
            else -> String.format("%.1fw", count / 10000.0)
        }
    }

    //点击卡片跳转到播放页

    //点击作者区域跳转到作者页面

    //更新指定位置的点赞状态
    fun updateLikeStatus(position: Int, isLiked: Boolean) {
        mList.getOrNull(position)?.let { video ->
            video.isLiked = isLiked
            notifyItemChanged(position)
        }
    }

    //点赞动画效果

    inner class GridVideoViewHolder(val binding: ItemGridvideoBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}