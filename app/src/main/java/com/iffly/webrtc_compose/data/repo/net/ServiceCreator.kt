package com.iffly.webrtc_compose.data.repo.net

import com.iffly.webrtc_compose.util.LiveDataCallAdapterFactory
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceCreator {

    //    private final static String IP = "192.168.2.111";
    const val IP = "42.192.40.58:5000"

    private const val HOST = "http://$IP/"

    // 信令地址
    const val WS = "ws://$IP/ws"
    private const val _url = "http://42.192.40.58:5000"
    val url get() = _url
    private val okHttpClient by lazy { OkHttpClient().newBuilder() }
    private val retrofit: Retrofit by lazy {
        val builder = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
        val dispatcher = Dispatcher()
        dispatcher.maxRequests = 1

        okHttpClient
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .dispatcher(dispatcher)
        builder.client(okHttpClient.build()).build()
    }

    fun <T> create(clazz: Class<T>): T = retrofit.create(clazz)

    inline fun <reified T> createService(clazz: Class<T>): T =
        create(clazz)

}