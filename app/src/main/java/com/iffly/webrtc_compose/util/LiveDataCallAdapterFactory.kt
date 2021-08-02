package com.iffly.webrtc_compose.util

import androidx.lifecycle.LiveData
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean


class LiveDataCallAdapterFactory : CallAdapter.Factory() {
    /**
     * 如果你要返回
     * LiveData<?>
     */
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *> {
        if (returnType !is ParameterizedType) {
            throw IllegalArgumentException("返回值需为参数化类型")
        }
        //获取returnType的class类型
        val returnClass = CallAdapter.Factory.getRawType(returnType)
        if (returnClass != LiveData::class.java) {
            throw IllegalArgumentException("返回值不是LiveData类型")
        }
        //先解释一下getParameterUpperBound
        //官方例子
        //For example, index 1 of {@code Map<String, ? extends Runnable>} returns {@code Runnable}.
        //获取的是Map<String,? extends Runnable>参数列表中index序列号的参数类型,即0为String,1为Runnable
        //这里的0就是LiveData<?>中?的序列号,因为只有一个参数
        //其实这个就是我们请求返回的实体
        val type = CallAdapter.Factory.getParameterUpperBound(0, returnType as ParameterizedType)
        return LiveDataCallAdapter<Any>(type)
    }

    /**
     * 请求适配器
     */
    class LiveDataCallAdapter<R>(var type: Type) : CallAdapter<R, LiveData<R>> {
        override fun adapt(call: Call<R>): LiveData<R> {
            return object : LiveData<R>() {
                //这个作用是业务在多线程中,业务处理的线程安全问题,确保单一线程作业
                val flag = AtomicBoolean(false)
                override fun onActive() {
                    super.onActive()
                    if (flag.compareAndSet(false, true)) {
                        call!!.enqueue(object : Callback<R> {
                            override fun onFailure(call: Call<R>, t: Throwable) {
                                postValue(null)
                            }

                            override fun onResponse(call: Call<R>, response: Response<R>) {

                                postValue(response.body())
                            }
                        })
                    }
                }
            }
        }

        override fun responseType(): Type {
            return type
        }
    }


}