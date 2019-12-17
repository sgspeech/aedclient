package com.example.client;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    //final int DataLen = 20;

    Client client;
    Button startBtn, stopBtn;
    TextView textView;


    ArrayList<String> serverData = new ArrayList<String>();
    String[] serverResult = new String[30];

    BarChart barChart;
    ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
    ArrayList<String> labels = new ArrayList<String>();

    BarDataSet barDataSet;
    BarData barData;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startBtn = (Button)findViewById(R.id.Button1);
        stopBtn = (Button)findViewById(R.id.Button2);
        textView = (TextView)findViewById(R.id.textView);

        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText("Result");

        for (int i=0; i<30; i++) {
            serverResult[i] = "0";
        }

        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    float[] a = new float[30];

                    int sum = 0;
                    for (int i=0; i<30; i++) {
                        a[i] = Integer.parseInt(serverResult[i]);
                        sum += a[i];
                    }

                    /* 정규화 */
                    for (int i=0; i<30; i++)
                        a[i] /= sum;

                    chartUpdata(a);
                }
            }
        };

        initBarView();

        checkPermission();

        Listener listener = new Listener() {

            @Override
            public void onStart() {

                // TODO : Add Progressbar
            }

            @Override
            public void onError(final int e) {

                // TODO : MakeToast of Error
            }

            @Override
            public void onResult(String s) {
                try {
                    textView.setText("");

                    String[] temp = new String[2];
                    System.out.println("1." + s);
                    temp = s.split("\\n");      // '\n'로 한번 파싱
                    System.out.println("2." + temp[0]);
                    serverData.add(0, temp[0]);

                    serverResult = temp[1].split("\\s");    // ' '로 파싱

                    for (String str : serverData)
                        textView.append("\n" + str);
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
            }

        };

        client = new Client(this, listener);
    }
    // 최신 버전의 안드로이드에 대하여, 권한 획득을 위한 mainactivity의 method입니다.
    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    ) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    // Explain to the user why we need to write the permission.
                    Toast.makeText(this, "마이크 권한 획득", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE},
                        0);

                // MY_PERMISSION_REQUEST_STORAGE is an
                // app-defined int constant

            } else {
                // 다음 부분은 항상 허용일 경우에 해당이 됩니다.
                //
            }
        }
    }

    public void onStartBtnClicked(View v) {

        textView.setText("");
        client.start();
        viewThreadStart();
    }

    public void onStopBtnClicked(View v) {

        client.cancel();
        Toast.makeText(getApplicationContext(), "Stop Recording", Toast.LENGTH_SHORT).show();
    }

    private void initBarView() {

        barChart = (BarChart) findViewById(R.id.chart);
        chartInit();
    }

    private void chartInit() {

        barChart.getAxisLeft().setAxisMaxValue(1f);
        barChart.getAxisLeft().setAxisMinValue(0f);
        barChart.getAxisLeft().setMaxWidth(30);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.setDrawGridBackground(false);
        barChart.setMaxVisibleValueCount(30);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setSpaceBetweenLabels(0);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.setDrawValueAboveBar(false);
        barChart.getXAxis().setTextSize(8);
        barChart.setDescription("");
        barChart.getXAxis().setLabelRotationAngle(45);

        labels.add("air_conditioner"); labels.add("car_horn"); labels.add("children_playing"); labels.add("dog_bark"); labels.add("drilling");
        labels.add("engine_idling"); labels.add("gun_shot"); labels.add("jackhammer"); labels.add("siren"); labels.add("street_music");
        labels.add("baby_cry"); labels.add("boiling"); labels.add("rain"); labels.add("river"); labels.add("wind");
        labels.add("bird"); labels.add("car_roads"); labels.add("cow"); labels.add("crowd"); labels.add("sea_wave");
        labels.add("ship"); labels.add("horse"); labels.add("city_center"); labels.add("train"); labels.add("cafe/restaurant");
        labels.add("grocery_store"); labels.add("park"); labels.add("metro_station"); labels.add("office"); labels.add("forest");


        entries.add(new BarEntry(0, 0)); entries.add(new BarEntry(0, 1)); entries.add(new BarEntry(0, 2));
        entries.add(new BarEntry(0, 3)); entries.add(new BarEntry(0, 4)); entries.add(new BarEntry(0, 5));
        entries.add(new BarEntry(0, 6)); entries.add(new BarEntry(0, 7)); entries.add(new BarEntry(0, 8));
        entries.add(new BarEntry(0, 9)); entries.add(new BarEntry(0, 10)); entries.add(new BarEntry(0, 11));
        entries.add(new BarEntry(0, 12)); entries.add(new BarEntry(0, 13)); entries.add(new BarEntry(0, 14));
        entries.add(new BarEntry(0, 15)); entries.add(new BarEntry(0, 16)); entries.add(new BarEntry(0, 17));
        entries.add(new BarEntry(0, 18)); entries.add(new BarEntry(0, 19)); entries.add(new BarEntry(0, 20));
        entries.add(new BarEntry(0, 21)); entries.add(new BarEntry(0, 22)); entries.add(new BarEntry(0, 23));
        entries.add(new BarEntry(0, 24)); entries.add(new BarEntry(0, 25)); entries.add(new BarEntry(0, 26));
        entries.add(new BarEntry(0, 27)); entries.add(new BarEntry(0, 28)); entries.add(new BarEntry(0, 29));

        barDataSet = new BarDataSet(entries,"");

        barData = new BarData(labels, barDataSet);
        barDataSet.setColor(Color.GREEN);

        barChart.setData(barData);
        barChart.animateY(500);
    }

    private void chartUpdata(float[] dataset) {

        entries.clear();

        for (int i=0; i<30; i++)
            entries.add(new BarEntry(dataset[i], i));

        barChart.notifyDataSetChanged();
        barChart.invalidate();
    }

    class ViewThread extends Thread {

        @Override
        public void run() {

            while(true) {
                handler.sendEmptyMessage(0);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void viewThreadStart() {

        ViewThread thread = new ViewThread();

        thread.setDaemon(true);
        thread.start();
    }
}

