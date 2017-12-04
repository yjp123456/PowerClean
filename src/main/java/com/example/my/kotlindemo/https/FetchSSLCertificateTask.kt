package com.example.my.kotlindemo

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.URL
import java.security.SecureRandom
import java.util.concurrent.CountDownLatch
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

/**
 * Created by jieping_yang on 2017/5/24.
 */

class FetchSSLCertificateTask : AsyncTask<String, Any, Any> {

    val LOG_TAG = "FetchSSLCertificateTask"
    var countDownLatch: CountDownLatch? = null
    var sslCertificateFetcherListener: com.example.my.kotlindemo.SSLCertificateFetcherListener? = null
    var mContext: Context? = null

    constructor(countDownLatch: CountDownLatch, sslCertificateFetcherListener: SSLCertificateFetcherListener, mContext: Context) {
        this.countDownLatch = countDownLatch
        this.sslCertificateFetcherListener = sslCertificateFetcherListener
        this.mContext = mContext

    }

    override fun doInBackground(vararg param: String): Any? {
        val hostURL = param[0]
        val url = URL(hostURL)
        if (url.protocol.toLowerCase().equals("https")) {
            try {
                val conn = url.openConnection() as HttpsURLConnection
                conn.setHostnameVerifier { hostName, sslSession ->
                    val hostnameVerification = HttpsURLConnection.getDefaultHostnameVerifier().verify(hostName, sslSession)
                    Log.d(LOG_TAG, "hostname verify result is " + hostnameVerification)
                    hostnameVerification
                }
                val sslContext = SSLContext.getInstance("TLS")
               // val keyStore = SSLUtil.getTrustStore(mContext)
                val myTrustManager = MyTrustManager(sslCertificateFetcherListener, null)
                sslContext.init(null, arrayOf<TrustManager>(myTrustManager), SecureRandom())
                conn.sslSocketFactory = sslContext.socketFactory
                conn.requestMethod = "GET"
                conn.connectTimeout = 5 * 1000
                val response = conn.responseCode
                Log.d(LOG_TAG,"response is "+response)

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                countDownLatch?.countDown()
            }
        } else
            countDownLatch?.countDown()

        return null
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
    }

}
