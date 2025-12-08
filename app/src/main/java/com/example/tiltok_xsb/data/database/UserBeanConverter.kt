package com.example.tiltok_xsb.data.database

import androidx.room.TypeConverter
import com.example.tiltok_xsb.data.model.UserBean
import com.google.gson.Gson

@Suppress("unused")
class UserBeanConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromUserBean(userBean: UserBean): String {
        return gson.toJson(userBean)
    }

    @TypeConverter
    fun toUserBean(json: String): UserBean {
        return gson.fromJson(json, UserBean::class.java)
    }
}