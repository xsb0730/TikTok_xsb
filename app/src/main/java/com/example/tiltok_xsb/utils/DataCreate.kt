package com.example.tiltok_xsb.utils

import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.data.model.UserBean
import com.example.tiltok_xsb.data.model.VideoBean
import java.util.ArrayList


class DataCreate {
    init {
        val videoBean1= VideoBean()
        videoBean1.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean1.videoId= R.raw.video1
        videoBean1.videoRes = "android.resource://com.example.tiltok_xsb/${R.raw.video1}"
        videoBean1.distance = 5f
        videoBean1.userBean?.isFollowed = false
        videoBean1.isLiked = false
        videoBean1.likeCount = 666
        videoBean1.commentCount = 444
        videoBean1.collectCount = 500
        videoBean1.shareCount = 500

        val userBean1 = UserBean()
        userBean1.userId = 1
        userBean1.headId = R.mipmap.head1
        userBean1.nickName = "南京街坊"
        userBean1.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean1.subCount = 1000000
        userBean1.focusCount = 10
        userBean1.fansCount = 1000000
        userBean1.workCount = 50
        userBean1.dynamicCount = 50
        userBean1.likeCount = 10
        userBean1.isFollowed = false
        userList.add(userBean1)
        videoBean1.userBean = userBean1

        val videoBean2= VideoBean()
        videoBean2.content = "#搞笑 #街拍 有趣的灵魂万里挑一"
        videoBean2.videoId= R.raw.video2
        videoBean2.videoRes = "android.resource://com.example.tiltok_xsb/${R.raw.video2}"
        videoBean2.distance = 5f
        videoBean2.userBean?.isFollowed = false
        videoBean2.isLiked = false
        videoBean2.likeCount = 777
        videoBean2.commentCount = 444
        videoBean2.collectCount = 500
        videoBean2.shareCount = 500

        val userBean2 = UserBean()
        userBean2.userId = 2
        userBean2.headId = R.mipmap.head1
        userBean2.nickName = "搞笑街拍"
        userBean2.sign = "分享生活中的有趣瞬间"
        userBean2.subCount = 1000000
        userBean2.focusCount = 10
        userBean2.fansCount = 1000000
        userBean2.workCount = 50
        userBean2.dynamicCount = 50
        userBean2.likeCount = 10
        userBean2.isFollowed = false
        userList.add(userBean2)
        videoBean2.userBean = userBean2



        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean1)
        datas.add(videoBean2)

    }

    //无需实例化，直接静态访问
    companion object {
        @JvmField
        var datas = ArrayList<VideoBean>()
        @JvmField
        var userList = ArrayList<UserBean>()
    }
}