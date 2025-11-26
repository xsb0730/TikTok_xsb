package com.example.tiltok_xsb.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tiltok_xsb.databinding.ItemVideoPlayBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.ui.viewmodel.VideoPlayViewModel

class VideoPlayAdapter(
    private val videoList:List<VideoBean>,
    private val viewModel: VideoPlayViewModel
):RecyclerView.Adapter<VideoPlayAdapter.VideoViewHolder>() {

    private var currentPlayingPosition = -1
    private val videoHolders = mutableMapOf<Int, VideoViewHolder>()

    //缓存列表项布局中的所有控件，绑定控件的点击、状态更新等逻辑，将视频数据绑定到布局控件上实现列表项的渲染
    inner class VideoViewHolder(val binding: ItemVideoPlayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isVideoPlaying = false
        private var recordAnimator: ObjectAnimator? = null
        private lateinit var currentVideo: VideoBean

        //将视频数据绑定到ViewHolder的视图上
        fun bind(video: VideoBean, position: Int) {
            currentVideo = video

            with(binding) {
                //加载封面
                loadCover(video)

                //加载头像
                loadAvatar(video)

                //加载唱片封面
                Glide.with(ivHeadAnim)
                    .load(video.userBean?.headId ?: R.drawable.default_avatar)
                    .apply(RequestOptions().circleCrop())
                    .into(ivHeadAnim)


                //设置文字信息
                tvNickname.text = "@${video.userBean?.nickName ?: "抖音用户"}"
                tvTitle.text = video.content ?: ""

                //设置统计数据
                updateUIState(video)

                //设置关注状态
                ivFollow.visibility =
                    if (video.userBean?.isFollowed == true) View.GONE else View.VISIBLE

                setupClickListeners(video, position)
                setupVideoPlayer(video)
            }

            //保存ViewHolder
            videoHolders[position] = this
        }

        //加载封面
        private fun loadCover(video: VideoBean) {
            if(video.coverRes!=0) {
                Glide.with(binding.ivCover)
                    .load(video.coverRes)
                    .into(binding.ivCover)
            }else
            {
                Glide.with(binding.ivCover)
                    .asBitmap()
                    .load(video.videoRes)
                    .apply (RequestOptions().frame(0))
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.default_error)
                    .into(binding.ivCover)
            }
        }

        //加载头像
        private fun loadAvatar(video: VideoBean) {
            val avatarRes = video.userBean?.headId ?: R.drawable.default_avatar
            Glide.with(binding.ivAvatar)
                .load(avatarRes)
                .apply(RequestOptions().circleCrop())
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(binding.ivAvatar)
        }

        //更新推荐单列页面状态
        private fun updateUIState(video: VideoBean) {
            with(binding) {
                // 点赞状态
                ivLike.text = if (video.isLiked) {
                    ivLike.context.getString(R.string.icon_like_fill)
                } else {
                    ivLike.context.getString(R.string.icon_like_border)
                }
                ivLike.setTextColor(
                    if (video.isLiked) {
                        ivLike.context.getColor(R.color.red)
                    } else {
                        ivLike.context.getColor(R.color.white)
                    }
                )

                // 数字
                tvLikecount.text = formatCount(video.likeCount)
                tvCommentcount.text = formatCount(video.commentCount)
                tvCollectcount.text = formatCount(0) // 暂无收藏数
                tvSharecount.text = formatCount(video.shareCount)
            }
        }

        //设置点击监听
        private fun setupClickListeners(video: VideoBean, position: Int) {
            with(binding) {
                // 点赞
                rlLike.setOnClickListener {
                    viewModel.toggleLike(video, position)
                }

                // 评论
                ivComment.setOnClickListener {
                    Toast.makeText(it.context, "评论功能待实现", Toast.LENGTH_SHORT).show()
                }

                // 收藏
                ivCollect.setOnClickListener {
                    viewModel.toggleCollect(video, position)
                }

                // 分享
                ivShare.setOnClickListener {
                    Toast.makeText(it.context, "分享功能待实现", Toast.LENGTH_SHORT).show()
                }

                // 头像
                ivAvatar.setOnClickListener {
                    Toast.makeText(it.context, "进入用户主页", Toast.LENGTH_SHORT).show()
                }

                // 关注
                ivFollow.setOnClickListener {
                    viewModel.followUser(video, position)
                }

                // 唱片点击
                rlRecord.setOnClickListener {
                    togglePlayPause()
                }

                // 视频点击（单击暂停/双击点赞）
                var lastClickTime = 0L
                videoView.setOnClickListener {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime < 300) {
                        // 双击点赞
                        if (!video.isLiked) {
                            viewModel.toggleLike(video, position)
                            playLikeAnimation()
                        }
                    } else {
                        // 单击暂停/播放
                        togglePlayPause()
                    }
                    lastClickTime = currentTime
                }
            }
        }

        //视频播放器初始状态
        private fun setupVideoPlayer(video: VideoBean) {
            with(binding) {
                videoView.setVideoPath(video.videoRes)

                videoView.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    progressBar.visibility = View.GONE
                    ivCover.visibility = View.GONE
                    isVideoPlaying = true
                }

                videoView.setOnErrorListener { _, what, extra ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        videoView.context,
                        "视频加载失败 (错误码: $what-$extra)",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            }
        }








        //点赞动画播放
        private fun playLikeAnimation() {
            with(binding.likeAnimationView) {
                setAnimation(R.raw.like_animation)
                playAnimation()
            }
        }

        //切换播放/暂停
        private fun togglePlayPause() {
            with(binding.videoView) {
                if (isPlaying) {
                    pause()
                    pauseRecordAnimation()
                    isVideoPlaying = false
                } else {
                    start()
                    startRecordAnimation()
                    isVideoPlaying = true
                }
            }
        }

        //播放
        fun play() {
            binding.progressBar.visibility = View.VISIBLE
            binding.videoView.start()
            startRecordAnimation()
            isVideoPlaying = true
        }

        //暂停
        fun pause() {
            binding.videoView.pause()
            pauseRecordAnimation()
            isVideoPlaying = false
        }

        //视频资源释放，播放状态重置
        fun release() {
            binding.videoView.stopPlayback()
            stopRecordAnimation()
            isVideoPlaying = false
        }

        //启动唱片旋转动画的方法
        private fun startRecordAnimation() {
            if (recordAnimator == null) {
                recordAnimator = ObjectAnimator.ofFloat(
                    binding.rlRecord,
                    "rotation",
                    0f,
                    360f
                ).apply {
                    duration = 10000
                    repeatCount = ObjectAnimator.INFINITE
                    interpolator = LinearInterpolator()
                }
            }
            recordAnimator?.start()
        }

        //暂停唱片旋转动画的方法
        private fun pauseRecordAnimation() {
            recordAnimator?.pause()
        }

        //停止唱片旋转动画，重置状态
        private fun stopRecordAnimation() {
            recordAnimator?.cancel()
            recordAnimator = null
            binding.rlRecord.rotation = 0f
        }





        //更新点赞状态，并同步UI与动画
        fun updateLikeState(isLiked: Boolean) {
            currentVideo.isLiked = isLiked
            updateUIState(currentVideo)
            if (isLiked) {
                playLikeAnimation()
            }
        }

        //更新收藏状态
        fun updateCollectState(isCollected: Boolean) {
            currentVideo.isCollected = isCollected
            updateUIState(currentVideo)
        }

        //更新关注状态
        fun updateFollowState(isFollowed: Boolean) {
            currentVideo.userBean?.isFollowed = isFollowed
            binding.ivFollow.visibility = if (isFollowed) View.GONE else View.VISIBLE
        }






        //格式化数字
        private fun formatCount(count: Int): String {
            return when {
                count >= 10000 -> "${count / 10000}.${(count % 10000) / 1000}w"
                count >= 1000 -> "${count / 1000}.${(count % 1000) / 100}k"
                else -> count.toString()
            }
        }
    }






    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPlayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position], position)
    }

    //更新列表中指定位置视频的点赞状态
    fun updateLikeStatus(position: Int, isLiked: Boolean) {
        videoHolders[position]?.updateLikeState(isLiked)
    }

    //更新列表中指定位置视频的收藏状态
    fun updateCollectStatus(position: Int, isCollected: Boolean) {
        videoHolders[position]?.updateCollectState(isCollected)
    }

    //更新列表中指定位置视频的关注状态
    fun updateFollowStatus(position: Int, isFollowed: Boolean) {
        videoHolders[position]?.updateFollowState(isFollowed)
    }

    fun onPageSelected(position: Int) {
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            videoHolders[currentPlayingPosition]?.pause()
        }
        videoHolders[position]?.play()
        currentPlayingPosition = position
    }

    //恢复当前视频
    fun resumeCurrentVideo() {
        if (currentPlayingPosition != -1) {
            videoHolders[currentPlayingPosition]?.play()
        }
    }

    //暂停当前视频
    fun pauseCurrentVideo() {
        if (currentPlayingPosition != -1) {
            videoHolders[currentPlayingPosition]?.pause()
        }
    }

    //释放所有视频资源
    fun releaseAllVideos() {
        videoHolders.values.forEach { it.release() }
        videoHolders.clear()
    }
}
