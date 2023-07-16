package com.gncbrown.GetMeBack.Utilities;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BackgroundTask {
    private ExecutorService executors = null;

    public BackgroundTask() {
        this.executors = Executors.newSingleThreadExecutor();
    }

    private void startBackground() {
        onPreExecute();
        executors.execute(new Runnable() {
            @Override
            public void run() {
                doInBackground();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute();
                    }
                });
            }
        });
    }

    public void execute() {
        startBackground();
    }

    public void shutdown() {
        executors.shutdown();
    }

    public boolean isShutdown() {
        return executors.isShutdown();
    }

    public abstract void onPreExecute();

    public abstract void doInBackground();

    public abstract void onPostExecute();
}