package com.example.my.kotlindemo

import android.content.Context
import java.util.concurrent.CountDownLatch


/**
 * Created by jieping_yang on 2017/5/24.
 */

class MIitmCheck : SSLCertificateFetcherListener {
    public var mContext: Context? = null

    constructor(mContext: Context) {
        this.mContext = mContext
    }

    override fun shouldAcceptCertificate(chain: String, result: Boolean) {
        var verify_result = ""
        if (result) {
            verify_result = if (getChainHashes().contains(chain)) CertificateResult.CERTIFICATE_OK else CertificateResult.WRONG_CA
        } else {
            verify_result = CertificateResult.NOT_TRUSTED
        }

        handleResult(verify_result)
    }

    private fun getChainHashes(): String {
        val mitm_prefs = mContext?.getSharedPreferences("MITM_SHARED", Context.MODE_PRIVATE)
        val chain = mitm_prefs?.getString("cert_chain", "MIIFFDCCA/ygAwIBAgIMIt3cjPmCP+Es4FUwMA0GCSqGSIb3DQEBCwUAMGAxCzAJBgNVBAYTAkJFMRkwFwYDVQQKExBHbG9iYWxTaWduIG52LXNhMTYwNAYDVQQDEy1HbG9iYWxTaWduIERvbWFpbiBWYWxpZGF0aW9uIENBIC0gU0hBMjU2IC0gRzIwHhcNMTYwODA0MDMwNjUyWhcNMTcwODA1MDMwNjUyWjBDMSEwHwYDVQQLExhEb21haW4gQ29udHJvbCBWYWxpZGF0ZWQxHjAcBgNVBAMMFSoubWFycy50cmVuZG1pY3JvLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMQAvRE1i37aw+3jRIKw/cFAWU1FB1kBnEvph9CIBB/YXEOEpbpGn3+A7QIogS8/KnvoUiom54szbL6WcA0HqXO/QzP+FJVYPJUIXZXqShI9poIf/RO83HSlajeVaecF88bH5TuEJiW4HvI5LiU7g5G56qKWTmvuyjg2xH2/OcL539VSx2vkyFnnSnEdhNgSLRR/C7QHFgqVRUwdIANlgmETKR7KYZ1HW/doq4OgRqCxu31LPZiBuq/l5QSaOgSqhLm+MJw9zb/lW57bqY2pqJiWu18Okc/PQ/Mbq+wbgc8hxmVkvEHeRm672Fk6A/pc2NV4EJm4WMH96TjXxNBOmb8CAwEAAaOCAekwggHlMA4GA1UdDwEB/wQEAwIFoDCBlAYIKwYBBQUHAQEEgYcwgYQwRwYIKwYBBQUHMAKGO2h0dHA6Ly9zZWN1cmUuZ2xvYmFsc2lnbi5jb20vY2FjZXJ0L2dzZG9tYWludmFsc2hhMmcycjEuY3J0MDkGCCsGAQUFBzABhi1odHRwOi8vb2NzcDIuZ2xvYmFsc2lnbi5jb20vZ3Nkb21haW52YWxzaGEyZzIwVgYDVR0gBE8wTTBBBgkrBgEEAaAyAQowNDAyBggrBgEFBQcCARYmaHR0cHM6Ly93d3cuZ2xvYmFsc2lnbi5jb20vcmVwb3NpdG9yeS8wCAYGZ4EMAQIBMAkGA1UdEwQCMAAwQwYDVR0fBDwwOjA4oDagNIYyaHR0cDovL2NybC5nbG9iYWxzaWduLmNvbS9ncy9nc2RvbWFpbnZhbHNoYTJnMi5jcmwwNQYDVR0RBC4wLIIVKi5tYXJzLnRyZW5kbWljcm8uY29tghNtYXJzLnRyZW5kbWljcm8uY29tMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAdBgNVHQ4EFgQUYoQVrmHWMGZgNXznWbyEK+xevwcwHwYDVR0jBBgwFoAU6k581IAt5RWBhiaMgm3AmKTPlw8wDQYJKoZIhvcNAQELBQADggEBACxQzoXXw3I6Wftz1npAN3oNxnbyXp2bua/oXOmy/7mkNrnjNh7H6upx6hWkHlibMXW0CAGFS7pXMI18f5QWJ9lL/7iNMNYQ00nqvGvzUSgPHkEjZNit+sr0eipRGPyYIZzesJzuY1EvOjpcpF9aHXVW4y6mm/9x3v5b1hmLxIRyFax69QavxTOda0wNOohk6wQEpbBQzEkcU+gyTL768S+VvibPM2m/C7/D7vjlM77zMiy0D3K5RifY/OqbfaRvcygpQOrAfjvUUHqAfuo6IIYAk2+He57qG8v1C2CUg4Umal5onVv5gOgI712fozBw8PRf9MpxbtRL0oo0k5pHb08=")
        return chain!!
    }

    private fun handleResult(verifyResult: String) {

        when (verifyResult) {
            CertificateResult.CERTIFICATE_OK -> {

            }
            CertificateResult.NOT_TRUSTED -> {

            }
            CertificateResult.WRONG_CA -> {

            }
            else -> {

            }
        }

    }


    public fun check(countDownLatch: CountDownLatch) {
        FetchSSLCertificateTask(countDownLatch, this as SSLCertificateFetcherListener, mContext!!).execute("https://rest.mars.trendmicro.com")
    }

}
