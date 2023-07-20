package com.jyj.okdownloader.listener

import com.jyj.okdownloader.DownloadTask

interface OnDownloadListener {

    fun onStart(task: DownloadTask)

    fun onProcess(task: DownloadTask?, process: Long, total: Long)

    fun onPause(task: DownloadTask)

    fun onComplete(task: DownloadTask?)

    fun onError(task: DownloadTask?,error: Exception)

}