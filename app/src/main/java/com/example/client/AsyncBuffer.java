package com.example.client;

import android.util.Log;

/**
 * Created by speech on 2018-02-19.
 */

public class AsyncBuffer extends AsyncQueue {

    Client client;

    private AsyncQueue receivers[];


    public AsyncBuffer(Client _client) {

        super();
        this.client = _client;
    }

    @Override
    protected String doInBackground(AsyncQueue... params) {

        this.receivers = params;

        try {
            while(!isCancelled()) {

                byte[] target = this.queue.peek();

                if(target == null) {

                    if(this.isDone)
                        break;

                    this.takeRest();
                }
                else
                    ResizeAndSend(target);
            }
        } catch (InterruptedException e) {}

        return null;
    }

    @Override
    protected void onPostExecute (String result){

        for(AsyncQueue t : this.receivers) {

            if(t != null) {

                t.wakeUp();
                t.setDone();
            }
        }
    }

    @Override
    protected void onProgressUpdate (byte[]... values) {

        for(AsyncQueue t : this.receivers) {

            if(t != null)
                t.add(values[0]);
        }
    }

    @Override
    protected void onCancelled(String result) {

        Log.d("AsyncBuffer", "onCancelled");

        if(this.receivers != null) {

            for(AsyncQueue t : this.receivers) {

                if(t != null) {
                    t.wakeUp();
                    t.cancel(false);
                }
            }
        }
    }

    private byte[] rest = null;
    private final static int damn3200 = 3200;

    static class MyByteUtils{

        /*if larger than 3200bytes : Divde int out1(3200bytes) and out2 */

        public static void Divide(byte[] in, byte[] out1, byte[] out2) {

            for(int i=0; i<in.length; i++) {

                if (i < damn3200)
                    out1[i] = in[i];
                else
                    out2[i-damn3200] = in[i];
            }
        }

        /* if small than 3200bytes : Combine out1 and out2 */

        public static void Link(byte[] a, byte[] b, byte[] out1, byte[] out2) {

            for(int i=0; i<a.length; i++)
                out1[i] = a[i];

            for(int i=a.length, j=0, k=-1; j<b.length; j++) {

                if(i >= damn3200)
                    out2[++k] = b[j];
                else
                    out1[i++] = b[j];
            }
        }
    }

    private void ResizeAndSend(byte[] buffer){
        byte[] temp, temp2;  // now, later

        if(this.rest != null) {

            // 1. if rest available
            if(this.rest.length > damn3200) {

                // 1-A. Rest already got more then buffer size. (can send 1+ times)
                MyByteUtils.Divide (this.rest, (temp = new byte[damn3200]), (temp2 = new byte[this.rest.length - damn3200]));

                Send(temp);
                this.rest = temp2;

                // retry with rest, we didn't touched the buffer.
                ResizeAndSend(buffer);
            }
            else if(this.rest.length == damn3200) {

                // 1-B. Rest can fitted as buffer size.
                Send(this.rest);
                this.rest = null;

                // retry with buffer.
                ResizeAndSend(buffer);
            }
            else {
                if(buffer != null) {

                    // 1-C. Rest+Buffer -> send
                    if(this.rest.length + buffer.length > damn3200) {

                        // 1-C-a. New rest
                        MyByteUtils.Link(this.rest, buffer, (temp = new byte[damn3200]),
                                (temp2 = new byte[this.rest.length + buffer.length - damn3200]));

                        Send(temp);
                        this.queue.remove();  // buffer-used
                        this.rest = temp2;

                        // retry without buffer, we already used it.
                        ResizeAndSend(null);
                    }
                    else if(this.rest.length + buffer.length == damn3200) {

                        // 1-C-b. Affordable
                        MyByteUtils.Link(this.rest, buffer, (temp = new byte[damn3200]), null);

                        Send(temp);

                        this.queue.remove();  // buffer-used
                        this.rest = null;
                    }
                    else {

                        // 1-C-c. Wait.
                        MyByteUtils.Link(this.rest, buffer, (temp = new byte[this.rest.length + buffer.length]), null);

                        this.queue.remove();  // buffer-used
                        this.rest = temp;
                    }
                }
            }
        }
        else {

            // 2. if not
            if(buffer != null) {

                // 2-A. Over the buffer size.
                if (buffer.length > damn3200) {

                    MyByteUtils.Divide(buffer, (temp = new byte[damn3200]), (this.rest = new byte[buffer.length - damn3200]));

                    this.queue.remove();  // buffer-used
                    Send(temp);
                    ResizeAndSend(null);

                }
                else if (buffer.length == damn3200) {

                    // 2-B. Expected!
                    Send(this.queue.remove());  // buffer-used
                }
                else  // 2-C. Less then expected.
                    rest = this.queue.remove();  // buffer-used
            }
        }
    }

    private void Send(byte[] buffer) {

        if(buffer.length <= 0)
            return;

        this.publishProgress(buffer);
    }
}
