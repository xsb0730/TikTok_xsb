package com.example.tiltok_xsb.ui.activity

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.tiltok_xsb.R
import com.example.tiltok_xsb.base.BaseBindingActivity
import com.example.tiltok_xsb.base.CommPagerAdapter
import com.example.tiltok_xsb.databinding.ActivityMainBinding
import com.example.tiltok_xsb.ui.fragment.MainFragment

class MainActivity:BaseBindingActivity<ActivityMainBinding>({ActivityMainBinding.inflate(it)}) {
    private var pagerAdapter:CommPagerAdapter?=null
    private val fragments=ArrayList<Fragment>()

    private val mainFragment= MainFragment()

    private var lastTime:Long=0
    private val EXIT_TIME=2000

    override fun init() {
        fragments.add(mainFragment)
        pagerAdapter= CommPagerAdapter(supportFragmentManager,lifecycle,fragments, arrayOf(""))

        binding.viewPager!!.adapter=pagerAdapter

        setupBackPressed()
    }

    //双击退出
    private fun setupBackPressed(){
        onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(System.currentTimeMillis()-lastTime>EXIT_TIME){
                    Toast.makeText(applicationContext,"再按一次退出",Toast.LENGTH_SHORT).show()
                    lastTime=System.currentTimeMillis()
                }else{
                    finish()
                }
            }
        })
    }

    companion object{
        var curMainPage=0
    }
}