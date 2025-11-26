package com.example.tiltok_xsb.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import android.view.View
import android.widget.AdapterView.OnItemClickListener

abstract class BaseAdapter<VH:RecyclerView.ViewHolder,T>constructor(diffUtil: DiffUtil.ItemCallback<T>) : ListAdapter<T,VH>(diffUtil) {

    protected var mList:ArrayList<T> = ArrayList()
    private  var itemClickListener:OnItemClickListener?=null

    //列表项点击监听器
    fun setOnClickListener(itemClickListener:OnItemClickListener){
        this.itemClickListener = itemClickListener
    }

    //为列表项添加点击事件
    fun View.setOnItemClick(position: Int) {
        setOnClickListener {
            itemClickListener?.let {
                it.onItemClick(mList[position])
            }
        }
    }

    //设置列表数据（全量刷新）
    fun setList(list:List<T>){
        mList.clear()
        mList.addAll(list)
        submitList(mList.toList()) //只刷新变化的项（而非全量刷新）
    }

    fun clearList(){
        mList.clear()
        submitList(mList.toList())
    }


    //追加列表数据（增量刷新）
    fun appendList(list:List<T>){
        mList.addAll(list)
        submitList(mList.toList())
    }

    //获取当前列表数据
    fun getDatas(): ArrayList<T> {
        return mList
    }

    //返回列表项数量
    override fun getItemCount(): Int {
        return mList.size
    }


    //列表项点击事件接口
    interface OnItemClickListener{
        fun<E>onItemClick(item:E)
    }
}
