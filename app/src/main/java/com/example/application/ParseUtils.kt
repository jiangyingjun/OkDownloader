package com.example.application

object ParseUtils {

    fun parseInt(data: String?, defaultData: Int = 0): Int {
        return if (data.isNullOrEmpty()) {
            defaultData
        } else {
            try {
                data.toInt()
            } catch (e: Exception) {
                defaultData
            }

        }
    }

}