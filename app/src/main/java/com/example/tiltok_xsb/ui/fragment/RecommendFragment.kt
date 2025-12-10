package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.util.Pair
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentRecommendBinding
import com.example.tiltok_xsb.databinding.ItemGridVideoBinding
import com.example.tiltok_xsb.ui.activity.VideoPlayActivity
import com.example.tiltok_xsb.ui.adapter.GridVideoAdapter
import com.example.tiltok_xsb.ui.viewmodel.RecommendViewModel
import com.example.tiltok_xsb.utils.Resource

class RecommendFragment : BaseBindingFragment<FragmentRecommendBinding>({FragmentRecommendBinding.inflate(it)}), IScrollToTop {
    //懒加载创建和获取 ViewModel 实例
    private val viewModel:RecommendViewModel by viewModels()
    //双列列表适配器
    private var adapter:GridVideoAdapter? = null
    //是否正在加载
    private var isLoading=false
    //是否是首次加载
    private var isFirstLoad = true
    // 标记是否还有更多数据
    private var hasMoreData = true
    // 上次触发加载的时间戳（防抖）
    private var lastLoadTime = 0L
    // 加载间隔（毫秒）
    private val loadInterval = 1000L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()      //设置 RecyclerView（双列瀑布流）
        setRefreshEvent()       //设置下拉刷新
        setupLoadMore()         //设置上拉加载更多
        observeViewModel()      //观察 ViewModel 数据变化

        viewModel.loadRecommendVideos(isRefresh = true)

    }

    //设置双列瀑布流布局
    private fun initRecyclerView(){

        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //初始化适配器并绑定数据
        adapter= GridVideoAdapter(
            context=requireContext(),
            onItemClick = { _, position, itemBinding->
                // 启动带共享元素的转场动画
                startVideoPlayWithTransition(position, itemBinding)
            },
            onAvatarClick = { _, _ ->
                // 点击头像跳转到作者页面
            },
            onLikeClick = { _, _ ->
                // 点赞
            }
        )

        //性能优化
        binding.recyclerView.adapter=adapter
        binding.recyclerView.setHasFixedSize(true)      //RecyclerView 大小固定
    }

    // 启动带共享元素转场的视频播放页面
    private fun startVideoPlayWithTransition(
        position: Int,
        itemBinding: ItemGridVideoBinding
    ) {
        // 获取完整视频列表
        val videoList = viewModel.getCurrentVideoList()

        // 创建共享元素配对
        val coverPair = Pair.create(
            itemBinding.ivCover as View,
            "video_cover_$position"
        )

        // 创建转场动画选项
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            coverPair
        )

        // 启动 Activity
        VideoPlayActivity.startWithTransition(
            requireContext(),
            videoList,           // 完整视频列表
            position,            // 当前点击的视频位置
            options.toBundle()   // 将动画配置传递给目标 Activity
        )
    }

    //下拉刷新
    private fun setRefreshEvent(){
        // 设置进度条颜色
        binding.refreshLayout.setColorSchemeResources(R.color.color_link)
        // 设置下拉刷新监听器
        binding.refreshLayout.setOnRefreshListener {
            isFirstLoad = false
            hasMoreData = true
            viewModel.loadRecommendVideos(isRefresh = true)
        }
    }

    //上拉加载更多
    private fun setupLoadMore(){
        binding.recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 只处理向上滚动
                if (dy <= 0) return

                // 防止重复触发
                if (isLoading || !hasMoreData) {
                    return
                }

                // 距离上次加载不足 1 秒则不触发(防抖）
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLoadTime < loadInterval) {
                    return
                }

                // 获取布局管理器
                val layoutManager=recyclerView.layoutManager as StaggeredGridLayoutManager

                //最后一个可见项的位置
                val lastVisibleItems=IntArray(2)
                layoutManager.findLastVisibleItemPositions(lastVisibleItems)
                val lastVisibleItem=lastVisibleItems.maxOrNull()?:0

                // 获取总项数
                val totalItemCount=layoutManager.itemCount

                //滚动到倒数第4个加载更多
                if(lastVisibleItem>=totalItemCount-4&&totalItemCount>0){
                    isLoading = true
                    lastLoadTime = currentTime
                    viewModel.loadMore()
                }
            }
        })
    }

    //观察视频列表
    private fun observeViewModel() {

        //下拉刷新
        viewModel.videoList.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 首次加载（不是下拉刷新触发的）
                }
                is Resource.Success -> {
                    binding.refreshLayout.isRefreshing = false
                    isLoading = false

                    resource.data?.let { videos ->
                        adapter?.clearList()                //  刷新时清空
                        adapter?.appendList(videos)         //  添加全部数据

                        if (!isFirstLoad) {
                            Toast.makeText(context, "刷新成功", Toast.LENGTH_SHORT).show()
                        }
                        isFirstLoad = false
                    }
                }
                is Resource.Error -> {
                    binding.refreshLayout.isRefreshing = false
                    isLoading = false
                    Toast.makeText(context, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 加载更多结果
        viewModel.loadMoreResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    resource.data?.let { newVideos ->
                        if (newVideos.isEmpty()) {
                            // 没有更多数据了
                            hasMoreData = false
                            Toast.makeText(context, "没有更多数据了", Toast.LENGTH_SHORT).show()
                        } else {
                            // 记录当前滚动位置
                            val layoutManager = binding.recyclerView.layoutManager as StaggeredGridLayoutManager

                            // 记录第一个可见项的位置和偏移量
                            val firstVisibleItems = IntArray(2)
                            layoutManager.findFirstVisibleItemPositions(firstVisibleItems)
                            val firstVisiblePosition = firstVisibleItems.minOrNull() ?: 0
                            val firstView = layoutManager.findViewByPosition(firstVisiblePosition)
                            val topOffset = firstView?.top ?: 0

                            // 添加新数据
                            adapter?.appendList(newVideos)

                            // 恢复滚动位置
                            binding.recyclerView.post {
                                layoutManager.scrollToPositionWithOffset(firstVisiblePosition, topOffset)
                            }
                        }
                    }

                    // 延迟重置 isLoading，确保数据渲染完成
                    binding.recyclerView.postDelayed({
                        isLoading = false
                    }, 300)
                }
                is Resource.Error -> {
                    isLoading = false  //  加载失败，恢复状态
                    Toast.makeText(context, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 观察点赞结果
        viewModel.likeResult.observe(viewLifecycleOwner) { (position, isLiked) ->
            adapter?.updateLikeStatus(position, isLiked)
            Toast.makeText(
                context,
                if (isLiked) "已点赞" else "取消点赞",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 观察关注结果
        viewModel.followResult.observe(viewLifecycleOwner) { (_, isFollowed) ->
            if (isFollowed) {
                Toast.makeText(context, "已关注", Toast.LENGTH_SHORT).show()
            }
        }

        // 观察错误信息
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    //滚动到顶部
    override fun scrollToTop() {
        // 添加生命周期检查
        if (!isAdded || isDetached) {
            return
        }

        binding.recyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }
}
