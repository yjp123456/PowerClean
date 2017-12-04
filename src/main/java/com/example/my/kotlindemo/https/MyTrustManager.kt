package com.example.my.kotlindemo


import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


/**
 * Created by jieping_yang on 2017/5/24.
 */

class MyTrustManager : X509TrustManager {
    var defaultTrustManager: X509TrustManager? = null
    var sslCertificateFetcherListener: com.example.my.kotlindemo.SSLCertificateFetcherListener? = null
    val LOG_TAG = "MyTrustManager"

    constructor(sslCertificateFetcherListener: com.example.my.kotlindemo.SSLCertificateFetcherListener?, keyStore: KeyStore?) {
        this.sslCertificateFetcherListener = sslCertificateFetcherListener
        this.defaultTrustManager = createTrustManager(keyStore)
    }


    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        var cert_verify = true
        try {
            defaultTrustManager!!.checkServerTrusted(chain, authType)
        } catch (e: CertificateException) {
            Log.d(LOG_TAG, "cert verify fail")
            cert_verify = false
        }

        val cert = Base64.encodeToString(chain!![0].encoded, Base64.NO_WRAP)
        sslCertificateFetcherListener?.shouldAcceptCertificate(cert, cert_verify)

    }

    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class)
    private fun createTrustManager(store: KeyStore?): X509TrustManager {
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
        tmf.init(store)//store is null will use system cer
        val trustManagers = tmf.trustManagers
        return trustManagers[0] as X509TrustManager
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
