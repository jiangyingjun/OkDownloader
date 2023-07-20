package com.example.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import com.jyj.okdownloader.DownloadTask
import com.jyj.okdownloader.OkDownloader
import com.jyj.okdownloader.RealDownloadTask
import com.jyj.okdownloader.entity.DownloadTaskStatus
import com.jyj.okdownloader.listener.OnDownloadListener
import java.io.File
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {


    private lateinit var taskContentLayout: LinearLayout

    private val decimalFormat = DecimalFormat("0")
    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        path = this.externalCacheDir!!.absolutePath

        taskContentLayout = findViewById(R.id.taskContentLayout)

        findViewById<Button>(R.id.button4).setOnClickListener {
            OkDownloader.instance.setGlobalInterval(500)

            OkDownloader.instance
                .addTask(makeTask("http://www.w3school.com.cn/i/movie.mp4", 1))
                .addTask(makeTask("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4", 2))
                .addTask(makeTask("http://vjs.zencdn.net/v/oceans.mp4", 3))
                .addTask(makeTask("https://media.w3.org/2010/05/sintel/trailer.mp4", 4))
                .setTotalTaskListener(true, listener)
            taskContentLayout.removeAllViews()
            OkDownloader.instance.getRunnableList().forEach {
                val layout = LayoutInflater.from(this).inflate(R.layout.download_task_item, null)
                val btn = layout.findViewById<Button>(R.id.btn)
                layout.tag = it.taskId()
                taskContentLayout.addView(layout)
                btn.tag = it.taskId()
                btn.setOnClickListener { view ->
                    val tag = view.tag as String?
                    if (!tag.isNullOrEmpty()) {
                        OkDownloader.instance.getRunnableList().forEach { innerRunnable ->
                            if (innerRunnable.taskId() == tag) {
                                Log.e("jyj-->", "operation task tag=${innerRunnable.taskTag()}")
                                if (innerRunnable.taskStatus() == DownloadTaskStatus.RUNNING) {
                                    innerRunnable.pause()
                                    (view as Button).text = "开始"
                                } else if (innerRunnable.taskStatus() == DownloadTaskStatus.COMPLETE) {
                                    (view as Button).text = "已完成"
                                } else {
                                    innerRunnable.start()
                                    (view as Button).text = "暂停"
                                }
                                return@forEach
                            }
                        }
                    }
                }
            }
        }


        findViewById<Button>(R.id.start).setOnClickListener {
            OkDownloader.instance.start(true)
        }

        findViewById<Button>(R.id.pause).setOnClickListener {
            OkDownloader.instance.pause()
        }
        findViewById<Button>(R.id.button5).setOnClickListener {
            val file = File(path)
            FileUtils.deleteFile(file)
            taskContentLayout.removeAllViews()
            OkDownloader.instance.clearAllTask()
        }


    }

    private fun makeTask(url: String, tag: Int): DownloadTask {
        return RealDownloadTask.Builder().downloadUrl(url).localPath(path).setTag(tag).build()
    }


    val listener = object : OnDownloadListener {
        override fun onStart(task: DownloadTask) {
            for (index in 0 until taskContentLayout.childCount) {
                val tag = taskContentLayout.getChildAt(index).tag as? String
                if (tag == task.getId()) {
                    val button = taskContentLayout.getChildAt(index)
                        .findViewById<Button>(R.id.btn)
                    button.text = "暂停"
                }
            }
            Log.e("jyj-->", "onStart  task tag=${task.getTag()}");
        }

        override fun onProcess(task: DownloadTask?, process: Long, total: Long) {
            val processCalculate =
                decimalFormat.format((process / total.toDouble()) * 100)
            for (index in 0 until taskContentLayout.childCount) {
                val tag = taskContentLayout.getChildAt(index).tag as? String
                if (tag == task?.getId()) {
                    val seekBar = taskContentLayout.getChildAt(index)
                        .findViewById<SeekBar>(R.id.seekBar)
                    seekBar.progress = ParseUtils.parseInt(processCalculate)
                    break
                }
            }
            Log.e("jyj-->", "taskTag=${task?.getTag()}  onProcess=${processCalculate}")

        }

        override fun onPause(task: DownloadTask) {
            for (index in 0 until taskContentLayout.childCount) {
                val tag = taskContentLayout.getChildAt(index).tag as? String
                if (tag == task.getId()) {
                    val button = taskContentLayout.getChildAt(index)
                        .findViewById<Button>(R.id.btn)
                    button.text = "开始"
                }
            }
        }

        override fun onComplete(task: DownloadTask?) {
            for (index in 0 until taskContentLayout.childCount) {
                val tag = taskContentLayout.getChildAt(index).tag as? String
                if (tag == task?.getId()) {
                    val button = taskContentLayout.getChildAt(index)
                        .findViewById<Button>(R.id.btn)
                    button.text = "完成"
                    taskContentLayout.getChildAt(index).findViewById<SeekBar>(R.id.seekBar).progress = 100
                }
            }

            Log.e(
                "jyj-->",
                "taskTag=${task?.getTag()}  onComplete=${task?.downloadFile()?.absolutePath}"
            );
        }

        override fun onError(task: DownloadTask?, error: Exception) {
            onPause(task!!)
            Log.e("jyj-->", "error message=${error.message}");
        }
    }

}