package com.example.client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by speech on 2018-02-22.
 */

public class AsyncResult extends AsyncTask<Void, String, String> {


    Client client;

    String result = "";
    Socket socket = null;

    private final String ip = "Your_IP";
    private final int port = 5001;

    AsyncResult(Client client) {

        this.client = client;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(String... values) {

        client.onResult(result);
    }

    @Override
    protected void onPostExecute(String s) {

        client.onResult(s);
    }

    @Override
    protected String doInBackground(Void... voids) {

        try {
            while(socket ==null || isCancelled()) {
                try {
                    socket = new Socket(ip, port);
                    System.out.println(socket);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            while (!isCancelled()) {
                System.out.println("Start receive result");
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                System.out.println(dis.available() < 0);
                try {
                    this.result = dis.readUTF();

                    publishProgress(result);
                    System.out.println(result);
                    Log.d("AsyncResult", result);}
                catch (Exception e)
                {
                    System.out.println(e);
                    //continue;
                }
            }

            socket.close();

        } catch (Exception e) {}

        return result;
    }

    @Override
    protected void onCancelled() {

        Log.d("AsyncResult", "onCancelled");
    }
}
