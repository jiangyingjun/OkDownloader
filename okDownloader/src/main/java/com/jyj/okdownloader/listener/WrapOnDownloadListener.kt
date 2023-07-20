package com.jyj.okdownloader.listener

import com.jyj.okdownloader.DownloadTask
import com.jyj.okdownloader.manager.ProcessManger
import com.jyj.okdownloader.manager.RunnableManager

class WrapOnDownloadListener(private val notifyInUI: Boolean, val listener: OnDownloadListener) :
    OnDownloadListener {
    var isInnerListener = false

    private var mTotal = 0L
    private var mProcess = 0L
    private var lastTimeTransmitProcess = 0L

    private val processManger = ProcessManger()
    override fun onStart(task: DownloadTask) {
        if (notifyInUI) {
            processManger.handler.post {
                listener.onStart(task)
            }
        } else {
            listener.onStart(task)
        }

    }

    override fun onProcess(task: DownloadTask?, process: Long, total: Long) {
        if (total == 0L) {
            return
        }
        if (process != total && lastTimeTransmitProcess + ProcessManger.INTERVAL >= System.currentTimeMillis()) {
            return
        }

        mProcess = process
        mTotal = total
        if (notifyInUI) {
            processManger.handler.post {
                listener.onProcess(task, process, total)
            }
        } else {
            listener.onProcess(task, process, total)
        }
        lastTimeTransmitProcess = System.currentTimeMillis()
    }

    override fun onPause(task: DownloadTask) {
        if (notifyInUI) {
            processManger.handler.post {
                listener.onPause(task)
            }
        } else {
            listener.onPause(task)
        }

    }

    override fun onComplete(task: DownloadTask?) {
        onProcess(task, mTotal, mTotal)
        RunnableManager.removeTask(task)
        if (notifyInUI) {
            processManger.handler.post { listener.onComplete(task) }
        } else {
            listener.onComplete(task)
        }

    }

    override fun onError(task: DownloadTask?, error: Exception) {
        if (notifyInUI) {
            processManger.handler.post { listener.onError(task, error) }
        } else {
            listener.onError(task, error)
        }
    }

}