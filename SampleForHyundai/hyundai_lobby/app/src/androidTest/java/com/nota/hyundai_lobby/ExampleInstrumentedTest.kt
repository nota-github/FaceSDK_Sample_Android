package com.nota.hyundai_lobby

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import okhttp3.Request

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import android.R.string.no
import android.util.Log
import java.security.SecureRandom
import javax.net.ssl.SSLContext


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.nota.hyundai_lobby", appContext.packageName)
    }

    @Test
    fun redirectTest(){

        val TRUST_ALL_CERTS = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(TRUST_ALL_CERTS), SecureRandom())

        val clientBuilder = OkHttpClient().newBuilder()
        clientBuilder.sslSocketFactory(sslContext.socketFactory, TRUST_ALL_CERTS)
        val client = clientBuilder.build()
        val request = Request.Builder().url("https://google.com").build()
        val response = client.newCall(request).execute()

        Log.d("Lobby", response.body()!!.string())
    }

}