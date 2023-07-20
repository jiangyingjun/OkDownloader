package com.jyj.okdownloader.utils

import android.util.Log
import com.jyj.okdownloader.OkDownloader
import okhttp3.Interceptor
import okhttp3.Response

class RetryInterceptor : Interceptor {

    companion object {
        var RETRY_TIMES = 3
    }

    var maxRetry = RETRY_TIMES
    private var retryNum = 0 //假如设置为3次重试的话，则最大可能请求4次（默认1次+3次重试）

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        while (!response.isSuccessful && retryNum < maxRetry) {
            retryNum++
            Log.d(OkDownloader.TAG, "retry times =${retryNum}")
            response = chain.proceed(request)
        }
        return response
    }
}