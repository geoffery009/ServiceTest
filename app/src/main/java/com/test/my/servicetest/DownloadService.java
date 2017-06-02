package com.test.my.servicetest;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadService extends Service {
    public static final String TAG = DownloadService.class.getName();

    public static final String MESSAGE_DOWNLOAD = "message_download";
    public static final String MESSAGE_STOP = "message_stop";

    public static final String COL_DOWNLOAD_URL = "col_download_url";
    public static final String COL_SAVE_FILE_PATH = "col_save_file_path";

    private Map<String, DownloadExecutor> downloadExecutorMap;//下载线管理
    private int max_download_line = 5;//最大下载线个数

    public DownloadService() {
        downloadExecutorMap = new HashMap<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Start service");

        //下载监听
        final IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(MESSAGE_DOWNLOAD);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String url = intent.getStringExtra(COL_DOWNLOAD_URL);
                final String path = intent.getStringExtra(COL_SAVE_FILE_PATH);
                if (downloadExecutorMap.size() <= max_download_line) {

                    if (downloadExecutorMap.get(url) == null) {
                        //开始下载
                        DownloadExecutor executor = new DownloadExecutor(url, path, new DownloadExecutor.IDownloadListener() {
                            @Override
                            public void onProgress(String url, int progress) {
                                Log.d(TAG, "url:" + url + ", progress:" + progress);
                            }
                        });
                        executor.start();
                        downloadExecutorMap.put(url, executor);
                    } else {
                        Log.d(TAG, "下载文件已在列表中");
                    }
                } else {//下载线过多
                    Log.d(TAG, "下载数过多，无法开始下载");
                }
            }
        }, downloadFilter);


        //停止监听
        IntentFilter stopFilter = new IntentFilter();
        downloadFilter.addAction(MESSAGE_STOP);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String url = intent.getStringExtra(COL_DOWNLOAD_URL);
                if (downloadExecutorMap != null) {
                    for (Map.Entry<String, DownloadExecutor> entry : downloadExecutorMap.entrySet()) {
                        if (url.equals(entry.getKey())) {
                            entry.getValue().stop();
                        }
                    }
                }
                Log.d(TAG, "download stop");
            }
        }, stopFilter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
