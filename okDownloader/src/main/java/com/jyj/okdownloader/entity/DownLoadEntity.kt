package com.jyj.okdownloader.entity

data class FileDownLoadInfoBean(
    var hasDownloadSize: Long = 0,
    var length: Long,
    var url: String,
    var filePath: String,
    var fileExtension: String,
    @Volatile
    var taskStatus: DownloadTaskStatus
)


enum class DownloadTaskStatus {
    IDLE,
    RUNNING,
    PAUSE,
    COMPLETE,
    ERROR

}