package com.example.tiltok_xsb.data.repository

import android.content.Context
import android.net.Uri
import com.example.tiltok_xsb.data.model.UserInfo
import com.example.tiltok_xsb.utils.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class UserRepository(private val context: Context) {

    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(userId: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                delay(500) // 模拟网络延迟

                // 模拟数据
                val userInfo = UserInfo(
                    userId = "123456",
                    nickname = "药獸",
                    douyinId = "570610221xsb",
                    avatarUrl = "",
                    backgroundUrl = "",
                    signature = "为实现中华民族伟大复兴的中国梦而努力奋斗",
                    age = 24,
                    location = "重庆",
                    likesCount = 520,
                    followingCount = 13,
                    fansCount = 14,
                    isFollowing = false
                )

                Result.success(userInfo)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 上传头像
     */
    suspend fun uploadAvatar(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                delay(1000) // 模拟上传延迟

                // 模拟返回的头像 URL
                val avatarUrl = uri.toString()

                Result.success(avatarUrl)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}