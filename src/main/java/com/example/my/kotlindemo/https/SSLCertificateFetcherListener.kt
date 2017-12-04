package com.example.my.kotlindemo

/**
 * Created by jieping_yang on 2017/5/24.
 */

interface SSLCertificateFetcherListener {//只有声明为open的类才能被继承，interface默认是open
    fun shouldAcceptCertificate(chain:String, result:Boolean)
}
