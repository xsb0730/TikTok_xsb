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
        videoBean1.distance = 5f
        videoBean1.userBean?.isFollowed = false
        videoBean1.isLiked = true
        videoBean1.likeCount = 100000
        videoBean1.commentCount = 1000
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
        userList.add(userBean1)
        videoBean1.userBean = userBean1

        val videoBean2= VideoBean()
        videoBean2.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean2.videoId= R.raw.video1
        videoBean2.distance = 5f
        videoBean2.userBean?.isFollowed = false
        videoBean2.isLiked = true
        videoBean2.likeCount = 100000
        videoBean2.commentCount = 1000
        videoBean2.shareCount = 500

        val userBean2 = UserBean()
        userBean2.userId = 2
        userBean2.headId = R.mipmap.head1
        userBean2.nickName = "南京街坊"
        userBean2.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean2.subCount = 1000000
        userBean2.focusCount = 10
        userBean2.fansCount = 1000000
        userBean2.workCount = 50
        userBean2.dynamicCount = 50
        userBean2.likeCount = 10
        userList.add(userBean2)
        videoBean2.userBean = userBean2

        val videoBean3= VideoBean()
        videoBean3.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean3.videoId= R.raw.video1
        videoBean3.distance = 5f
        videoBean3.userBean?.isFollowed = false
        videoBean3.isLiked = true
        videoBean3.likeCount = 100000
        videoBean3.commentCount = 1000
        videoBean3.shareCount = 500

        val userBean3 = UserBean()
        userBean3.userId = 3
        userBean3.headId = R.mipmap.head1
        userBean3.nickName = "南京街坊"
        userBean3.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean3.subCount = 1000000
        userBean3.focusCount = 10
        userBean3.fansCount = 1000000
        userBean3.workCount = 50
        userBean3.dynamicCount = 50
        userBean3.likeCount = 10
        userList.add(userBean3)
        videoBean3.userBean = userBean3

        val videoBean4= VideoBean()
        videoBean4.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean4.videoId= R.raw.video1
        videoBean4.distance = 5f
        videoBean4.userBean?.isFollowed = false
        videoBean4.isLiked = true
        videoBean4.likeCount = 100000
        videoBean4.commentCount = 1000
        videoBean4.shareCount = 500

        val userBean4 = UserBean()
        userBean4.userId = 4
        userBean4.headId = R.mipmap.head1
        userBean4.nickName = "南京街坊"
        userBean4.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean4.subCount = 1000000
        userBean4.focusCount = 10
        userBean4.fansCount = 1000000
        userBean4.workCount = 50
        userBean4.dynamicCount = 50
        userBean4.likeCount = 10
        userList.add(userBean4)
        videoBean4.userBean = userBean4

        val videoBean5= VideoBean()
        videoBean5.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean5.videoId= R.raw.video1
        videoBean5.distance = 5f
        videoBean5.userBean?.isFollowed = false
        videoBean5.isLiked = true
        videoBean5.likeCount = 100000
        videoBean5.commentCount = 1000
        videoBean5.shareCount = 500

        val userBean5 = UserBean()
        userBean5.userId = 5
        userBean5.headId = R.mipmap.head1
        userBean5.nickName = "南京街坊"
        userBean5.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean5.subCount = 1000000
        userBean5.focusCount = 10
        userBean5.fansCount = 1000000
        userBean5.workCount = 50
        userBean5.dynamicCount = 50
        userBean5.likeCount = 10
        userList.add(userBean5)
        videoBean5.userBean = userBean5

        val videoBean6= VideoBean()
        videoBean6.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean6.videoId= R.raw.video1
        videoBean6.distance = 5f
        videoBean6.userBean?.isFollowed = false
        videoBean6.isLiked = true
        videoBean6.likeCount = 100000
        videoBean6.commentCount = 1000
        videoBean6.shareCount = 500

        val userBean6 = UserBean()
        userBean6.userId = 6
        userBean6.headId = R.mipmap.head1
        userBean6.nickName = "南京街坊"
        userBean6.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean6.subCount = 1000000
        userBean6.focusCount = 10
        userBean6.fansCount = 1000000
        userBean6.workCount = 50
        userBean6.dynamicCount = 50
        userBean6.likeCount = 10
        userList.add(userBean6)
        videoBean6.userBean = userBean6

        val videoBean7= VideoBean()
        videoBean7.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean7.videoId= R.raw.video1
        videoBean7.distance = 5f
        videoBean7.userBean?.isFollowed = false
        videoBean7.isLiked = true
        videoBean7.likeCount = 100000
        videoBean7.commentCount = 1000
        videoBean7.shareCount = 500

        val userBean7 = UserBean()
        userBean7.userId = 7
        userBean7.headId = R.mipmap.head1
        userBean7.nickName = "南京街坊"
        userBean7.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean7.subCount = 1000000
        userBean7.focusCount = 10
        userBean7.fansCount = 1000000
        userBean7.workCount = 50
        userBean7.dynamicCount = 50
        userBean7.likeCount = 10
        userList.add(userBean7)
        videoBean7.userBean = userBean7

        val videoBean8= VideoBean()
        videoBean8.content = "#街坊 #颜值打分 给自己颜值打100分的女生集合"
        videoBean8.videoId= R.raw.video1
        videoBean8.distance = 5f
        videoBean8.userBean?.isFollowed = false
        videoBean8.isLiked = true
        videoBean8.likeCount = 100000
        videoBean8.commentCount = 1000
        videoBean8.shareCount = 500

        val userBean8 = UserBean()
        userBean8.userId = 8
        userBean8.headId = R.mipmap.head1
        userBean8.nickName = "南京街坊"
        userBean8.sign = "你们喜欢的话题，就是我们采访的内容"
        userBean8.subCount = 1000000
        userBean8.focusCount = 10
        userBean8.fansCount = 1000000
        userBean8.workCount = 50
        userBean8.dynamicCount = 50
        userBean8.likeCount = 10
        userList.add(userBean8)
        videoBean8.userBean = userBean8

        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean3)
        datas.add(videoBean4)
        datas.add(videoBean5)
        datas.add(videoBean6)
        datas.add(videoBean7)
        datas.add(videoBean8)
        datas.add(videoBean1)
        datas.add(videoBean2)
        datas.add(videoBean3)
        datas.add(videoBean4)
        datas.add(videoBean5)
        datas.add(videoBean6)
        datas.add(videoBean7)
        datas.add(videoBean8)
    }

    //无需实例化，直接静态访问
    companion object {
        @JvmField
        var datas = ArrayList<VideoBean>()
        @JvmField
        var userList = ArrayList<UserBean>()
    }
}