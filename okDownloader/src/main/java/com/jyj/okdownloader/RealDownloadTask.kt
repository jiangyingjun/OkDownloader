package com.jyj.okdownloader

import android.net.Uri
import android.util.Log
import com.jyj.okdownloader.entity.DownloadTaskStatus
import com.jyj.okdownloader.entity.FileDownLoadInfoBean
import com.jyj.okdownloader.listener.OnDownloadListener
import com.jyj.okdownloader.listener.WrapOnDownloadListener
import com.jyj.okdownloader.manager.ProcessManger
import com.jyj.okdownloader.utils.OkHttpUtil
import com.jyj.okdownloader.utils.Util
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.concurrent.Executor

class RealDownloadTask : DownloadTask {

    private val TEMP_FILE_EXTENSION = "downloading"

    private val downloadInfoBean by lazy {
        FileDownLoadInfoBean(
            0,
            0,
            "",
            "", "",
            DownloadTaskStatus.IDLE
        )
    }

    private var mListenerList: ArrayList<WrapOnDownloadListener>? = null
    private var mTag: Any? = null
    private var mId: String = ""
    override fun localPath(path: String?): DownloadTask {
        if (path.isNullOrEmpty()) {
            throw NullPointerException("path can not is empty")
        }
        return localPath(File(path))
    }

    override fun localPath(file: File): DownloadTask {
        downloadInfoBean.filePath = file.path
        return this
    }

    override fun downloadUrl(url: String): DownloadTask {
        val uri = Uri.parse(url)
        if (uri.scheme.isNullOrEmpty() || uri.authority.isNullOrEmpty()) {
            throw Exception("url is error")
        }
        downloadInfoBean.url = url
        return this
    }

    override fun downloadFile(): File {
        return File(downloadInfoBean.filePath)
    }

    override fun listener(notifyInUI: Boolean, listener: OnDownloadListener?): DownloadTask {
        listener ?: return this
        if (mListenerList.isNullOrEmpty()) {
            mListenerList = ArrayList()
        }
        mListenerList?.also { list ->
            list.forEach {
                if (it.listener.hashCode() == listener.hashCode()) {
                    return@also
                }
            }
        }
        mListenerList?.add(WrapOnDownloadListener(notifyInUI, listener))
        ProcessManger.processRecordMap[this] = mListenerList
        return this
    }

    override fun startDownLoad() {
        start()
    }

    override fun pause(tag: Any?) {
        if (tag == mTag || tag == null) {
            downloadInfoBean.taskStatus = DownloadTaskStatus.PAUSE
        }
    }

    override fun taskStatus(): DownloadTaskStatus {
        return downloadInfoBean.taskStatus
    }

    override fun setTag(tag: Any?): DownloadTask {
        this.mTag = tag
        return this
    }

    override fun getTag(): Any? {
        return mTag
    }

    override fun getId(): String {
        return mId
    }

    override fun createExecutor(): Executor {
        return ExecutorManager.instance.ioExecutor
    }

    fun generateTaskId() {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(downloadInfoBean.url.toByteArray())
        messageDigest.update(downloadInfoBean.filePath.toByteArray())
        mId = Util.sha256BytesToHex(messageDigest.digest())
    }

    fun checkHasDownFileSize() {
        val file = File(downloadInfoBean.filePath)
        if (!file.exists()) return
        if (file.isDirectory) {
            file.list()?.forEach { name ->
                if (name.startsWith(generateFileName(downloadInfoBean.url))) {
                    val childFile = File(downloadInfoBean.filePath + File.separator + name)
                    if (childFile.exists()) {
                        downloadInfoBean.hasDownloadSize = childFile.length()
                    }
                    return@forEach
                }

            }
        }

    }

    private fun start() {

        ProcessManger.processRecordMap[this]?.forEach {
            it.onStart(this)
        }
        val targetFile = makeTargetFile(downloadInfoBean)
        if (targetFile.extension != TEMP_FILE_EXTENSION) {
            downloadInfoBean.fileExtension = targetFile.extension
        }

        val sameDirectoryHasFile = checkTheSameDirectoryHasFile(targetFile)
        if (sameDirectoryHasFile) {
            downloadInfoBean.taskStatus = DownloadTaskStatus.COMPLETE
            notifyIsComplete()
            return
        }
        val file: File
        try {
            file = makeTempLocalFile(targetFile, downloadInfoBean)
        } catch (ex: Exception) {
            notifyIsError(ex.message ?: "make temp file fail")
            return
        }


        if (downloadInfoBean.length <= 0) {
            downloadInfoBean.length = getFileSizeForUrl(downloadInfoBean.url)
            if (downloadInfoBean.length <= 0) {
                notifyIsError("task tag =${mTag}  url=${downloadInfoBean.url} request error.\n please check download url is available")
                return
            }
        }


        try {
            val randomAccessFile = RandomAccessFile(file, "rw")
            val request = Request.Builder()
                .addHeader(
                    "Range",
                    "bytes=${downloadInfoBean.hasDownloadSize}-${downloadInfoBean.length}"
                )
                .url(downloadInfoBean.url).build()
            val response = OkHttpUtil.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    responseBody.byteStream().use { inputStream ->
                        try {

                            val buffer = ByteArray(4096)
                            var len = inputStream.read(buffer)
                            randomAccessFile.seek(downloadInfoBean.hasDownloadSize)
                            if (len != -1) {
                                downloadInfoBean.taskStatus = DownloadTaskStatus.RUNNING
                            }
                            while (len != -1 && downloadInfoBean.taskStatus == DownloadTaskStatus.RUNNING) {

                                randomAccessFile.write(buffer, 0, len)
                                len = inputStream.read(buffer)
                                downloadInfoBean.hasDownloadSize += len
                                ProcessManger.processRecordMap[this]?.forEach { listener ->
                                    listener.onProcess(
                                        this@RealDownloadTask,
                                        downloadInfoBean.hasDownloadSize,
                                        downloadInfoBean.length
                                    )
                                }
                            }

                            when (downloadInfoBean.taskStatus) {
                                DownloadTaskStatus.RUNNING -> {
                                    val absolutePath = file.absolutePath
                                    if (absolutePath.endsWith(TEMP_FILE_EXTENSION)) {
                                        val okFile = File(
                                            file.absolutePath.substringBeforeLast(
                                                TEMP_FILE_EXTENSION
                                            ).plus(downloadInfoBean.fileExtension)
                                        )
                                        file.renameTo(okFile)
                                        downloadInfoBean.filePath = okFile.absolutePath
                                    }
                                    downloadInfoBean.taskStatus = DownloadTaskStatus.COMPLETE
                                    notifyIsComplete()
                                }
                                DownloadTaskStatus.PAUSE -> {
                                    ProcessManger.processRecordMap[this]?.forEach { listener ->
                                        listener.onPause(this)
                                    }
                                }
                                else -> {
                                    notifyIsError("running Error")
                                }
                            }
                        } catch (ex: Exception) {
                            notifyIsError(ex.message ?: "task download fail")
                        } finally {
                            inputStream.close()
                            randomAccessFile.close()
                        }

                    }
                }
            } else {
                randomAccessFile.close()
            }
        } catch (e: Exception) {
            notifyIsError(e.message ?: "download error")
        }
    }

    private fun makeTargetFile(bean: FileDownLoadInfoBean): File {
        var file = File(bean.filePath)
        if (file.isDirectory || file.extension.isNullOrEmpty()) {
            val networkFileName = Uri.parse(bean.url).pathSegments.last().trim()
            val lastIndexOf = networkFileName.lastIndexOf(".") + 1
            var extensionName = ""
            if (lastIndexOf < networkFileName.length) {
                extensionName = networkFileName.substring(lastIndexOf)
            }
            bean.filePath =
                file.absolutePath + File.separator + generateFileName(bean.url) + "." + extensionName
            file = File(bean.filePath)
        }
        return file
    }

    /**
     * 根据外部传入的下载源url 生成本地downloading文件
     * */
    private fun makeTempLocalFile(_file: File, bean: FileDownLoadInfoBean): File {
        var file = _file
        if (file.absolutePath.endsWith(TEMP_FILE_EXTENSION) && bean.taskStatus == DownloadTaskStatus.PAUSE) {
            return file
        }
        if (!bean.fileExtension.isNullOrEmpty()) {
            val tempPath =
                file.absolutePath.substringBeforeLast(file.extension).plus(TEMP_FILE_EXTENSION)
            bean.filePath = tempPath
            file = File(tempPath)
        }

        if (file.parentFile != null && !file.parentFile!!.exists()) {
            file.parentFile!!.mkdirs()
        }

        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    private fun notifyIsComplete(): Boolean {
        if (downloadInfoBean.taskStatus == DownloadTaskStatus.COMPLETE) {
            ProcessManger.processRecordMap[this]?.forEach { listener ->
                listener.onComplete(this)
            }
            return true
        }
        return false
    }

    private fun notifyIsError(errorMessage: String) {
        downloadInfoBean.taskStatus = DownloadTaskStatus.ERROR
        ProcessManger.processRecordMap[this]?.forEach { listener ->
            listener.onError(this, Exception(errorMessage))
        }
    }

    /**
     *
     * @return 待下载文件的大小  单位 Byte
     * */
    private fun getFileSizeForUrl(url: String): Long {
        val uri = Uri.parse(url)
        if (uri.scheme.isNullOrEmpty() || uri.authority.isNullOrEmpty()) {
            return 0L
        }
        var result = 0L
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            val response = OkHttpUtil.okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                response.header("Content-Length")?.apply {
                    result = toLong()
                }
                if (result <= 0L) {
                    response.body()?.apply {
                        result = contentLength()
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("jyj---", "${e.message}")
        }

        return result

    }


    private fun generateFileName(url: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(url.toByteArray())
        return Util.sha256BytesToHex(messageDigest.digest())
    }

    private fun checkTheSameDirectoryHasFile(file: File?): Boolean {
        file ?: return false
        file.parentFile ?: return false
        val targetFileName = file.name
        if (file.parentFile!!.isDirectory) {

            for (index in 0 until (file.parentFile!!.list()?.size ?: 0)) {
                val fileName = file.parentFile!!.list()?.getOrNull(index)
                if (fileName == targetFileName && fileName?.endsWith(TEMP_FILE_EXTENSION) == false) {
                    return true
                }
            }
        }
        return false
    }

    class Builder {
        private lateinit var url: String
        private var tag: Any? = null
        private var localFilePath: String? = null
        private var notifyInUI: Boolean = false
        private var listener: OnDownloadListener? = null
        fun downloadUrl(url: String): Builder {
            this.url = url
            return this
        }

        fun setTag(tag: Any?): Builder {
            this.tag = tag
            return this
        }

        fun localPath(path: String): Builder {
            return localPath(File(path))
        }

        fun localPath(file: File): Builder {
            localFilePath = file.path
            return this
        }

        fun listener(notifyInUI: Boolean = false, listener: OnDownloadListener): Builder {
            this.notifyInUI = notifyInUI
            this.listener = listener
            return this
        }

        fun build(): DownloadTask {
            val task = RealDownloadTask()
            task.downloadUrl(url)
                .localPath(localFilePath)
                .listener(notifyInUI, listener)
                .setTag(tag)
            task.generateTaskId()
            task.checkHasDownFileSize()
            return task


        }

    }


}