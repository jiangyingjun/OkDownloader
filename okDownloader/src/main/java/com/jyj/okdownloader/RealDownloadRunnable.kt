package com.jyj.okdownloader

import android.util.Log
import com.jyj.okdownloader.entity.DownloadTaskStatus

internal class RealDownloadRunnable(val task: DownloadTask) : Runnable, DownloadRunnable {

    var mIsSerial = false

    override fun run() {
        task.startDownLoad()
    }

    override fun taskStatus(): DownloadTaskStatus {
        return task.taskStatus()
    }

    override fun start() {
        if (!checkTaskStatus()) {
            return
        }
        if (serialListener != null) {
            serialListener!!.onStart(this)
        } else {
            task.createExecutor().execute(this)
        }
    }

    /** @hide */
    override fun innerStart() {
        if (!checkTaskStatus()) {
            return
        }
        if (mIsSerial) {
            task.startDownLoad()
            if (task.taskStatus() == DownloadTaskStatus.COMPLETE || task.taskStatus() == DownloadTaskStatus.ERROR) {
                //Check if the runnable is the last one in the queue
                serialListener?.onStart(this, true)
            }
        } else {
            task.createExecutor().execute(this)
        }
    }

    private fun checkTaskStatus(): Boolean {
        val status = task.taskStatus()
        if (status == DownloadTaskStatus.RUNNING) {
            Log.d(OkDownloader.TAG, "task[${task.hashCode()}] is RUNNING")
            return false
        }
        if (status == DownloadTaskStatus.COMPLETE) {
            Log.d(OkDownloader.TAG, "task[${task.hashCode()}] is COMPLETE")
            return false
        }
        return true
    }

    override fun pause(tag: Any?) {
        task.pause(tag)
    }

    override fun stop() {
    }

    override fun taskTag(): Any? {
        return task.getTag()
    }

    override fun taskId(): String {
        return task.getId()
    }

    private var serialListener: OnSerialDownListener? = null

    fun setOnSerialDownListener(l: OnSerialDownListener) {
        serialListener = l
    }

    interface OnSerialDownListener {
        fun onStart(runnable: DownloadRunnable, checkIsLastRunnable: Boolean = false)
    }

}