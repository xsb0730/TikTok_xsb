package com.example.tiltok_xsb.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.tiltok_xsb.base.BaseBindingActivity
import com.example.tiltok_xsb.databinding.ActivityVideoPlayBinding
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.ui.adapter.VideoPlayAdapter
import com.example.tiltok_xsb.ui.view.CommentDialog
import com.example.tiltok_xsb.ui.viewmodel.VideoPlayViewModel
import com.example.tiltok_xsb.utils.FullScreenUtil

class VideoPlayActivity:BaseBindingActivity<ActivityVideoPlayBinding>({ActivityVideoPlayBinding.inflate(it)}) {
    private val viewModel: VideoPlayViewModel by viewModels()
    private var videoPlayAdapter:VideoPlayAdapter?=null
    private var currentPosition:Int=0
    private var videoList:ArrayList<VideoBean>?=null

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
            context.startActivity(intent, options)
        }

        // 原有的启动方法(不带转场）
        fun start(context: Context, videoList: ArrayList<VideoBean>, position: Int) {
            startWithTransition(context, videoList, position, null)
        }
    }

    //初始化
    override fun init() {
        //设置全屏
        FullScreenUtil.setFullScreen(this)

        //获取传递的数据
        videoList=intent.getParcelableArrayListExtraCompat<VideoBean>(KEY_VIDEO_LIST)
        currentPosition=intent.getIntExtra(KEY_POSITION,0)

        setupViewPager()
        setupClickListeners()
        observeViewModel()

        // 延迟转场动画，等待 View 准备好
        supportPostponeEnterTransition()
    }

    //设置页面
    private fun setupViewPager(){
        videoList?.let{list->
            videoPlayAdapter= VideoPlayAdapter(
                list,
                viewModel,
                onCommentClick = { video, position ->
                    showCommentDialog(video, position)
                }
            )


            binding.viewPager.adapter=videoPlayAdapter
            binding.viewPager.orientation=ViewPager2.ORIENTATION_VERTICAL
            binding.viewPager.offscreenPageLimit=1

            //设置当前位置
            binding.viewPager.setCurrentItem(currentPosition, false)

            // 等待第一帧渲染完成后启动转场动画
            binding.viewPager.post {
                videoPlayAdapter?.onPageSelected(currentPosition)

                // 设置共享元素的 transitionName（与列表页保持一致）
                val firstItemView = getViewPagerItemAt(currentPosition)
                firstItemView?.let { view ->
                    ViewCompat.setTransitionName(view, "video_cover_$currentPosition")
                }

                // 启动转场动画
                supportStartPostponedEnterTransition()
            }

            //监听页面切换
            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition=position

                    //暂停上一个视频，播放当前视频
                    videoPlayAdapter?.onPageSelected(position)
                }
            })
        }
    }

    //设置点击监听
    private fun setupClickListeners(){
        //返回按钮
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    // 获取 ViewPager2 中指定位置的 View
    private fun getViewPagerItemAt(position: Int): View? {
        val recyclerView = binding.viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
        val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
        return viewHolder?.itemView?.findViewById(com.example.tiltok_xsb.R.id.iv_cover)
    }


    //观察事件
    private fun observeViewModel() {
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

    // 显示评论弹窗
    private fun showCommentDialog(video: VideoBean, position: Int) {
        // 暂停当前视频
        videoPlayAdapter?.pauseCurrentVideo()

        val commentDialog = CommentDialog(
            context = this,
            videoId = video.videoId,
            viewModelStoreOwner = this,

            // 传入评论数变化的回调
            onCommentCountChanged = { newCount ->

                // 更新 VideoBean 中的评论数
                videoList?.getOrNull(position)?.let {
                    it.commentCount = newCount
                }

                // 更新 Adapter 中的显示
                videoPlayAdapter?.updateCommentCount(position, newCount)
            }
        )

        // 弹窗关闭时恢复视频播放
        commentDialog.setOnDismissListener {
            videoPlayAdapter?.resumeCurrentVideo()
        }
        commentDialog.show()
    }

    //释放
    override fun onDestroy() {
        super.onDestroy()

        //释放资源
        videoPlayAdapter?.releaseAllVideos()
    }
}