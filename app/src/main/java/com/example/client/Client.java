package com.example.client;

import android.app.Activity;
import android.util.Log;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

/**
 * Created by speech on 2018-02-19.
 */

public class Client {

    public static int ERROR_MIC_INIT = 1;
    public static int ERROR_CONN_FAILED = 2;
    public static int ERROR_CANCELLED = 3;
    public static int ERROR_TIMEOUTED = 4;


    private Activity parent;
    private Listener listener;

    AsyncRecorder current_recorder = null;
    AsyncBuffer resizer = null;
    AsyncSocket mySocket = null;
    AsyncResult output = null;


    public Client(Activity _activity, Listener _listener) {

        if((this.parent = _activity) == null || ((this.listener = _listener)== null))
            throw new java.security.InvalidParameterException();
    }

    public void start() {

        this.mySocket = new AsyncSocket(this);
        this.mySocket.executeOnExecutor(THREAD_POOL_EXECUTOR);

        this.resizer = new AsyncBuffer(this);
        this.resizer.executeOnExecutor(THREAD_POOL_EXECUTOR, this.mySocket);

        this.current_recorder = new AsyncRecorder(this);
        this.current_recorder.executeOnExecutor(THREAD_POOL_EXECUTOR, this.resizer);

        this.output = new AsyncResult(this);
        this.output.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    protected void cancel(boolean socket, boolean resizer, boolean recorder, boolean output) {

        if(socket && this.mySocket != null)
            this.mySocket.cancel(false);
        if(resizer && this.resizer != null)
            this.resizer.cancel(false);
        if(recorder && this.current_recorder != null)
            this.current_recorder.cancel(false);
    }

    protected void cancel(int error_code) {

        if(error_code == ERROR_CONN_FAILED)
            this.cancel(true, true, true, true);
        else if(error_code == ERROR_MIC_INIT)
            this.cancel(true, true, false, true);
        else if(error_code == ERROR_CANCELLED)
            this.cancel(true, true, true, true);
        else if(error_code == ERROR_TIMEOUTED)
            this.cancel(true, true, true, true);

        this.onError(error_code);
    }

    public void cancel() {

        this.cancel(ERROR_CANCELLED);
    }

    void onStart() {

        this.parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onStart();
            }
        });
    }

    void onError(final int e) {

        Log.d(this.toString(), Integer.toString(e));
        this.parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onError(e);
            }
        });
    }

    void onResult(final String s) {

        parent.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onResult(s);
            }
        });
    }
}
