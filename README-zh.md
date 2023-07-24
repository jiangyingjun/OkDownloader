# OkDownloader
这是一个非常简单的android多任务文件下载程序


### 特点：

#### 1您可以随意启动或暂停任何任务。（无论是串行下载还是并行下载）

#### 2可断点下载文件。（无需额外使用sqlite来记录下载信息）

#### 3未完成的下载文件 以扩展名 .downloading 的文件存在


## 开始使用

###  1. 将JitPack存储库添加到项目根目录下的构建文件中

allprojects {
 repositories {
   ...
   maven { url 'https://jitpack.io' }
  }
}

### Step 2. 添加依赖

dependencies {
 implementation 'com.github.jiangyingjun:OkDownloader:1.0.1'
}


##用户手册

###1.构建下载任务


    RealDownloadTask.Builder()
     //网络文件下载地址
    .downloadUrl(url)
    //文件下载目标路径
    .localPath(path)  
    //自定义任务 id
    .setTag(tag)   
    //任务监听 @参数 notifyInUI （true:回调到主线程。false:回调到任务执行线程） #任务回调默认到任务执行线程#
    .listener(false,object :OnDownloadListener{ 
                override fun onStart(task: DownloadTask) {
                }

                override fun onProcess(task: DownloadTask?, process: Long, total: Long) {
                }

                override fun onPause(task: DownloadTask) {
                }

                override fun onComplete(task: DownloadTask?) {
       		         //完成下载任务的下载文件
                    task?.downloadFile()
                }

                override fun onError(task: DownloadTask?, error: Exception) {
                }
            })
    .build()

### 2.开始下载
//参数 isSerial#是下载任务的序列号默认值：false（并行下载）#

OkDownloader.instance.start(true)

### 3.暂停下载
 OkDownloader.instance.pause()

### 4.所有任务的队列
//在这个队列中。您可以操作特定任务 启动或暂停

OkDownloader.instance.getRunnableList()

#### 示例：
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
### 5.其他配置
  //任务回调间隔 (in milliseconds). 默认: 50毫秒
  
  OkDownloader.instance.setGlobalInterval(50)

  //任务错误重试次数. 默认 3次
  
  OkDownloader.instance.setRetryTimes(3)

### 具体使用示例
更多使用示例，请阅读 [demo](app/src/main/java/com/example/application/MainActivity.kt)
 
