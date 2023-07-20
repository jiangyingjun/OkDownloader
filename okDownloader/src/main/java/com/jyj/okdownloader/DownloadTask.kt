package com.jyj.okdownloader

import com.jyj.okdownloader.entity.DownloadTaskStatus
import com.jyj.okdownloader.listener.OnDownloadListener
import java.io.File

interface DownloadTask : StartupExecutor {


    /**
     * 保存文件路径
     * */
    fun localPath(path: String?): DownloadTask
    fun localPath(file: File): DownloadTask

    fun downloadUrl(url: String): DownloadTask

    fun downloadFile(): File

    fun listener(notifyInUI: Boolean = false, listener: OnDownloadListener?): DownloadTask

    /**
     * 执行下载任务
     * */
    fun startDownLoad()

    fun pause(tag: Any?)


    fun taskStatus(): DownloadTaskStatus

    fun setTag(tag: Any?): DownloadTask

    fun getTag(): Any?

    fun getId(): String

}