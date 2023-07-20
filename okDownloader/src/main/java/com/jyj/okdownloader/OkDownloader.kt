package com.jyj.okdownloader

import android.util.Log
import com.jyj.okdownloader.listener.OnDownloadListener
import com.jyj.okdownloader.manager.ProcessManger
import com.jyj.okdownloader.manager.RunnableManager
import com.jyj.okdownloader.manager.RunnableManager.downloadRunnableList
import com.jyj.okdownloader.utils.RetryInterceptor


class OkDownloader private constructor() {

    companion object {
        val instance: OkDownloader by lazy { OkDownloader() }
        val TAG: String = OkDownloader::class.java.name
    }


    /**
     * set global process callback interval
     * */
    fun setGlobalInterval(interval: Int): OkDownloader {
        ProcessManger.setInterval(interval)
        return this
    }

    /**
     *Set the number of global download retries
     * */
    fun setRetryTimes(retryTimes: Int): OkDownloader {
        if (retryTimes in 0..10) {
            RetryInterceptor.RETRY_TIMES = retryTimes
        } else {
            Log.e(TAG, "retryTimes=${retryTimes} must Greater than or equal to 0 but less than or equal to 10")
        }
        return this
    }

    fun addTask(taskList: ArrayList<DownloadTask>): OkDownloader {
        RunnableManager.addTask(taskList)
        return this
    }

    fun addTask(task: DownloadTask): OkDownloader {
        RunnableManager.addTask(task)
        return this
    }

    fun removeTask(task: DownloadTask?): Boolean {
        return RunnableManager.removeTask(task)
    }

    fun clearAllTask() {
        RunnableManager.clearAllTask()
    }

    fun setTotalTaskListener(
        notifyInUI: Boolean = false,
        listener: OnDownloadListener
    ): OkDownloader {
        ProcessManger.setTotalTaskListener(notifyInUI, listener)
        return this
    }

    fun getRunnableList(): List<DownloadRunnable> = downloadRunnableList

    fun start(isSerial: Boolean = false) {
        RunnableManager.start(isSerial)
    }

    fun pause(tag: Any? = null) {
        RunnableManager.pause(tag)
    }


}