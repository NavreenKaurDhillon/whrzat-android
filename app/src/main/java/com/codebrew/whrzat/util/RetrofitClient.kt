package com.codebrew.whrzat.util


import android.content.Context
import android.util.Log
import com.codebrew.whrzat.webservice.api.whrzatApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private const val TAG = "RetrofitClient"
    lateinit var RETROFIT_CLIENT: whrzatApi

    fun get(): whrzatApi = RETROFIT_CLIENT

    fun setUpRetrofitClient(context: Context) {

        val okHttpClient =  getUnsafeOkHttpClient(context)
        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient?:OkHttpClient())

               //  .client(fetchHeaders(context))
                .build()
        RETROFIT_CLIENT = retrofit.create(whrzatApi::class.java)
    }

    fun getUnsafeOkHttpClient(context: Context): OkHttpClient? {
        return try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                        }
                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                        }
                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val interceptor = Interceptor { chain ->
                val builder = chain.request().newBuilder()
                // if (Prefs.with(context).getBoolean(Constants.LOGIN_STATUS, false))

                val newRequest = builder.addHeader("authorization", "bearer " +
                        Prefs.with(context).getString(Constants.ACCESS_TOKEN, ""))
                        .build()
                chain.proceed(newRequest)
                /* else {
                    val newRequest = builder.addHeader("authorization", "bearer " + Prefs.with(context).getString(Constants.FACEBOOK_ACCES_TOKEN, ""))
                            .build()
                    chain.proceed(newRequest)
                }*/
            }
            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { hostname, session -> true }
            builder.interceptors().add(interceptor)
            builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
    
}
