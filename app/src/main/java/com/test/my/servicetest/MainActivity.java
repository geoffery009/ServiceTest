package com.test.my.servicetest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String TEST_URL = "http://pic125.nipic.com/file/20170401/1924849_072639005000_2.jpg";

    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

        startService(new Intent(this, DownloadService.class));
        TEST_URL = "https://desktop.githubusercontent.com/releases/0.5.8-e55db469/GitHubDesktopSetup.exe";

        filePath = getExternalCacheDir().getPath();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.button:
                Intent intent = new Intent();
                intent.setAction(DownloadService.MESSAGE_DOWNLOAD);
                intent.putExtra(DownloadService.COL_DOWNLOAD_URL, TEST_URL);
                intent.putExtra(DownloadService.COL_SAVE_FILE_PATH, filePath);
                sendBroadcast(intent);
                break;

            case R.id.button2:

                Intent stopIntent = new Intent();
                stopIntent.putExtra(DownloadService.COL_DOWNLOAD_URL, TEST_URL);
                stopIntent.setAction(DownloadService.MESSAGE_STOP);
                sendBroadcast(stopIntent);
                break;
        }
    }
}
