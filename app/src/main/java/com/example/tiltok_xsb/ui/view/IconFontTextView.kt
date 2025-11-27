package com.example.tiltok_xsb.ui.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 *图标字体文本视图
 */
class IconFontTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    companion object {
        /** 所有IconFontTextView公用typeface  */
        private var typeface: Typeface? = null
    }

    init {
        try {
            // 尝试加载字体文件
            val typeface = Typeface.createFromAsset(context.assets, "iconfont.ttf")
            setTypeface(typeface)
        } catch (e: Exception) {
            // 如果字体文件不存在，打印警告但不崩溃
            e.printStackTrace()
            // 使用默认字体
        }
    }
}