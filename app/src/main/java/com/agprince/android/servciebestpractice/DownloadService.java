package com.agprince.android.servciebestpractice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask mDownloadTask;
    private String mDownloadUrl;
    private DownloadListener mListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("downloading....",progress));
        }

        @Override
        public void onSuccess() {
            mDownloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("downloaded success! ",-1));

        }

        @Override
        public void onFailed() {
            mDownloadTask=null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("downloaded failed!",-1));

        }

        @Override
        public void onCanceled() {
            mDownloadTask=null;
            stopForeground(true);
           // getNotificationManager().notify(1,getNotification("download cancel",-1));
        }

        @Override
        public void onPaused() {
            mDownloadTask=null;
            stopForeground(true);
        }
    };

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;

    }
    private DownloadBinder mBinder =new  DownloadBinder();

    public class DownloadBinder extends Binder {
        public void startDownload(String url){
            mDownloadTask = new DownloadTask(mListener);
            mDownloadUrl = url;
            mDownloadTask.execute(mDownloadUrl);
           startForeground(1,getNotification("downloading....",0));


        }
        public void pauseDownload(){
            if(mDownloadTask!=null){
                mDownloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if(mDownloadTask!=null){
                mDownloadTask.cancelDownload();
            }else {
                if (mDownloadUrl!=null){
                    String fileName = mDownloadUrl.substring(mDownloadUrl.lastIndexOf("/"));
                    String dictrory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(dictrory+fileName);
                    if(file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);

                }
            }
        }

    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("870905", "agtest", NotificationManager.IMPORTANCE_DEFAULT);
            builder = new NotificationCompat.Builder(this, channel.getId());
        }else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        if (progress > 0) {
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }


}
