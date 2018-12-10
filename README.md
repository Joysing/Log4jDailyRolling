# Log4jDailyRolling
当你把log4j分类输出时，比如debug输出在debug.log，error输出在error.log，可以用这个方法把每天生成的debug.log和error.log压缩成一个zip文件

log4j压缩使用方法
----
自定义了一个类org.apache.log4j.MyDailyRollingFileAppender
这个类继承了org.apache.log4j.DailyRollingFileAppender（注意我自定义的类多了个“My”）

**❤WARNING❤：如果你只想使用，不想添加更多高级的功能，可以忽略下面第1点和第2点。**

1.DailyRollingFileAppender的机制是：
----
生成log的时候，检测之前保存的log文件的最后修改日期，

如果不是今天，就把之前的重命名为xxx.log.2018-11-26这种格式，然后再把今天的新建一个xxx.log写进去。

其中2018-11-26是xxx.log的最后修改日期。


2.我的MyDailyRollingFileAppender思路
----
继承DailyRollingFileAppender的机制是类，就可以利用这个机制，在有新的xxx.log.2018-11-26生成的时候，把xxx.log.2018-11-26压缩到2018-11-26.zip并且删除xxx.log.2018-11-26。

logPath是我添加的参数，每个log文件都要写成相同的logPath才会压缩在一起。

如果你想添加其他自定义参数，记得要添加set方法，比如我的setLogPath()，才可以在xml文件中使用该参数（比如\<param name="logPath" value="D:/cccc/logs/aaa/" />）

3.使用的时候注意：
----
0. 压缩好的日志文件不要直接打开，复制出来再打开。避免文件正在压缩你却打开了。
1. MyDailyRollingFileAppender一定要在org.apache.log4j包下面，自己新建一个就行。
2. 设置\<param name="logPath" value="D:/cccc/logs/aaa/" />，使D:/cccc/logs/aaa/下面的日志文件压缩。
3. 一定注意\<appender name="FILE-DEBUG" class="org.apache.log4j.MyDailyRollingFileAppender">中的class是自定义的MyDailyRollingFileAppender而不是官方的DailyRollingFileAppender。
4. 其他配置都和官方的DailyRollingFileAppender配置一模一样。如果配置无效，尝试第5点。
5. 假设\<param name="DatePattern" value="'.'yyyy-MM-dd'.log'" />无效，这样解决：

        在MyDailyRollingFileAppender新建一个datePattern参数，

        同时添加public void setDatePattern(String datePattern)方法，

        方法体里面写super.setDatePattern(datePattern);就可以了。
