package com.example.application

import java.io.File

object FileUtils {
    /**
     * delete_red  file
     *
     * @param file
     */
    fun deleteFile(file: File?): Boolean {
        if (file == null || !file.exists()) {
            return false
        }
        if (file.isDirectory) { // 否则如果它是一个目录
            val files = file.listFiles() // 声明目录下所有的文件 files[];
            for (i in files.indices) { // 遍历目录下所有的文件
                deleteFile(files[i]) // 把每个文件 用这个方法进行迭代
            }
        }
        return file.delete()
    }
}