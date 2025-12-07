package com.example.tiltok_xsb.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.tiltok_xsb.ui.fragment.VideoListFragment

class PersonalHomePagerAdapter(
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles = arrayOf("作品", "推荐", "收藏", "喜欢")

    override fun getCount(): Int = titles.size

    override fun getItem(position: Int): Fragment {
        return VideoListFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}