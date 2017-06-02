package com.test.my.servicetest;

import java.io.File;
import java.net.URL;

/**
 * Created by Administrator on 2017/6/2 0002.
 */

public class DownloadUtil {

    //分块文件名定义规则
    public static String getThreadBlockFileName(String targetFilePath, int threadId) {
        String filename = targetFilePath;
        File file = new File(targetFilePath);
        filename = file.getName();
        return targetFilePath + File.separator + filename.replaceAll(".", "_") + "_downThread_" + threadId + ".dt";
    }

    //获取下载文件的名称
    public static String getFileName(URL url) {
        String filename = url.getFile();
        return filename.substring(filename.lastIndexOf("/") + 1);
    }

    public static void deleteFile(File filePath) {
        if (filePath.exists()) {
            filePath.delete();
        }
    }
}
