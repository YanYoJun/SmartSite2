package com.isoftstone.smartsite.model.map.service;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.utils.LogUtils;
import com.isoftstone.smartsite.utils.ToastUtils;

import java.io.File;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

/**
 * Created by zw on 2017/11/10.
 */

public class DownloadAPKService extends Service {

    private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
    private String apk_file_name = "";
    private DownloadReceiver downloadReceiver;
    private String apk_url;
    private long downloadId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            apk_url = intent.getStringExtra("apk_url");
            apk_file_name = "SmartSite" + intent.getStringExtra("apk_version") + ".apk";
            File file = new File(DOWNLOAD_PATH,apk_file_name);
            if(file.exists()){
                ToastUtils.showLong(getString(R.string.download_complete));
                installApk();
            }else{
                ToastUtils.showLong(getString(R.string.downloading));
                downloadNewAPK();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void installApk() {
        File file = new File(
                DOWNLOAD_PATH
                , apk_file_name);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Build.VERSION.SDK_INT>=24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri =
                    FileProvider.getUriForFile(this, "com.isoftstone.smartsite.fileprovider", file);

            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }else{
                intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        }

        startActivity(intent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(downloadReceiver != null){
            this.unregisterReceiver(downloadReceiver);
        }
    }

    private void downloadNewAPK(){
        downloadReceiver = new DownloadReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        this.registerReceiver(downloadReceiver,filter);

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apk_url));
        //设置下载路径为SD卡Download文件夹
        File folder = new File(DOWNLOAD_PATH);
        if(!folder.exists() || !folder.isDirectory()){
            folder.mkdirs();
        }
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("smartsite")) {
                    file.delete();
                }
            }
        }
        request.setDestinationInExternalPublicDir("Download", apk_file_name);
        //通知栏中的标题
        request.setTitle(getString(R.string.notify_title));
        //通知栏中的描述
        request.setDescription("下载" + apk_file_name + "中...");
        request.setMimeType("application/vnd.android.package-archive");
        request.setNotificationVisibility(VISIBILITY_VISIBLE);
        downloadId = downloadManager.enqueue(request);
    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == DownloadManager.ACTION_DOWNLOAD_COMPLETE){
                if(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,0) == downloadId){
                    installApk();
                }
            } else if(intent.getAction() == DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS){
                long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
                DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                //点击通知栏取消下载
                manager.remove(ids);
                ToastUtils.showLong(getString(R.string.cancel_download));
            }
        }
    }
}
