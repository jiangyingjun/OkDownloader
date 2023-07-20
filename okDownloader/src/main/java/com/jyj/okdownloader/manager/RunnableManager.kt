package com.jyj.okdownloader.manager

import android.util.Log
import com.jyj.okdownloader.*
import com.jyj.okdownloader.RealDownloadRunnable
import com.jyj.okdownloader.entity.DownloadTaskStatus
import java.util.AbstractList
import java.util.Collections

internal object RunnableManager {


    val downloadRunnableList = Collections.synchronizedList(ArrayList<DownloadRunnable>())

    fun addTask(taskList: ArrayList<DownloadTask>) {
        taskList.forEach { task -> addTask(task) }
    }

    @Synchronized
    fun addTask(task: DownloadTask) {
        downloadRunnableList.forEach { runnable ->
            if (task.getId() == runnable.taskId()) {
                return
            }
        }

        downloadRunnableList.add(RealDownloadRunnable(task))
    }


    fun removeTask(task: DownloadTask?): Boolean {
        task ?: return false
        synchronized(downloadRunnableList) {
            downloadRunnableList.forEach { runnable ->
                if (task.getId() == runnable.taskId()) {
                    downloadRunnableList.remove(runnable)
                    return true
                }
            }
        }
        return false
    }

    fun clearAllTask() {
        downloadRunnableList.forEach { it.pause() }
        downloadRunnableList.clear()
    }

    fun start(isSerial: Boolean = false) {
        if (downloadRunnableList.isEmpty()) {
            Log.d(OkDownloader.TAG, "task is empty")
            return
        }
        if (isSerial) {
            ExecutorManager.instance.ioExecutor.execute {
                downloadRunnableList.reverse()
                startIsSerial()
            }

        } else {
            downloadRunnableList.forEach { runnable ->
                if (runnable is RealDownloadRunnable) {
                    ProcessManger.addTaskListener(runnable.task)
                }
                runnable.innerStart()
            }
        }

    }

    private fun startIsSerial(operationRunnable: DownloadRunnable? = null) {
        synchronized(downloadRunnableList) {
            var tempRunnable = operationRunnable
            for (index in downloadRunnableList.size - 1 downTo 0) {
                val runnable = downloadRunnableList[index]
                if (runnable is RealDownloadRunnable) {
                    ProcessManger.addTaskListener(runnable.task)
                    runnable.mIsSerial = true
                    runnable.setOnSerialDownListener(object : RealDownloadRunnable.OnSerialDownListener {
                        override fun onStart(runnable: DownloadRunnable, checkIsLastRunnable: Boolean) {
                            if (checkIsLastRunnable && downloadRunnableList.last().taskId() != runnable.taskId()) {
                                return
                            }
                            ExecutorManager.instance.ioExecutor.execute {
                                pause()
                                startIsSerial(runnable)
                            }
                        }
                    })
                }
                if (tempRunnable != null) {
                    tempRunnable.innerStart()
                    tempRunnable = null
                } else {
                    runnable.innerStart()
                }

            }
        }
    }

    fun pause(tag: Any? = null) {
        if (downloadRunnableList.isEmpty()) {
            Log.d(OkDownloader.TAG, "task is empty")
            return
        }
        downloadRunnableList.forEach { runnable -> runnable.pause(tag) }
    }

}