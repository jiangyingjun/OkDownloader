# OkDownloader
这是一个非常简单的android多任务文件下载程序


###特点：

####1您可以随意启动或暂停任何任务。（无论是串行下载还是并行下载）

####2可断点下载文件。（无需额外使用sqlite来记录下载信息）

####3下载扩展名可能存在不完整的下载文件


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
  
