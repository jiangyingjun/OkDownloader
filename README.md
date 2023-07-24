# OkDownloader
This is a very simple multi-task file downloader for android。
### feature：

#### 1 You can start or pause any task at will. (Whether it is a serial or parallel download)
#### 2 Files can be downloaded at breakpoints.（No additional use of sqlite to record download information）
#### 3 Incomplete download files may exist with the downloading extension 



## Getting Started

### Step 1. Add the JitPack repository to your build file

allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

 ### Step 2. Add the dependency

 dependencies {
	        implementation 'com.github.jiangyingjun:OkDownloader:1.0.1'
	}

## User Manual

 ### 1.Build Download Task
    RealDownloadTask.Builder()
     //File Network Address
    .downloadUrl(url)
    //Download file storage directory
    .localPath(path)  
     //Custom Task ID
    .setTag(tag)   
    //Task monitoring @param notifyInUI （true:Callback to main thread。false:Callback to task execution thread） #Task callback execution thread#
    .listener(false,object :OnDownloadListener{ 
                override fun onStart(task: DownloadTask) {
                }

                override fun onProcess(task: DownloadTask?, process: Long, total: Long) {
                }

                override fun onPause(task: DownloadTask) {
                }

                override fun onComplete(task: DownloadTask?) {
		    //Completed downloading files
                    task?.downloadFile()
                }

                override fun onError(task: DownloadTask?, error: Exception) {
                }
            })
    .build()

 ### 2.Start downloading 
   //param isSerial #Is the download task serial。default ：false（parallel downloading） #
   
   OkDownloader.instance.start(true)

 ### 3.pause downloading 
   OkDownloader.instance.pause()

## 4.Queue for all tasks
   //In this queue. You can operate a specific task to start or pause
   
   OkDownloader.instance.getRunnableList()

   ### for example
     OkDownloader.instance.getRunnableList().forEach { innerRunnable ->
                            if (innerRunnable.taskId() == tag) {
                                Log.e("jyj-->", "operation task tag=${innerRunnable.taskTag()}")
                                //innerRunnable.taskStatus() #The status of the task#
                                if (innerRunnable.taskStatus() == DownloadTaskStatus.RUNNING) {
                                    //Pause the task
                                    innerRunnable.pause()
                                }  else {
                                    //Let the task begin
                                    innerRunnable.start()
                                }
                                return@forEach
                            }
                        }
 ## 5.Other configurations

      //Callback message interval (in milliseconds). Default: 50
      OkDownloader.instance.setGlobalInterval(50)

      //Number of task error retries. Default 3 times
      OkDownloader.instance.setRetryTimes(3)
      

## Specific usage examples
  For more complete usage examples, please read
  [demo](app/src/main/java/com/example/application/MainActivity.kt)

 
