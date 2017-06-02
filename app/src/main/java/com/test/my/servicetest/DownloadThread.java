package com.test.my.servicetest;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载文件的模块下载
 * Created by Administrator on 2017/6/2 0002.
 */

//下载的线程
public class DownloadThread extends Thread {
    public static final String TAG = DownloadThread.class.getName();

    private int threadId;
    private int startIndex;
    private int orignalIndex;
    private int endIndex;
    private String path;
    private String targetFilePath;
    private IThreadListener threadListener;

    public DownloadThread(int threadId, int startIndex, int endIndex, String downloadUrl, String targetFilePath, IThreadListener listener) {
        this.threadId = threadId;
        this.startIndex = startIndex;
        this.orignalIndex = startIndex;
        this.endIndex = endIndex;
        this.targetFilePath = targetFilePath;
        this.path = downloadUrl;
        this.threadListener = listener;
    }

    @Override
    public void run() {
        System.out.println("线程" + threadId + "开始下载");
        try {
            //分段请求网络连接,分段将文件保存到本地.
            URL url = new URL(path);

            //创建分块文件
            File downThreadFile = new File(DownloadUtil.getThreadBlockFileName(targetFilePath, threadId));
            RandomAccessFile downThreadStream = null;
            if (downThreadFile.exists()) {//如果文件存在
                downThreadStream = new RandomAccessFile(downThreadFile, "rwd");
                String startIndex_str = downThreadStream.readLine();//获取保存的该块中下载位置点的值

                if (!TextUtils.isEmpty(startIndex_str)) {
                    this.startIndex = Integer.parseInt(startIndex_str) - 1;//设置下载起点
                }

            } else {
                downThreadStream = new RandomAccessFile(downThreadFile, "rwd");
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);

            //设置分段下载的头信息。  Range:做分段数据请求用的。格式: Range bytes=0-1024  或者 bytes:0-1024
            connection.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);

            System.out.println("线程_" + threadId + "的下载起点是 " + startIndex + "  下载终点是: " + endIndex);

            if (connection.getResponseCode() == 206) {//200：请求全部资源成功， 206代表部分资源请求成功

                InputStream inputStream = connection.getInputStream();//获取流
                RandomAccessFile randomAccessFile = new RandomAccessFile(
                        new File(targetFilePath, DownloadUtil.getFileName(url)), "rw");//获取前面已创建的文件.

                randomAccessFile.seek(startIndex);//文件写入的开始位置.


                //将网络流中的文件写入本地
                byte[] buffer = new byte[1024];
                int length = -1;
                int total = 0;//记录本次下载文件的大小

                while ((length = inputStream.read(buffer)) > 0) {

                    randomAccessFile.write(buffer, 0, length);
                    total += length;

                    //将当前现在到的位置保存到文件中
                    downThreadStream.seek(0);
                    downThreadStream.write((startIndex + total + "").getBytes("UTF-8"));

                    //模块进度应该是(现在起始点-原始起始点）/（原始结束点-原始起始点）
                    Log.d(TAG, "startIndex:" + startIndex + ",orignalIndex:" + orignalIndex + ",endIndex:" + endIndex);
                    threadListener.onProgress(threadId, (int) ((startIndex - orignalIndex) + total / (float) (endIndex - orignalIndex) * 100));

                }

                downThreadStream.close();
                inputStream.close();
                randomAccessFile.close();

                cleanTemp(downThreadFile);//删除临时文件

                System.out.println("线程" + threadId + "下载完毕");

            } else {

                System.out.println("响应码是" + connection.getResponseCode() + ". 服务器不支持多线程下载");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //删除线程产生的临时文件
    private synchronized void cleanTemp(File file) {
        file.delete();
    }

    interface IThreadListener {
        public void onProgress(int threadId, int progress);
    }

}


