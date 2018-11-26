package org.apache.log4j;

import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class MyDailyRollingFileAppender extends DailyRollingFileAppender {

    private String datePattern = "";//日期格式
    private String dateStr = "";//文件后面的日期
    private String isCleanLog = "true";//是否清日志
    private String logPath = "D:/zip/";//日志路径，将压缩这个路径下的日志
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    /**
     * 设置日期格式
     */
    public void setDatePattern(String datePattern) {
        if (null != datePattern && !"".equals(datePattern)) {
            super.setDatePattern(datePattern);
            this.datePattern = datePattern;
        }
    }

    /**
     * 获取日期格式
     */
    public String getDatePattern() {
        return this.datePattern;
    }

    private Map<String, List<File>> logFileMap = new HashMap<>();

    void rollOver() throws IOException {
        super.rollOver();
        getAllLogs();
        for (String key : logFileMap.keySet()) {
            List<File> logFileList = logFileMap.get(key);
            if (logFileList.size() > 0) {
                zipFiles(logFileList, new File(logPath + key + ".zip"));
                for (int i = 0; i < logFileList.size(); i++) {
                    boolean deleted = logFileList.get(i).delete();
                    if (deleted) {
                        logFileMap.get(key).remove(i);
                    }
                }
            }
        }
    }

    /**
     * 获取文件名中的日期
     */
    private String getFileDateSuffix(File file) {
        String fileName = file.getName();
        //备份日志格式为 log-info.log.2018-11-20.log
        return fileName.substring(fileName.lastIndexOf(".log.") + 5, fileName.length() - 4);
    }

    private void getAllLogs() {
        File allFile = new File(logPath);
        File[] tempList = allFile.listFiles();
        if (tempList != null) {
            for (File aTempList : tempList) {
                if (aTempList.isFile()) {
                    String suffix = getFileDateSuffix(aTempList);
                    try {
                        if (formatter.format(formatter.parse(suffix)).equals(suffix)) {
                            System.out.println("待压缩文件：" + aTempList);
                            if (logFileMap.get(suffix) == null) {
                                List<File> logFileList = new ArrayList<>();
                                logFileList.add(aTempList);
                                logFileMap.put(suffix, logFileList);
                            } else {
                                logFileMap.get(suffix).add(aTempList);
                            }
                        }
                    } catch (ParseException ignored) {
                    }
                }
            }
        }
    }

    /**
     * 压缩多个文件成一个zip文件
     *
     * @param srcfile：源文件列表
     * @param zipFile：压缩后的文件
     */
    private void zipFiles(List<File> srcfile, File zipFile) {
        byte[] buf = new byte[1024];
        try {
            if (zipFile.exists()) {//压缩文件已经存在，则只能单个追加
                for (File file : srcfile) {
                    zip(zipFile, file);
                }
            } else {//不存在则新建
                //ZipOutputStream类：完成文件或文件夹的压缩
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
                for (File logFile : srcfile) {
                    FileInputStream in = new FileInputStream(logFile);
                    out.putNextEntry(new ZipEntry(logFile.getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 追加单个文件到zip
     *
     * @param zipFile    追加到该文件
     * @param sourceFile 待压缩的单文件
     */
    private void zip(File zipFile, File sourceFile) throws Exception {
        if (zipFile.exists()) {// 添加到已经存在的压缩文件中
            File tempFile = new File(zipFile.getAbsolutePath() + ".tmp");
            // 创建zip输出流
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(tempFile), Charset.forName("UTF-8"));
            // 创建缓冲输出流
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(zipOutStream);
            ZipFile zipOutFile = new ZipFile(zipFile);

            Enumeration<? extends ZipEntry> entries = zipOutFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                System.out.println("copy: " + entry.getName());
                zipOutStream.putNextEntry(entry);
                if (!entry.isDirectory()) {
                    write(zipOutFile.getInputStream(entry), bufferOutStream);
                }

                zipOutStream.closeEntry();
            }
            zipOutFile.close();//记得关闭zip文件，否则后面无法删除原始文件
            ZipEntry entry = new ZipEntry(sourceFile.getName());
            // 添加实体
            zipOutStream.putNextEntry(entry);
            BufferedInputStream bufferInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            write(bufferInputStream, bufferOutStream);
            //最后关闭输出流
            bufferOutStream.close();
            zipOutStream.close();
            boolean flag = zipFile.delete();
            if (flag) {
                tempFile.renameTo(zipFile);
            } else {
                System.out.println("删除文件失败。");
            }
        } else {// 新创建压缩文件
            // 创建zip输出流
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(zipFile), Charset.forName("UTF-8"));
            // 创建缓冲输出流
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(zipOutStream);
            // 创建压缩文件实体
            ZipEntry entry = new ZipEntry(sourceFile.getName());
            // 添加实体
            zipOutStream.putNextEntry(entry);
            // 创建输入流
            BufferedInputStream bufferInputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            write(bufferInputStream, bufferOutStream);
            //最后关闭输出流
            bufferOutStream.close();
            zipOutStream.close();
        }
    }

    private static void write(InputStream inputStream, OutputStream outStream) throws IOException {
        byte[] data = new byte[4096];
        int length = 0;
        while ((length = inputStream.read(data)) != -1) {
            outStream.write(data, 0, length);
        }
        outStream.flush();//刷新输出流
        inputStream.close();//关闭输入流
    }

    private String getDateStr(File file) {
        if (file == null) {
            return "null";
        }
        return file.getName().replaceAll(new File(fileName).getName(), "");
    }


    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getIsCleanLog() {
        return isCleanLog;
    }

    public void setIsCleanLog(String isCleanLog) {
        this.isCleanLog = isCleanLog;
    }
}