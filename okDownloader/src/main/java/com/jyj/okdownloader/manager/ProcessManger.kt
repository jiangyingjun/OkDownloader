package com.jyj.okdownloader.manager

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.jyj.okdownloader.DownloadTask
import com.jyj.okdownloader.listener.OnDownloadListener
import com.jyj.okdownloader.listener.WrapOnDownloadListener
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * manage download task rate
 *
 * */
internal class ProcessManger {

    companion object {

        val TAG: String = ProcessManger::class.java.name

        /**
         * unit ms
         * */
        private const val DEFAULT_INTERVAL = 50

        var INTERVAL = DEFAULT_INTERVAL

        fun setInterval(interval: Int) {
            if (interval <= 0) {
                Log.d(TAG, "interval can not less than zero")
                return
            }
            INTERVAL = interval
        }

        fun isIntervalValid(): Boolean {
            return INTERVAL > 0
        }

        val processRecordMap = ConcurrentHashMap<DownloadTask, ArrayList<WrapOnDownloadListener>?>()

//        val innerProcessRecordMap = HashMap<DownloadTask, WrapOnDownloadListener>()

        private var mTotalTaskListener: OnDownloadListener? = null
        private var mNotifyInUI = false
        fun setTotalTaskListener(notifyInUI: Boolean = false, listener: OnDownloadListener) {
            mNotifyInUI = notifyInUI
            mTotalTaskListener = listener
        }

        fun addTaskListener(task: DownloadTask) {
            var listenerList = processRecordMap[task]
            if (listenerList.isNullOrEmpty()) {
                listenerList = ArrayList()
            }
            listenerList.forEach {
                if (it.isInnerListener) {
                    return
                }
            }
            val listener = WrapOnDownloadListener(mNotifyInUI, object : OnDownloadListener {
                override fun onStart(task: DownloadTask) {
                    mTotalTaskListener?.onStart(task)

                }

                override fun onProcess(task: DownloadTask?, process: Long, total: Long) {
                    mTotalTaskListener?.onProcess(task, process, total)
                }

                override fun onPause(task:DownloadTask) {
                    mTotalTaskListener?.onPause(task)
                }

                override fun onComplete(task: DownloadTask?) {
                    mTotalTaskListener?.onComplete(task)
                    processRecordMap.remove(task)
                }

                override fun onError(task: DownloadTask?, error: Exception) {
                    mTotalTaskListener?.onError(task, error)
                }

            })
            listener.isInnerListener = true
            listenerList.add(listener)
            processRecordMap[task] = listenerList
        }
    }


    private val PROCESS_MESSAGE = 100

    val handler by lazy { Handler(Looper.getMainLooper(), UIHandlerCallback()) }


    inner class UIHandlerCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                PROCESS_MESSAGE -> {

                }
            }
            return true
        }
    }

}