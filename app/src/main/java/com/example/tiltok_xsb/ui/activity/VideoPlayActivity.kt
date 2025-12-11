package com.example.tiltok_xsb.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.tiltok_xsb.base.BaseBindingActivity
import com.example.tiltok_xsb.databinding.ActivityVideoPlayBinding
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.ui.adapter.VideoPlayAdapter
import com.example.tiltok_xsb.ui.fragment.CommentDialog
import com.example.tiltok_xsb.ui.viewmodel.CommentViewModel
import com.example.tiltok_xsb.ui.viewmodel.VideoPlayViewModel
import com.example.tiltok_xsb.utils.FullScreenUtil
import com.example.tiltok_xsb.utils.Resource
import com.example.tiltok_xsb.utils.VideoPlayTouchHelper

class VideoPlayActivity:BaseBindingActivity<ActivityVideoPlayBinding>({ActivityVideoPlayBinding.inflate(it)}) {
    private val viewModel: VideoPlayViewModel by viewModels()
    private val commentViewModel: CommentViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[CommentViewModel::class.java]
    }
    private var videoPlayAdapter:VideoPlayAdapter?=null
    private var currentPosition:Int=0
    private val videoList = mutableListOf<VideoBean>()

    private var touchHelper: VideoPlayTouchHelper? = null
    private var isRefreshing = false
    private var isLoadingMore = false
    private var isFirstEnter = true

    companion object{
        private const val KEY_VIDEO_LIST="video_list"
        private const val KEY_POSITION="position"

        // 带转场动画的启动方法
        fun startWithTransition(
            context: Context,
            videoList: ArrayList<VideoBean>,
            position: Int,
            options: Bundle?
        ) {
            val intent = Intent(context, VideoPlayActivity::class.java).apply {
                putParcelableArrayListExtra(KEY_VIDEO_LIST, videoList)
                putExtra(KEY_POSITION, position)
            }

            // 带动画启动
            context.startActivity(intent, options)
        }
    }

    //初始化
    override fun init() {
        //设置全屏
        FullScreenUtil.setFullScreen(this)

        //获取传递的数据
        val receivedList = intent.getParcelableArrayListExtraCompat<VideoBean>(KEY_VIDEO_LIST)
        currentPosition=intent.getIntExtra(KEY_POSITION,0)
        receivedList?.let {
            videoList.clear()
            videoList.addAll(it)
        }

        supportPostponeEnterTransition()        // 延迟启动转场动画，等待目标 View 准备好
        setupViewPager()            // 设置 ViewPager2
        setupClickListeners()       // 设置点击事件
        setupTouchHelper()          // 设置触摸手势（下拉刷新/上拉加载）
        observeViewModel()          // 观察数据变化
        observeCommentViewModel()   // 观察评论数变化

    }

    //设置页面
    private fun setupViewPager() {
        videoPlayAdapter = VideoPlayAdapter(
            videoList,
            viewModel,
            onFirstFrameReady = {
                if (isFirstEnter) {
                    isFirstEnter = false
                    // 切回主线程启动动画
                    binding.root.post {
                        supportStartPostponedEnterTransition()
                    }
                }
            },
            onCommentClick = { video, position ->
                showCommentDialog(video, position)
            }
        )

        binding.viewPager.adapter = videoPlayAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager.offscreenPageLimit = 1

        //定位到点击的视频位置
        binding.viewPager.setCurrentItem(currentPosition, false)

        // 监听页面切换
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                videoPlayAdapter?.onPageSelected(position)
            }
        })
    }

    //设置返回按钮点击监听
    private fun setupClickListeners(){
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    //设置触摸监听（上下滑动）
    private fun setupTouchHelper() {
        touchHelper = VideoPlayTouchHelper(
            viewPager = binding.viewPager,
            refreshIcon = binding.ivRefreshIcon,

            //下拉进度更新
            onPullDown = { distance ->
                // 下拉刷新动画
                val progress = (distance / 200f).coerceIn(0f, 1f)

                // 容器透明度和位置
                binding.refreshContainer.alpha = progress
                binding.refreshContainer.translationY = distance * 0.5f

                // 视频区域同步向下移动
                binding.viewPager.translationY = distance
//                binding.ivGlobalCover.translationY = distance

                // 根据距离更新文字和颜色
                when {
                    distance >= 200f -> {
                        binding.tvRefreshHint.text = "松手即可刷新 ↓"
                        binding.tvRefreshHint.setTextColor(
                            android.graphics.Color.parseColor("#00FF00")
                        )
                    }
                    distance >= 100f -> {
                        binding.tvRefreshHint.text = "继续下拉 ↓"
                        binding.tvRefreshHint.setTextColor(android.graphics.Color.WHITE)
                    }
                    else -> {
                        binding.tvRefreshHint.text = "下拉刷新"
                        binding.tvRefreshHint.setTextColor(android.graphics.Color.WHITE)
                    }
                }
            },

            //触发刷新
            onRefresh = {
                if (!isRefreshing) {
                    isRefreshing = true

                    // 更新UI状态
                    binding.tvRefreshHint.text = "正在刷新..."
                    binding.tvRefreshHint.setTextColor(android.graphics.Color.WHITE)
                    binding.ivRefreshIcon.visibility = View.GONE
                    binding.pbRefreshLoading.visibility = View.VISIBLE

                    viewModel.refreshVideos()
                }
            },

            //触发加载更多
            onLoadMore = {
                if (!isLoadingMore && !isRefreshing) {
                    isLoadingMore = true
                    viewModel.loadMoreVideos()
                }
            }
        )
    }

    // 显示评论弹窗
    private fun showCommentDialog(video: VideoBean, position: Int) {
        // 暂停当前视频
        videoPlayAdapter?.pauseCurrentVideo()

        val commentDialog = CommentDialog(
            context = this,
            videoId = video.videoId,
            viewModelStoreOwner = this,
        )

        // 弹窗关闭时恢复视频播放
        commentDialog.setOnDismissListener {
            videoPlayAdapter?.resumeCurrentVideo()
        }

        commentDialog.show()
    }

    //观察事件
    private fun observeViewModel() {
        // 观察刷新结果
        viewModel.refreshResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                }

                is Resource.Success -> {
                    isRefreshing = false

                    // 停止图标旋转动画
                    touchHelper?.stopRefreshAnimation()

                    // 动画收起刷新提示
                    binding.refreshContainer.animate()
                        .alpha(0f)
                        .translationY(0f)
                        .setDuration(300)
                        .withEndAction {
                            binding.ivRefreshIcon.visibility = View.VISIBLE
                            binding.pbRefreshLoading.visibility = View.GONE
                            binding.tvRefreshHint.text = "下拉刷新"
                            binding.tvRefreshHint.setTextColor(android.graphics.Color.WHITE)
                        }
                        .start()

                    // 视频区域同步回弹
                    binding.viewPager.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .start()

                    resource.data?.let { newVideos ->

                        // 保存当前位置
                        val currentPos = currentPosition

                        // 暂停当前视频
                        videoPlayAdapter?.pauseCurrentVideo()

                        // 延迟更新数据（避免闪烁）
                        binding.viewPager.postDelayed({
                            // 更新数据
                            videoList.clear()
                            videoList.addAll(newVideos)

                            // 释放旧资源
                            videoPlayAdapter?.releaseAllVideos()

                            // 通知适配器
                            videoPlayAdapter?.notifyItemRangeChanged(0, newVideos.size)

                            // 确定安全位置
                            val safePosition = if (currentPos < videoList.size) currentPos else 0
                            currentPosition = safePosition

                            // 刷新后不显示封面（因为不是首次进入）
                            // 直接播放视频
                            binding.viewPager.postDelayed({
                                if (binding.viewPager.currentItem != safePosition) {
                                    binding.viewPager.setCurrentItem(safePosition, false)
                                }

                                binding.viewPager.postDelayed({
                                    videoPlayAdapter?.onPageSelected(safePosition)
                                }, 200)
                            }, 100)
                        }, 100)

                        Toast.makeText(this, "刷新成功", Toast.LENGTH_SHORT).show()
                    }
                }

                is Resource.Error -> {
                    isRefreshing = false

                    // 停止动画并快速收起
                    touchHelper?.stopRefreshAnimation()

                    binding.refreshContainer.animate()
                        .alpha(0f)
                        .translationY(0f)
                        .setDuration(200)
                        .withEndAction {
                            binding.ivRefreshIcon.visibility = View.VISIBLE
                            binding.pbRefreshLoading.visibility = View.GONE
                            binding.tvRefreshHint.text = "下拉刷新"
                            binding.tvRefreshHint.setTextColor(android.graphics.Color.WHITE)
                        }
                        .start()

                    // 视频区域同步快速回弹
                    binding.viewPager.animate()
                        .translationY(0f)
                        .setDuration(200)
                        .start()

                    Toast.makeText(this, resource.message ?: "刷新失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 观察加载更多结果
        viewModel.loadMoreResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    isLoadingMore = false

                    resource.data?.let { newVideos ->
                        if (newVideos.isNotEmpty()) {
                            val startPosition = videoList.size
                            videoList.addAll(newVideos)  // 追加到末尾
                            videoPlayAdapter?.notifyItemRangeInserted(startPosition, newVideos.size)

                            // 重置加载状态，允许再次触发
                            touchHelper?.resetLoadMoreState()
                        } else {

                            Toast.makeText(this, "没有更多数据了", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                is Resource.Error -> {
                    isLoadingMore = false

                    // 失败后也重置状态，允许重试
                    touchHelper?.resetLoadMoreState()

                    Toast.makeText(this, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 观察点赞结果
        viewModel.likeResult.observe(this) { (position, isLiked) ->
            videoPlayAdapter?.updateLikeStatus(position, isLiked)
        }

        // 观察收藏结果
        viewModel.collectResult.observe(this) { (position, isCollected) ->
            videoPlayAdapter?.updateCollectStatus(position, isCollected)
        }

        // 观察关注结果
        viewModel.followResult.observe(this) { (position, isFollowed) ->
            videoPlayAdapter?.updateFollowStatus(position, isFollowed)
        }

        // 观察错误信息
        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // 观察成功信息
        viewModel.successMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 观察评论数变化
    private fun observeCommentViewModel() {
        commentViewModel.commentCountUpdate.observe(this) { (videoId, newCount) ->
            // 查找对应的视频位置
            val position = videoList.indexOfFirst { it.videoId == videoId }

            if (position != -1) {
                // 更新 VideoBean 中的评论数
                videoList[position].commentCount = newCount
                // 更新 Adapter 中的显示
                videoPlayAdapter?.updateCommentCount(position, newCount)
            }
        }
    }

    //恢复
    override fun onResume() {
        super.onResume()

        //恢复视频播放
        videoPlayAdapter?.resumeCurrentVideo()
    }

    //暂停
    override fun onPause() {
        super.onPause()

        //暂停当前视频播放
        videoPlayAdapter?.pauseCurrentVideo()
    }

    // 拦截触摸事件
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 先让 touchHelper 尝试处理
        val handled = touchHelper?.onTouchEvent(ev) ?: false

        // 如果 touchHelper 拦截了事件，返回 true
        if (handled) {
            return true
        }

        // 否则交给父类处理（让 ViewPager2 正常工作）
        return super.dispatchTouchEvent(ev)
    }

    //释放
    override fun onDestroy() {
        super.onDestroy()
        touchHelper?.release()
        touchHelper = null
        //释放资源
        videoPlayAdapter?.releaseAllVideos()
    }
}