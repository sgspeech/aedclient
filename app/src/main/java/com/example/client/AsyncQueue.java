package com.example.client;

import android.os.AsyncTask;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by speech on 2018-02-19.
 */

public abstract class AsyncQueue extends AsyncTask<AsyncQueue, byte[], String> {

    protected int count = 0;
    protected boolean isDone = false;
    protected BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]>(1000);

    public boolean add(byte[] data) {

        boolean value = this.queue.add(data);
        if (value) {
            this.count++;
            this.wakeUp();
        }

        return value;
    }

    protected void takeRest() throws InterruptedException {

        synchronized(this) {
            wait();
        }
    }

    public void wakeUp() {

        synchronized(this) {
            notify();
        }
    }

    @Override
    protected void onCancelled(String res) {

        this.wakeUp();
    }

    public void setDone() {

        this.wakeUp();
        this.isDone = true;
    }
}
