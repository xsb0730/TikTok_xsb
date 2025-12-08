package com.example.tiltok_xsb.data.repository

import android.content.Context
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.data.database.AppDatabase
import com.example.tiltok_xsb.data.database.CommentDao
import com.example.tiltok_xsb.data.database.CommentEntity
import com.example.tiltok_xsb.data.model.CommentBean
import com.example.tiltok_xsb.data.model.UserBean
import com.google.gson.Gson
import kotlinx.coroutines.delay

class CommentRepository(context: Context) {

    private val commentDao: CommentDao = AppDatabase.getDatabase(context).commentDao()
    private val gson = Gson()

    // 获取评论列表（优先从数据库加载）
    suspend fun getCommentList(videoId: Int): Result<List<CommentBean>> {
        return try {
            delay(300)  // 模拟网络延迟

            // 从数据库读取
            val localComments = commentDao.getCommentsByVideoId(videoId)

            if (localComments.isNotEmpty()) {
                // 数据库有数据，直接返回
                val commentBeans = localComments.map { it.toCommentBean() }
                return Result.success(commentBeans)
            }

            // 数据库为空，为该视频生成专属模拟数据
            val mockComments = createMockCommentsForVideo(videoId)

            // 保存到数据库
            val entities = mockComments.map { it.toEntity() }
            commentDao.insertAll(entities)

            Result.success(mockComments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 发布评论（保存到数据库）
    suspend fun publishComment(videoId: Int, content: String): Result<CommentBean> {
        return try {
            delay(800)

            // 创建评论对象
            val currentUser = UserBean(
                userId = 9999,
                nickName = "我",
                headId = R.mipmap.default_avatar,
                sign = "我好看"
            )

            val newComment = CommentBean(
                commentId = System.currentTimeMillis().toInt(),
                videoId = videoId,
                userBean = currentUser,
                content = content,
                likeCount = 0,
                isLiked = false,
                createTime = System.currentTimeMillis()
            )

            // 保存到数据库
            commentDao.insert(newComment.toEntity())

            Result.success(newComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 切换点赞状态（同步到数据库）
    suspend fun toggleCommentLike(comment: CommentBean): Result<Boolean> {
        return try {
            delay(200)

            val newLikedState = !comment.isLiked
            val newLikeCount = if (newLikedState) {
                comment.likeCount + 1
            } else {
                comment.likeCount - 1
            }

            // 更新数据库
            commentDao.updateLikeStatus(
                commentId = comment.commentId,
                isLiked = newLikedState,
                likeCount = newLikeCount
            )

            Result.success(newLikedState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 获取评论数量
    suspend fun getCommentCount(videoId: Int): Int {
        return commentDao.getCommentCount(videoId)
    }

    // 新增：批量获取多个视频的评论数
    suspend fun getCommentCountsForVideos(videoIds: List<Int>): Map<Int, Int> {
        return try {
            val result = mutableMapOf<Int, Int>()

            videoIds.forEach { videoId ->
                val count = getCommentCount(videoId)
                result[videoId] = count
            }

            result
        } catch (e: Exception) {
            emptyMap()
        }
    }


    // CommentBean → CommentEntity
    private fun CommentBean.toEntity(): CommentEntity {
        return CommentEntity(
            commentId = this.commentId,
            videoId = this.videoId,
            userJson = gson.toJson(this.userBean),
            content = this.content,
            likeCount = this.likeCount,
            isLiked = this.isLiked,
            createTime = this.createTime
        )
    }

    // CommentEntity → CommentBean
    private fun CommentEntity.toCommentBean(): CommentBean {
        return CommentBean(
            commentId = this.commentId,
            videoId = this.videoId,
            userBean = gson.fromJson(this.userJson, UserBean::class.java),
            content = this.content,
            likeCount = this.likeCount,
            isLiked = this.isLiked,
            createTime = this.createTime
        )
    }

    // 为每个视频生成专属的模拟评论
    private fun createMockCommentsForVideo(videoId: Int): List<CommentBean> {
        // 模拟用户池
        val userPool = listOf(
            UserBean(
                userId = 1001,
                nickName = "用户12138",
                headId = R.mipmap.head_one,
                sign = "这个人很懒，什么都没留下"
            ),
            UserBean(
                userId = 1002,
                nickName = "小红",
                headId = R.mipmap.head_two,
                sign = "热爱生活"
            ),
            UserBean(
                userId = 1003,
                nickName = "阿明",
                headId = R.mipmap.head_three
            ),
            UserBean(
                userId = 1004,
                nickName = "小美",
                headId = R.mipmap.head_four
            ),
            UserBean(
                userId = 1005,
                nickName = "老王",
                headId = R.mipmap.head_five
            ),
            UserBean(
                userId = 1006,
                nickName = "张三",
                headId = R.mipmap.head_one
            ),
            UserBean(
                userId = 1007,
                nickName = "李四",
                headId = R.mipmap.head_two
            )
        )

        // 评论内容池
        val commentPool = listOf(
            "这个也太好看了吧",
            "太有趣了哈哈哈",
            "学到了学到了",
            "已经循环看了10遍了",
            "这个bgm是什么",
            "笑死我了",
            "拍得真好",
            "这也太真实了",
            "厉害厉害",
            "有内味了",
            "666666",
            "太棒了",
            "好喜欢这个视频",
            "每天都来看一遍",
            "转发给朋友了"
        )

        // 根据 videoId 生成不同数量和内容的评论
        val random = java.util.Random(videoId.toLong())  // 使用 videoId 作为随机种子，确保相同视频总是生成相同评论
        val commentCount = random.nextInt(8) + 3  // 每个视频 3-10 条评论

        return (1..commentCount).map { index ->
            val user = userPool[random.nextInt(userPool.size)]
            val content = commentPool[random.nextInt(commentPool.size)]
            val likeCount = random.nextInt(500)

            CommentBean(
                commentId = videoId * 1000 + index,  // 确保每个视频的评论 ID 不重复
                videoId = videoId,
                userBean = user.copy(),  // 复制用户对象，避免共享引用
                content = content,
                likeCount = likeCount,
                isLiked = false,
                createTime = System.currentTimeMillis() - (commentCount - index) * 3600000L  // 按时间倒序
            )
        }
    }
}