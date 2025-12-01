package com.example.tiltok_xsb.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiltok_xsb.data.model.CommentBean
import com.example.tiltok_xsb.data.repository.CommentRepository
import com.example.tiltok_xsb.utils.Resource
import kotlinx.coroutines.launch

class CommentViewModel(
    private val repository: CommentRepository=CommentRepository()
) :ViewModel(){

    private val _commentList = MutableLiveData<Resource<List<CommentBean>>>()
    val commentList: LiveData<Resource<List<CommentBean>>> = _commentList

    private val _publishResult = MutableLiveData<Resource<CommentBean>>()
    val publishResult: LiveData<Resource<CommentBean>> = _publishResult

    private val _commentCount = MutableLiveData<Int>()
    val commentCount: LiveData<Int> = _commentCount

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentVideoId: Int = 0

    // 加载评论
    fun loadComments(videoId: Int) {
        currentVideoId = videoId
        android.util.Log.d("CommentViewModel", "开始加载评论，videoId: $videoId")

        viewModelScope.launch {
            _commentList.value = Resource.Loading()

            val result = repository.getCommentList(videoId)
            android.util.Log.d("CommentViewModel", "加载结果: ${result.isSuccess}")

            if (result.isSuccess) {
                val comments = result.getOrNull() ?: emptyList()
                android.util.Log.d("CommentViewModel", "评论数量: ${comments.size}")

                _commentList.value = Resource.Success(comments)
                _commentCount.value = comments.size
            } else {
                android.util.Log.e("CommentViewModel", "加载失败: ${result.exceptionOrNull()}")

                _commentList.value = Resource.Error("加载评论失败")
                _errorMessage.value = "加载评论失败"
            }
        }
    }

    // 发表评论
    fun publishComment(content: String) {
        if (content.trim().isEmpty()) {
            _errorMessage.value = "评论内容不能为空"
            return
        }

        viewModelScope.launch {
            _publishResult.value = Resource.Loading()

            val result = repository.publishComment(currentVideoId, content.trim())

            if (result.isSuccess) {
                val newComment = result.getOrNull()
                if (newComment != null) {
                    val currentList = _commentList.value?.data?.toMutableList() ?: mutableListOf()
                    currentList.add(0, newComment)

                    _commentList.value = Resource.Success(currentList)
                    _commentCount.value = currentList.size
                    _publishResult.value = Resource.Success(newComment)
                }
            } else {
                _publishResult.value = Resource.Error("发布失败")
                _errorMessage.value = "发布评论失败"
            }
        }
    }


    // 给评论点赞
    fun toggleCommentLike(comment: CommentBean, position: Int) {
        viewModelScope.launch {
            val result = repository.toggleCommentLike(comment)

            if (result.isSuccess) {
                comment.isLiked = result.getOrNull() ?: false

                // 点赞数同步更新
                if (comment.isLiked) {
                    comment.likeCount++
                } else {
                    comment.likeCount--
                }

                val currentList = _commentList.value?.data?.toMutableList() ?: return@launch
                currentList[position] = comment
                _commentList.value = Resource.Success(currentList)
            } else {
                _errorMessage.value = "操作失败"
            }
        }
    }

}