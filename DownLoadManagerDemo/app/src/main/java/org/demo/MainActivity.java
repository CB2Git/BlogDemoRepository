package org.demo;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView mTextView;

    private Button mDownProgress;

    private String mApkUri = "http://gdown.baidu.com/data/wisegame/9d4083325b73f6d7/fennudexiaoniaozhongwenban_22200603.apk";

    private static final Uri DOWNLOAD_URI = Uri.parse("content://downloads/my_downloads");

    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.main_download_info);
        mDownProgress = (Button) findViewById(R.id.main_download_progress);
    }

    //开始下载
    public void beginDownload(View view) {
        //创建一个下载对象
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mApkUri));
        //DownloadManager.Request.NETWORK_MOBILE
        //DownloadManager.Request.NETWORK_WIFI
        //设置允许下载的网络条件，分为流量以及WIFI,默认为全部网络都允许下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        //设置下载的时候时候显示在通知栏上面，分为如下几种
        //DownloadManager.Request.VISIBILITY_HIDDEN 永远不显示，不过需要权限android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
        //DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED 下载完成的时候显示
        //DownloadManager.Request.VISIBILITY_VISIBLE 下载中显示(默认)
        //可以组合使用
        //| DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);


        //设置是否允许使用漫游流量，默认是允许
        //request.setAllowedOverRoaming(true);

        //设置通知栏标题
        request.setTitle("标题");
        //设置通知栏描述
        request.setDescription("不可描述");

        //设置Mime类型，默认为下面这个
        //request.setMimeType("application/vnd.android.package-archive");
        //request.setDestinationUri();


        //下载到外置SD卡的DownLoad目录下
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "a.apk");

        //下载到外置缓存路径，当本应用被卸载的时候会被一同删除
        File file = new File(getExternalCacheDir(), "a.apk");
        request.setDestinationUri(Uri.fromFile(file));

        //文件是否允许被MediaScanner扫描，默认为false
        //request.allowScanningByMediaScanner();

        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        id = dm.enqueue(request);
        showInfo("Download id = " + id);

        //注册下载完毕监听，以及点击通知栏监听
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(myBroadcastReceiver, filter);
    }

    /**
     * 接收通知栏点击事件以及下载完成事件
     */
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                showInfo("点击了通知栏");
                stopDownload(null);
            }
            //当没有下载完成，通过remove删除掉任务，也会发出完成广播
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                showInfo("下载完毕");
            }
        }
    };


    /**
     * 停止下载，已经下载的部分文件也会被一起删除
     */
    public void stopDownload(View view) {
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        dm.remove(id);
        showInfo("Remove Download id = " + id);
    }

    /**
     * 监听下载进度
     * 下载信息全部保存在/data/data/com.android.providers.downloads/databases/downloads.db中
     * 所以我们可以直接使用ContentObserver监听其(content://downloads/my_downloads)数据变化，然后查询出已经下载的字节数和总字节数
     */
    public void Progresslistener(View view) {
        getContentResolver().registerContentObserver(DOWNLOAD_URI, true, mContentObserver);
    }

    private ContentObserver mContentObserver = new ContentObserver(null) {

        /**
         * 这个在非主线程
         */
        public void onChange(boolean selfChange, Uri uri) {
            Log.d(TAG, "selfChange=" + selfChange + ", uri=" + uri);
            long id = -1;
            try {
                id = Long.parseLong(uri.getLastPathSegment());
                DownloadInfo downinfo = new DownloadInfo(MainActivity.this);
                downinfo.query((int) id);
                Log.i(TAG, "onChange: downinfo" + downinfo.toString());
            } catch (NumberFormatException e) {
                Log.d(TAG, "Unknown uri received!");
            }
            Log.i(TAG, "id = " + id);
        }
    };

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(myBroadcastReceiver);
            getContentResolver().unregisterContentObserver(mContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void showInfo(View view) {
        DownloadInfo downinfo = new DownloadInfo(this);
        downinfo.query((int) id);
        Log.i(TAG, "onChange: downinfo```" + downinfo.toString());
    }

    public void pause(View view) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        //1 停止下载
        values.put("control", 1);
        //Value of {@link #COLUMN_STATUS} 193 pause by app
        values.put("status", 193);
        Uri temp = ContentUris.withAppendedId(Uri.parse("content://downloads/my_downloads"), id);
        int nRet = resolver.update(temp, values, null, null);
        if (nRet >= 1) {
            showInfo("暂停ok");
        } else {
            showInfo("暂停error");
        }

    }

    public void resume(View view) {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        //0 This download is allowed to run.
        values.put("control", 0);
        //Value of {@link #COLUMN_STATUS} when the download is currently running. 192
        values.put("status", 192);
        Uri temp = ContentUris.withAppendedId(Uri.parse("content://downloads/my_downloads"), id);
        int nRet = resolver.update(temp, values, null, null);
        if (nRet >= 1) {
            showInfo("开始ok");
        } else {
            showInfo("开始error");
        }
    }

    private void showInfo(String info) {
        mTextView.setText(mTextView.getText() + "\n" + info);
    }
}