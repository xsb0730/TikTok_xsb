package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tiltok_xsb.databinding.FragmentVideoLikeBinding
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.ui.adapter.VideoGridAdapter

class VideoListFragment : Fragment() {

    private var _binding: FragmentVideoLikeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: VideoGridAdapter
    private val videoList = mutableListOf<VideoBean>()

    companion object {
        private const val ARG_TYPE = "type"
        private const val TYPE_WORKS = 0      // 作品
        private const val TYPE_RECOMMEND = 1  // 推荐
        private const val TYPE_COLLECT = 2    // 收藏
        private const val TYPE_LIKE = 3       // 喜欢

        fun newInstance(type: Int): VideoListFragment {
            return VideoListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TYPE, type)
                }
            }
        }
    }

    private val type by lazy { arguments?.getInt(ARG_TYPE) ?: TYPE_WORKS }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoLikeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        // ✅ 设置三列瀑布流布局
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = layoutManager

        // ✅ 初始化适配器
        adapter = VideoGridAdapter(
            videoList,
            showLock = type == TYPE_LIKE  // 喜欢列表显示锁标识
        ) { video, position ->
            // 点击视频项
            openVideoPlay(video, position)
        }

        binding.recyclerView.adapter = adapter

        // ✅ 防止瀑布流跳动
        binding.recyclerView.itemAnimator = null
    }

    private fun loadData() {
        // TODO: 根据 type 加载不同的数据
        // 这里先用模拟数据
        val mockData = createMockData()
        videoList.clear()
        videoList.addAll(mockData)
        adapter.notifyDataSetChanged()

        // 显示/隐藏空状态
        binding.tvEmpty.visibility = if (videoList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun createMockData(): List<VideoBean> {
        // 模拟数据（不同高度模拟瀑布流效果）
        return List(20) { index ->
            VideoBean(
                videoId = "video_$index",
                coverRes = com.example.tiltok_xsb.R.drawable.default_cover,
                videoRes = "android.resource://${requireContext().packageName}/raw/video_$index",
                likeCount = (1000..100000).random(),
                commentCount = (100..10000).random(),
                content = "视频标题 $index"
            )
        }
    }

    private fun openVideoPlay(video: VideoBean, position: Int) {
        android.util.Log.d("VideoListFragment", "点击视频: ${video.videoId}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}