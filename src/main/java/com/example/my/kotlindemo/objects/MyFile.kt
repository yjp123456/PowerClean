package com.example.my.kotlindemo

import android.graphics.drawable.Drawable

/**
 * Created by jieping_yang on 2017/4/27.
 */

class MyFile {
    var name: String? = null
    var size: Long = 0
    var isChose = true
    var filePath: String? = null
    var packageName: String? = null
    var formatSize: String? = null
    var icon: Drawable? = null

    constructor(name: String?, formatSize: String, filePath: String?, packageName: String?, size: Long, icon: Drawable?) {
        //每个属性默认有set/get，获取时直接当做json属性来获取，不需要调用set/get函数了
        this.name = name
        this.size = size
        this.filePath = filePath
        this.packageName = packageName
        this.formatSize = formatSize
        this.icon = icon
    }

    open fun test(){

    }




}
