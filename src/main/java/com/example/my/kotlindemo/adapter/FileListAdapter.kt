package com.example.my.kotlindemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by jieping_yang on 2017/4/27.
 */

class FileListAdapter(private val context: Context, private var fileList: List<com.example.my.kotlindemo.MyFile>) : BaseAdapter() {
    private val mInflater: LayoutInflater

    init {
        this.mInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return fileList.size
    }

    override fun getItem(postion: Int): Any {
        return fileList[postion]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(com.example.my.kotlindemo.R.layout.item, null)
            holder = ViewHolder()
            holder.appIcon = convertView!!.findViewById(com.example.my.kotlindemo.R.id.appIcon) as ImageView
            holder.name = convertView.findViewById(com.example.my.kotlindemo.R.id.name) as TextView
            holder.size = convertView.findViewById(com.example.my.kotlindemo.R.id.size) as TextView
            holder.chose = convertView.findViewById(com.example.my.kotlindemo.R.id.chose) as CheckBox
            convertView.tag = holder
        } else
            holder = convertView.tag as ViewHolder
        val myFile = fileList[position]
        holder.appIcon!!.setImageDrawable(myFile.icon)
        holder.chose!!.isChecked = myFile.isChose
        holder.name!!.text = myFile.name
        holder.size!!.text = myFile.formatSize
        holder.chose!!.setOnCheckedChangeListener { compoundButton, b -> fileList[position].isChose = b }
        return convertView
    }

    private class ViewHolder {

        internal var appIcon: ImageView? = null
        internal var name: TextView? = null
        internal var size: TextView? = null
        internal var chose: CheckBox? = null

    }
}
