package com.ivanasen.smarttickets.ui

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.net.Uri
import android.support.annotation.Nullable
import com.bumptech.glide.Glide
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.ivanasen.smarttickets.R
import org.jetbrains.anko.layoutInflater
import java.util.ArrayList


internal class ImageAdapter(val context: Context, val data: LiveData<MutableList<String>>) : BaseAdapter() {

    private val mData: MutableList<String> = mutableListOf()

    init {
        observeData()
    }

    private fun observeData() {
        data.observe(context as LifecycleOwner, Observer {
            if (it != null) {
                swapData(it)
            }
        })
    }

    override fun getView(postion: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?:
            context.layoutInflater.inflate(R.layout.list_item_image, parent, false)

        val imageView: ImageView = view.findViewById(R.id.imageView)
        Glide.with(context)
                .load(mData[postion])
                .into(imageView)

        return view
    }

    override fun getItem(position: Int): Any = mData[position]


    override fun getItemId(postion: Int): Long = 0

    override fun getCount(): Int = mData.size

    fun swapData(@Nullable data: List<String>) {
        if (mData != data) {
            mData.clear()
            mData.addAll(data)
            notifyDataSetChanged()
        }
    }
}