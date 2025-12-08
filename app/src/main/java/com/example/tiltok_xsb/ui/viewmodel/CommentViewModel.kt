package com.example.tiltok_xsb.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tiltok_xsb.data.model.CommentBean
import com.example.tiltok_xsb.data.repository.CommentRepository
import com.example.tiltok_xsb.utils.Resource
import kotlinx.coroutines.launch

class CommentViewModel(
    application: Application
) : AndroidViewModel(application) {

    // 传入 Context
    private val repository: CommentRepository = CommentRepository(application)

    private val _commentList = MutableLiveData<Resource<List<CommentBean>>>()
    val commentList: LiveData<Resource<List<CommentBean>>> = _commentList

    private val _publishResult = MutableLiveData<Resource<CommentBean>>()
    val publishResult: LiveData<Resource<CommentBean>> = _publishResult

    private val _commentCount = MutableLiveData<Int>()
    val commentCount: LiveData<Int> = _commentCount

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentVideoId: Int = 0

    // 加载评论（从数据库）
    fun loadComments(videoId: Int) {
        currentVideoId = videoId

        viewModelScope.launch {
            _commentList.value = Resource.Loading()

            val result = repository.getCommentList(videoId)

            if (result.isSuccess) {
                val comments = result.getOrNull() ?: emptyList()
                _commentList.value = Resource.Success(comments)
                _commentCount.value = comments.size
            } else {
                _commentList.value = Resource.Error("加载评论失败")
                _errorMessage.value = "加载评论失败"
            }
        }
    }

    // 发表评论（保存到数据库）
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

                    // 将新评论插入到列表头部
                    currentList.add(0, newComment)

                    // 更新评论列表
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

    // 给评论点赞（同步到数据库）
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