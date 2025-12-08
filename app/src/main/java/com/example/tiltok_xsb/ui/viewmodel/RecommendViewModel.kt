package com.example.tiltok_xsb.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.data.repository.CommentRepository
import com.example.tiltok_xsb.data.repository.VideoRepository
import com.example.tiltok_xsb.utils.Resource
import kotlinx.coroutines.launch

class RecommendViewModel(application: Application): AndroidViewModel(application) {

    private val videoRepository = VideoRepository()
    private val commentRepository = CommentRepository(application)  // 添加评论仓库

    private val _videoList = MutableLiveData<Resource<List<VideoBean>>>()
    val videoList: LiveData<Resource<List<VideoBean>>> = _videoList

    private val _loadMoreResult = MutableLiveData<Resource<List<VideoBean>>>()
    val loadMoreResult: LiveData<Resource<List<VideoBean>>> = _loadMoreResult

    private val _likeResult = MutableLiveData<Pair<Int, Boolean>>() // <position, isLiked>
    val likeResult: LiveData<Pair<Int, Boolean>> = _likeResult

    private val _followResult = MutableLiveData<Pair<Int, Boolean>>() // <userId, isFollowed>
    val followResult: LiveData<Pair<Int, Boolean>> = _followResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentPage = 1
    private val pageSize = 20
    private val allVideos = mutableListOf<VideoBean>()

    // 加载推荐视频（首次加载或刷新）
    fun loadRecommendVideos(isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 1
            allVideos.clear()
        }

        viewModelScope.launch {
            _videoList.value = Resource.Loading()

            val result = videoRepository.getRecommendVideos(currentPage, pageSize)
            if (result.isSuccess) {
                val videos = result.getOrNull() ?: emptyList()

                // 同步评论数
                syncCommentCounts(videos)

                allVideos.addAll(videos)
                _videoList.value = Resource.Success(allVideos.toList())
                currentPage++
            } else {
                _videoList.value = Resource.Error(result.exceptionOrNull()?.message ?: "加载失败")
            }
        }
    }

    //加载更多
    fun loadMore() {
        viewModelScope.launch {
            _loadMoreResult.value = Resource.Loading()

            val result = videoRepository.getRecommendVideos(currentPage, pageSize)
            if (result.isSuccess) {
                val newVideos = result.getOrNull() ?: emptyList()

                if (newVideos.isNotEmpty()) {
                    // 同步评论数
                    syncCommentCounts(newVideos)

                    allVideos.addAll(newVideos)
                    _loadMoreResult.value = Resource.Success(newVideos)
                    currentPage++
                } else {
                    _loadMoreResult.value = Resource.Error("没有更多数据了")
                }
            } else {
                _errorMessage.value = "加载失败"
            }
        }
    }

    //点赞/取消点赞
    fun toggleLike(video:VideoBean,position: Int){
        viewModelScope.launch {
            val result = videoRepository.toggleLike(video)
            if (result.isSuccess) {

                video.isLiked = !video.isLiked
                if (video.isLiked) {
                    video.likeCount++
                } else {
                    video.likeCount--
                }
                _likeResult.value = Pair(position, video.isLiked)
            } else {
                _errorMessage.value = "操作失败"
            }
        }
    }

    // 同步视频列表的评论数
    private suspend fun syncCommentCounts(videos: List<VideoBean>) {
        try {
            val videoIds = videos.map { it.videoId }
            val commentCounts = commentRepository.getCommentCountsForVideos(videoIds)

            videos.forEach { video ->
                video.commentCount = commentCounts[video.videoId] ?: 0
            }

            android.util.Log.d(
                "RecommendViewModel",
                "✅ 同步评论数完成: ${videos.map { "${it.videoId}=${it.commentCount}" }}"
            )
        } catch (e: Exception) {
            android.util.Log.e("RecommendViewModel", "❌ 同步评论数失败: ${e.message}")
        }
    }


    //获取当前视频列表（用于跳转播放页）
    fun getCurrentVideoList(): ArrayList<VideoBean> {
        return ArrayList(allVideos)
    }
}