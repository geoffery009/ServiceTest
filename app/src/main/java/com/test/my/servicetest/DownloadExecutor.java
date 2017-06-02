package com.test.my.servicetest;


import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Administrator on 2017/6/2 0002.
 */

public class DownloadExecutor {
    public static final String TAG = DownloadExecutor.class.getName();

    private String downloadUrl;
    private String saveFilePath;
    private int threadCount;
    private ExecutorService mDownloadService;

    private int countLength;
    private int mProgress;
    private IDownloadListener mListener;


    public DownloadExecutor(int threadCount, String downloadUrl, String saveFilePath, IDownloadListener mListener) {
        this.threadCount = threadCount;
        this.downloadUrl = downloadUrl;
        this.saveFilePath = saveFilePath;
        this.mDownloadService = Executors.newFixedThreadPool(threadCount);//选择固定个数的线程池
        this.mListener = mListener;
    }

    public DownloadExecutor(String downloadUrl, String saveFilePath, IDownloadListener mListener) {
        this(3, downloadUrl, saveFilePath, mListener);
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * 下载文件
     */
    public void start() {
        new Thread() {
            @Override
            public void run() {//连接资源
                try {
                    URL url = new URL(downloadUrl);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);

                    int code = connection.getResponseCode();

                    if (code == 200) {

                        //获取资源大小
                        int connectionLength = connection.getContentLength();
                        countLength = connectionLength;

                        System.out.println("下载文件总大小：" + connectionLength);

                        //在本地创建一个与资源同样大小的文件来占位
                        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(saveFilePath, DownloadUtil.getFileName(url)), "rw");
                        randomAccessFile.setLength(connectionLength);

                /*
                 * 将下载任务分配给每个线程
                 */
                        int blockSize = connectionLength / threadCount;//计算每个线程理论上下载的数量.

                        for (int threadId = 0; threadId < threadCount; threadId++) {//为每个线程分配任务
                            int startIndex = threadId * blockSize; //线程开始下载的位置
                            int endIndex = (threadId + 1) * blockSize - 1; //线程结束下载的位置
                            if (threadId == (threadCount - 1)) {  //如果是最后一个线程,将剩下的文件全部交给这个线程完成
                                endIndex = connectionLength - 1;
                            }

                            //加入线程池
                            DownloadThread thread = new DownloadThread(threadId, startIndex, endIndex, downloadUrl, saveFilePath, new DownloadThread.IThreadListener() {
                                @Override
                                public void onProgress(int thread, int progress) {

                                    try {
//                                        mProgress = (int) (downloadLength() / (float) countLength * 100);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    Log.d(TAG, "Thread [" + thread + "] progress:" + progress + ", Download progress:" + mProgress);
                                }
                            });
                            mDownloadService.execute(thread);
                        }
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private int downloadLength() throws Exception {
        int downloadLength = 0;
        for (int threadId = 0; threadId < threadCount; threadId++) {
            File file = new File(DownloadUtil.getThreadBlockFileName(saveFilePath, threadId));
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            String index = randomAccessFile.readLine();
            randomAccessFile.close();
            Log.d(TAG, "saved size:" + index + ",total size:" + countLength);
            if (TextUtils.isEmpty(index)) throw new Exception();
            downloadLength += Integer.valueOf(index);
        }

        return downloadLength;
    }

    /**
     * 停止下载
     */
    public void stop() {
        if (mDownloadService != null) {
            mDownloadService.shutdownNow();
        }
    }

    /**
     * 重新下载
     */
    public void restart() throws MalformedURLException {
        stop();
        //删除临时文件
        for (int threadId = 0; threadId < threadCount; threadId++) {
            DownloadUtil.deleteFile(new File(DownloadUtil.getThreadBlockFileName(saveFilePath, threadId)));
        }

        DownloadUtil.deleteFile(new File(saveFilePath, DownloadUtil.getFileName(new URL(downloadUrl))));
    }

    interface IDownloadListener {
        public void onProgress(String url, int progress);
    }
}
