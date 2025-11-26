package com.example.tiltok_xsb.ui.activity

import android.content.Context
import android.content.Intent
import androidx.viewpager2.widget.ViewPager2
import com.example.tiltok_xsb.base.BaseBindingActivity
import com.example.tiltok_xsb.databinding.ActivityVideoPlayBinding
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.ui.adapter.VideoPlayAdapter
import com.example.tiltok_xsb.utils.FullScreenUtil

class VideoPlayActivity:BaseBindingActivity<ActivityVideoPlayBinding>({ActivityVideoPlayBinding.inflate(it)}) {
    private var videoPlayAdapter:VideoPlayAdapter?=null
    private var currentPosition:Int=0
    private var videoList:ArrayList<VideoBean>?=null

    companion object{
        private const val KEY_VIDEO_LIST="video_list"
        private const val KEY_POSITION="position"

        //启动视频播放页面
        fun start(context:Context, videoList: ArrayList<VideoBean>, position:Int){
            val intent= Intent(context,VideoPlayActivity::class.java).apply{
                putParcelableArrayListExtra(KEY_VIDEO_LIST,videoList)
                putExtra(KEY_POSITION,position)
            }
            context.startActivity(intent)
        }
    }

    override fun init() {
        //设置全屏
        FullScreenUtil.setFullScreen(this)

        //获取传递的数据
        videoList=intent.getParcelableArrayListExtra(KEY_VIDEO_LIST)
        currentPosition=intent.getIntExtra(KEY_POSITION,0)

        setupViewPager()
        setupClickListeners()

    }

    private fun setupViewPager(){
        videoList?.let{list->
            videoPlayAdapter= VideoPlayAdapter(list)
            binding.viewPager.adapter=videoPlayAdapter
            binding.viewPager.orientation=ViewPager2.ORIENTATION_VERTICAL
            binding.viewPager.offscreenPageLimit=1

            //设置当前位置
            binding.viewPager.post{
                binding.viewPager.setCurrentItem(currentPosition,false)
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

    private fun setupClickListeners(){
        //返回按钮
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        //恢复视频播放
        videoPlayAdapter?.resumeCurrentVideo()
    }

    override fun onPause() {
        super.onPause()

        //暂停当前视频播放
        videoPlayAdapter?.pauseCurrentVideo()
    }

    override fun onDestroy() {
        super.onDestroy()

        //释放资源
        videoPlayAdapter?.releaseAllVideos()
    }
}