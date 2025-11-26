package com.example.tiltok_xsb.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.base.BaseBindingFragment
import com.example.tiltok_xsb.databinding.FragmentRecommendBinding
import com.example.tiltok_xsb.ui.activity.VideoPlayActivity
import com.example.tiltok_xsb.ui.adapter.GridVideoAdapter
import com.example.tiltok_xsb.ui.viewmodel.RecommendViewModel
import com.example.tiltok_xsb.utils.Resource

class RecommendFragment : BaseBindingFragment<FragmentRecommendBinding>({FragmentRecommendBinding.inflate(it)}), IScrollToTop {

    private val viewModel:RecommendViewModel by viewModels()            //by viewModels()确保屏幕旋转时数据不丢失，生命周期自动管理

    //双列列表适配器
    private var adapter:GridVideoAdapter? = null

    //是否正在加载
    private var isLoading=false
    //是否是首次加载
    private var isFirstLoad = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initRecyclerView()
        setRefreshEvent()
        setupLoadMore()
        observeViewModel()

        viewModel.loadRecommendVideos(isRefresh = true)

    }
    
    private fun initRecyclerView(){
        //设置双列瀑布流布局
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //初始化适配器并绑定数据
        adapter= GridVideoAdapter(
            context=requireContext(),
            onItemClick = {video,position->
                // 点击卡片跳转到全屏播放
                val videoList = viewModel.getCurrentVideoList()
                VideoPlayActivity.start(requireContext(), videoList, position)
            },
            onAvatarClick = { video, position ->
                // 点击头像跳转到作者页面
                Toast.makeText(context, "进入 ${video.userBean?.nickName} 的主页", Toast.LENGTH_SHORT).show()

            },
            onLikeClick = { video, position ->
                // 点赞
                viewModel.toggleLike(video, position)
            }
        )

        //性能优化
        binding.recyclerView.adapter=adapter
        binding.recyclerView.setHasFixedSize(true)
    }

    //下拉刷新
    private fun setRefreshEvent(){
        binding.refreshLayout.setColorSchemeResources(R.color.color_link)
        binding.refreshLayout.setOnRefreshListener {
            isFirstLoad = false
            viewModel.loadRecommendVideos(isRefresh = true)
        }
    }


    //上拉加载更多
    private fun setupLoadMore(){
        binding.recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if(isLoading) return

                val layoutManager=recyclerView.layoutManager as StaggeredGridLayoutManager

                //最后一个可见项的位置
                val lastVisibleItems=IntArray(2)
                layoutManager.findLastVisibleItemPositions(lastVisibleItems)
                val lastVisibleItem=lastVisibleItems.maxOrNull()?:0
                val totalItemCount=layoutManager.itemCount

                //滚动到倒数第三行加载更多
                if(lastVisibleItem>=totalItemCount-3&&totalItemCount>0){
                    isLoading = true
                    viewModel.loadMore()
                }
            }
        })
    }

    //观察视频列表
    private fun observeViewModel() {

        viewModel.videoList.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 首次加载显示刷新动画（如果不是下拉刷新触发的）
                    if (!binding.refreshLayout.isRefreshing) {
                        // 可以显示一个加载框
                    }
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

        viewModel.loadMoreResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // 可以显示底部加载指示器
                }
                is Resource.Success -> {
                    isLoading = false  //  加载完成，恢复状态

                    resource.data?.let { newVideos ->
                        adapter?.appendList(newVideos)  //  只追加新数据
                        Toast.makeText(context, "加载了 ${newVideos.size} 条数据", Toast.LENGTH_SHORT).show()
                    }
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
        viewModel.followResult.observe(viewLifecycleOwner) { (userId, isFollowed) ->
            if (isFollowed) {
                Toast.makeText(context, "已关注", Toast.LENGTH_SHORT).show()
            }
        }

        // 观察错误信息
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun scrollToTop() {
        binding.recyclerView.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }
}
