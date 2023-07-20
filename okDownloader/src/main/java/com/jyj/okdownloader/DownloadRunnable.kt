package com.jyj.okdownloader

import com.jyj.okdownloader.entity.DownloadTaskStatus

interface DownloadRunnable {

    fun taskStatus(): DownloadTaskStatus

    fun start()

    /** @hide */
    fun innerStart()

    fun pause(tag:Any?=null)

    fun stop()

    fun taskTag():Any?

    fun taskId():String

}