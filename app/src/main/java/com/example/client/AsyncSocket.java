package com.example.client;


import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;


import static com.example.client.Client.ERROR_CONN_FAILED;

/**
 * Created by speech on 2018-02-19.
 */

public class AsyncSocket extends AsyncQueue {

    Client client;
    private String ip;
    private int port_send;
    private int port_recv;

    Boolean isTalksori;

    public AsyncSocket(Client _client) {

        this.client = _client;

        this.ip = "your_IP";
        this.port_send = 5000;
        this.port_recv = 5001;
    }

    @Override
    protected String doInBackground(AsyncQueue... params) {

        String result = null;

        if(params.length > 0)
            return null;

        try {
            result = TalksoriLoadBalancer();
        }catch(UnknownHostException e) {
            this.client.cancel(ERROR_CONN_FAILED);
        }catch(InterruptedException e) {
        }catch(java.io.UnsupportedEncodingException e) {
            this.client.cancel(ERROR_CONN_FAILED);
        }catch(IOException e) {
            this.client.cancel(ERROR_CONN_FAILED);
        }

        return result;
    }


    private String TalksoriLoadBalancer() throws UnknownHostException, IOException, InterruptedException {

        String result = null;
        {
            Socket socket = new Socket(this.ip, this.port_send);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            while (!isCancelled()) {


                byte[] target = this.queue.peek();

                if (target == null) {

                    if (this.isDone) break;

                    this.takeRest();
                } else {
                    dos.write(target);
                    dos.flush();
                    this.queue.remove();
                }
            }
            socket.close();
        }
        /*{
            Socket socket = new Socket(this.ip, this.port_recv);

            InputStreamReader isr = new InputStreamReader(socket.getInputStream(), "utf-8");
            BufferedReader br = new BufferedReader(isr);

            while (result == null) {

                if (br.ready())
                    result = br.readLine();

                if (isCancelled()) break;
            }
        }*/
        return result;
    }

    @Override
    protected void onCancelled() {

        Log.d("AsyncSocket", "onCancelled");
    }
}
