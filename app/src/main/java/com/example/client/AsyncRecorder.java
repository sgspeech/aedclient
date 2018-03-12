package com.example.client;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static com.example.client.Client.ERROR_MIC_INIT;

/**
 * Created by speech on 2018-02-19.
 */

public class AsyncRecorder extends AsyncTask<AsyncQueue, byte[], Void> {

    private AudioRecord recorder;
    private AsyncQueue receivers[];

    private int bufferSize;


    private int samplerate = 16000;

    Client client;

    public AsyncRecorder(Client client) {
        super();
        this.client = client;
    }

    protected void onPreExecute() {

        this.bufferSize = AudioRecord.getMinBufferSize(samplerate, CHANNEL_IN_MONO, ENCODING_PCM_16BIT);
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, samplerate, CHANNEL_IN_MONO, ENCODING_PCM_16BIT, this.bufferSize);
    }

    @Override
    protected Void doInBackground(AsyncQueue... asyncQueues) {

        this.receivers = asyncQueues;

        try {
            byte _data[], data[];

            this.recorder.startRecording();
            this.client.onStart();

            while (!isCancelled()) {

                int sizeRead = this.recorder.read((_data = new byte[this.bufferSize]), 0, this.bufferSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != sizeRead) {

                    data = dontTrustMICbytes(_data, sizeRead);
                    publishProgress(data);
                }
            }

            this.recorder.stop();
            this.recorder.release();

        } catch(IllegalStateException e) {
            this.client.cancel(ERROR_MIC_INIT);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate (byte[]... values){

        for( AsyncQueue t : this.receivers){
            if(t != null)
                t.add(values[0]);
        }
    }

    @Override
    protected void onPostExecute (Void result) {

        for( AsyncQueue t : this.receivers){
            if(t != null) {
                t.wakeUp();
                t.setDone();
            }
        }
    }

    @Override
    protected void onCancelled(Void result) {

        Log.d("AsyncRecorder", "onCancelled");

        if(this.receivers != null) {

            for( AsyncQueue t : this.receivers) {

                if(t != null) {
                    t.wakeUp();
                    t.cancel(false);
                }
            }
        }
    }

    private byte[] dontTrustMICbytes(byte _data[], int sizeRead){

        byte data[] = new byte[sizeRead];

        for(int i1=0; i1<sizeRead; i1++)
            data[i1] = _data[i1];

        return data;
    }


}
