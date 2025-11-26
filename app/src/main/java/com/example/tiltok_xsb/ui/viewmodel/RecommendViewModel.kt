package com.example.tiltok_xsb.ui.viewmodel

import androidx.compose.material3.TabPosition
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiltok_xsb.data.model.VideoBean
import com.example.tiltok_xsb.data.repository.VideoRepository
import com.example.tiltok_xsb.utils.Resource
import kotlinx.coroutines.launch

class RecommendViewModel(private val repository: VideoRepository= VideoRepository()):ViewModel() {


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

    //加载推荐视频（首次加载或刷新）
    fun loadRecommendVideos(isRefresh:Boolean=false){
        if(isRefresh){
            currentPage=1
            allVideos.clear()
        }

        viewModelScope.launch {
            _videoList.value=Resource.Loading()

            val result=repository.getRecommendVideos(currentPage,pageSize)
            if(result.isSuccess){
                val videos=result.getOrNull()?: emptyList()
                allVideos.addAll(videos)
                _videoList.value=Resource.Success(allVideos.toList())
                currentPage++

            }else{
                _videoList.value=Resource.Error(result.exceptionOrNull()?.message?:"加载失败")
            }
        }
    }

    //加载更多
    fun loadMore(){
        viewModelScope.launch {
            _loadMoreResult.value = Resource.Loading()

            val result=repository.getRecommendVideos(currentPage,pageSize)
            if(result.isSuccess){
                val newVideos = result.getOrNull() ?: emptyList()

                if (newVideos.isNotEmpty()) {
                    allVideos.addAll(newVideos)
                    _loadMoreResult.value = Resource.Success(newVideos) // 只返回新数据
                    currentPage++
                } else {
                    _loadMoreResult.value = Resource.Error("没有更多数据了")
                }

            }else{
                _errorMessage.value="加载失败"
            }
        }
    }

    //点赞/取消点赞
    fun toggleLike(video:VideoBean,position: Int){
        viewModelScope.launch {
            val result = repository.toggleLike(video)
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

    //关注用户
    fun followUser(userId: Int, position: Int) {
        viewModelScope.launch {
            val result = repository.followUser(userId)
            if (result.isSuccess) {
                allVideos.getOrNull(position)?.userBean?.isFollowed = true
                _followResult.value = Pair(userId, true)
            } else {
                _errorMessage.value = "关注失败"
            }
        }
    }

    //获取当前视频列表（用于跳转播放页）
    fun getCurrentVideoList(): ArrayList<VideoBean> {
        return ArrayList(allVideos)
    }
}