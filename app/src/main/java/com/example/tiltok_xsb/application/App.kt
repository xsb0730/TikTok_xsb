package com.example.tiltok_xsb.application

import android.app.Application
import android.util.Log

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("App", "Application 启动")
    }
}