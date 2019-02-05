package com.nobroker.nbgeo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.PriorityBlockingQueue;

public class DownloadService extends Service {

    Context mContext;
    private static final Object OBJ_LOCK = new Object(); //better
    private PriorityBlockingQueue<Integer> mQueue = new PriorityBlockingQueue<>();

    private void addtoQueue(int request) {

        mQueue.add(request);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            synchronized (OBJ_LOCK) {


                if (intent != null && intent.hasExtra("count")) {
                    Log.v("download_executing", "COunt:" + intent.getIntExtra("count", 0) + ",size:" + mQueue.size());
                    for (int i = 1; i < 100; i++) {
                        addtoQueue(i);
                    }

                }

                if (intent.getIntExtra("count",0)==5) {
                    Log.e("download_executing", "executing");

                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.start();

                }


            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onQuit() {
        // do something
        Log.e("download_executing", "COmpleteddddddddddddddddd");

    }

    class DownloadTask extends Thread {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            Log.e("download_executing", "executing run size:in Before " + mQueue.size());

            while (true) {
                try {

                    mQueue.take();

                    if (mQueue != null && mQueue.isEmpty()) {
                        onQuit();
                    }
                    Log.e("download_executing", "===========executing run size:in run after " + mQueue.size());

                } catch (Exception e) {

                }
            }


        }
    }

}
