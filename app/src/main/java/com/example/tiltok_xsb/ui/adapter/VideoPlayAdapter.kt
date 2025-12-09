package com.example.tiltok_xsb.ui.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.example.tiltok_xsb.databinding.ItemVideoPlayBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.ui.viewmodel.VideoPlayViewModel



class VideoPlayAdapter(
    private val videoList:List<VideoBean>,
    private val viewModel: VideoPlayViewModel,
    private val onCommentClick: ((VideoBean, Int) -> Unit)? = null,
    private val onCoverUpdate: ((Int, String?) -> Unit)? = null
):RecyclerView.Adapter<VideoPlayAdapter.VideoViewHolder>() {

    //当前正在播放的位置
    private var currentPlayingPosition = -1
    //缓存 ViewHolder
    private val videoHolders = mutableMapOf<Int, VideoViewHolder>()

    //缓存列表项布局中的所有控件，绑定控件的点击、状态更新等逻辑，将视频数据绑定到布局控件上实现列表项的渲染
    inner class VideoViewHolder(val binding: ItemVideoPlayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var exoPlayer: ExoPlayer? = null
        private var recordAnimator: ObjectAnimator? = null
        private lateinit var currentVideo: VideoBean
        private var isPlayerReady = false
        private var currentPosition: Int = -1
        // 标记是否应该播放（只有调用 play() 后才为 true）
        private var shouldPlay = false

        //将视频数据绑定到ViewHolder的视图上
        @SuppressLint("SetTextI18n")
        fun bind(video: VideoBean, position: Int) {

            currentVideo = video
            currentPosition = position      // 保存传入的位置
            shouldPlay = false              // 重置标志
            isPlayerReady = false           // 重置准备状态

            with(binding) {
                //加载头像
                loadAvatar(video)

                //加载唱片封面
                Glide.with(ivHeadAnim)
                    .load(video.userBean?.headId ?: R.drawable.default_avatar)
                    .apply(RequestOptions().circleCrop())
                    .into(ivHeadAnim)

                //设置文字信息
                val nickname = video.userBean?.nickName
                    ?: root.context.getString(R.string.default_user_name)
                tvNickname.text = root.context.getString(R.string.user_nickname_format, nickname)
                tvTitle.text = video.content ?: ""

                tvMarquee.text = "@$nickname 创作的原声 - $nickname"       //动态设置走马灯文本

                updateUIState(video)                                     //设置统计数据

                //设置关注状态
                ivFollow.visibility =
                    if (video.userBean?.isFollowed == true) View.GONE else View.VISIBLE

                setupClickListeners(video, position)        //设置点击监听
                setupExoPlayer(video)                       // 设置 ExoPlayer视频播放器
                setupLikeAnimationView(video, position)     // 设置双击点赞动画监听
            }

            //保存ViewHolder
            videoHolders[position] = this
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
                ivLike.text = ivLike.context.getString(R.string.icon_like_fill)
                ivLike.setTextColor(
                    if (video.isLiked) {
                        ivLike.context.getColor(R.color.red)      // 已点赞：红色
                    } else {
                        ivLike.context.getColor(R.color.white)    // 未点赞：白色
                    }
                )

                //收藏状态
                ivCollect.text = ivCollect.context.getString(R.string.icon_collect)
                ivCollect.setTextColor(
                    if (video.isCollected) {
                        ivCollect.context.getColor(R.color.yellow)  // 已收藏：黄色
                    } else {
                        ivCollect.context.getColor(R.color.white)   // 未收藏：白色
                    }
                )

                // 数字
                tvLikecount.text = formatCount(video.likeCount)
                tvCommentcount.text = formatCount(video.commentCount)
                tvCollectcount.text = formatCount(video.collectCount)
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
                    onCommentClick?.invoke(video, position)
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
                    Toast.makeText(it.context, "进入用户主页功能待实现", Toast.LENGTH_SHORT).show()
                }

                // 关注
                ivFollow.setOnClickListener {
                    viewModel.followUser(video, position)
                }

                // 唱片点击
                rlRecord.setOnClickListener {
                    togglePlayPause()
                }

            }
        }

        // 设置双击点赞动画视图的监听
        private fun setupLikeAnimationView(video: VideoBean, position: Int) {
            with(binding.likeAnimationView) {
                // 双击点赞
                setOnLikeListener {
                    if (!video.isLiked) {
                        viewModel.toggleLike(video, position)
                    }
                }

                // 单击播放/暂停
                setOnPlayPauseListener {
                    togglePlayPause()
                }
            }
        }

        // 设置 ExoPlayer
        @OptIn(UnstableApi::class)  //使用了不稳定API
        private fun setupExoPlayer(video: VideoBean) {

            // 检查 videoRes 是否为空
            if (video.videoRes.isEmpty()) {
                Toast.makeText(binding.root.context, "视频路径为空", Toast.LENGTH_SHORT).show()
                return
            }

            try {
                // 创建 ExoPlayer 实例
                exoPlayer = ExoPlayer.Builder(binding.root.context)
                    .build()
                    .apply {
                        repeatMode = Player.REPEAT_MODE_ONE  // 单曲循环

                        // 绑定到 PlayerView
                        binding.playerView.player = this

                        // 设置播放监听，当视频准备就绪时，触发回调
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                when (playbackState) {
                                    Player.STATE_BUFFERING -> {
                                        binding.progressBar.visibility = View.VISIBLE
                                    }

                                    Player.STATE_READY -> {
                                        binding.progressBar.visibility = View.GONE

                                        if (!isPlayerReady) {
                                            isPlayerReady = true

                                            // 如果需要播放，立即开始
                                            if (shouldPlay && !isPlaying) {
                                                play()
                                            }
                                        }
                                    }

                                    Player.STATE_ENDED -> {
                                        android.util.Log.d(
                                            "VideoPlayAdapter",
                                            "[pos=$currentPosition] 播放结束"
                                        )
                                    }

                                    Player.STATE_IDLE -> {
                                        android.util.Log.d(
                                            "VideoPlayAdapter",
                                            "[pos=$currentPosition] 播放器空闲"
                                        )
                                    }
                                }
                            }

                            // 渲染第一帧时通知 Activity 隐藏封面
                            override fun onRenderedFirstFrame() {
                                if (shouldPlay) {
                                    // 通知 Activity 隐藏封面（如果是首次进入）
                                    onCoverUpdate?.invoke(currentPosition.toInt(), null)
                                }
                            }

                            //播放状态变化
                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                if (isPlaying) {
                                    startRecordAnimation()
                                    hidePauseIcon()
                                } else {
                                    pauseRecordAnimation()
                                    showPauseIcon()
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    binding.root.context,
                                    "视频加载失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })

                        // 设置视频源
                        val uri = android.net.Uri.parse(video.videoRes)
                        val mediaItem = MediaItem.fromUri(uri)

                        val dataSourceFactory = DefaultDataSource.Factory(binding.root.context)
                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)
                        setMediaSource(mediaSource)
                        prepare()
                        playWhenReady = false  // 默认不自动播放
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(binding.root.context, "播放器初始化失败", Toast.LENGTH_SHORT).show()
            }
        }

        //切换播放/暂停
        private fun togglePlayPause() {
            exoPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }

        // 显示暂停图标
        private fun showPauseIcon() {
            binding.ivPause.visibility = View.VISIBLE
            //添加淡入动画
            binding.ivPause.alpha = 0f
            binding.ivPause.animate()
                .alpha(0.8f)
                .setDuration(200)
                .start()
        }

        // 隐藏暂停图标
        private fun hidePauseIcon() {
            //添加淡出动画
            binding.ivPause.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.ivPause.visibility = View.GONE
                }
                .start()
        }

        //播放
        fun play() {

            shouldPlay = true

            if (exoPlayer == null) {
                return
            }

            exoPlayer?.let { player ->
                when (player.playbackState) {
                    Player.STATE_IDLE -> {
                        player.prepare()
                    }

                    Player.STATE_ENDED -> {
                        player.seekTo(0)
                    }

                    Player.STATE_BUFFERING -> {

                    }

                    Player.STATE_READY -> {

                    }
                }

                player.play()
            }
        }

        //暂停
        fun pause() {
            // 清除播放标志
            shouldPlay = false
            exoPlayer?.pause()
        }

        //视频资源释放，播放状态重置
        fun release() {
            shouldPlay = false
            isPlayerReady = false

            exoPlayer?.stop()
            exoPlayer?.clearMediaItems()
            exoPlayer?.release()
            exoPlayer = null

            stopRecordAnimation()
            binding.ivPause.visibility = View.GONE
            binding.progressBar.visibility = View.GONE

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

        //更新评论数显示
        fun updateCommentCount(count: Int) {
            currentVideo.commentCount = count
            binding.tvCommentcount.text = formatCount(count)
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

    //更新指定位置视频的评论数
    fun updateCommentCount(position: Int, newCount: Int) {
        if (position in videoList.indices) {
            videoList[position].commentCount = newCount
            videoHolders[position]?.updateCommentCount(newCount)
        }
    }

    fun onPageSelected(position: Int) {

        // 通知 Activity 显示新封面
        val video = videoList.getOrNull(position)
        val coverRes = video?.coverRes?.let { if (it != 0) it.toString() else video.videoRes }
        onCoverUpdate?.invoke(position, coverRes)

        // 暂停之前的视频
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            videoHolders[currentPlayingPosition]?.pause()
        }

        currentPlayingPosition = position

        // 播放当前视频
        val holder = videoHolders[position]
        if (holder == null) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                videoHolders[position]?.play()
            }, 200)
        } else {
            holder.play()
        }
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
        currentPlayingPosition = -1  // 重置播放位置
    }
}
