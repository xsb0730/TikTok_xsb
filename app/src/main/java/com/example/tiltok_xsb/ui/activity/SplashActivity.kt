package com.example.tiltok_xsb.ui.activity


import android.content.Intent
import android.os.CountDownTimer
import android.widget.Toast
import com.example.tiltok_xsb.base.BaseBindingActivity
import com.example.tiltok_xsb.databinding.ActivitySplashBinding
import com.example.tiltok_xsb.utils.DataCreate

/**
 * 启动页
 */
class SplashActivity: BaseBindingActivity<ActivitySplashBinding>({ActivitySplashBinding.inflate(it)}) {
    override fun init() {
        //全屏显示
        setFullScreen()

        //数据初始化
        try {
            DataCreate()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "数据初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }


        object:CountDownTimer(2000,1000){
            //倒计时过程中无操作
            override fun onTick(millisUntilFinished: Long) {}

            //倒计时结束后跳转MainActivity
            override fun onFinish() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }.start()
    }
}