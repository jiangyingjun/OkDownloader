package com.jyj.okdownloader

import java.util.concurrent.Executor


interface StartupExecutor {

    fun createExecutor(): Executor
}