package com.example.ihavenoidea.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView textView;
    StringBuilder keyBuilder = new StringBuilder();

    SoundPool sp;
    boolean moveTaken = false;
    int soundfile;

    HashMap<String, String> buttonMapping = new HashMap<String, String>();
    int count = 0;
    SensorEvent oldEvent = null;

    String lastDirection = "NONE";

    float [] history = new float[3];
    int[] direction = {0};
    String[] pressed = {"0", "0", "0"};
    int[] oldDirection = {0};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        buttonMapping.put("000", "1");
        buttonMapping.put("001", "2");
        buttonMapping.put("010", "3");
        buttonMapping.put("011", "4");
        buttonMapping.put("100", "5");
        buttonMapping.put("101", "6");
        buttonMapping.put("110", "7");
        buttonMapping.put("111", "8");

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundfile = sp.load(this, R.raw.airhorn, 0);
    }

    long lastUpdate = -1;
    int rCount = 0;
    int lCount = 0;
    int zCount = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        long currTime = System.currentTimeMillis();
        if (currTime - lastUpdate < 100) {
            lastUpdate = currTime;
            return;
        }
        float xChange = history[0] - event.values[0];
        history[0] = event.values[0];

        if (xChange > 3) {
            direction[0] = -1;
        } else if (xChange < -3) {
            direction[0] = 1;
        }


        if (findViewById(R.id.greenButton).isPressed()) {
            pressed[0] = "1";
        } else {
            pressed[0] = "0";
        }
        if (findViewById(R.id.redButton).isPressed()) {
            pressed[1] = "1";
        } else {
            pressed[1] = "0";
        }
        if (findViewById(R.id.yellowButton).isPressed()) {
            pressed[2] = "1";
        } else {
            pressed[2] = "0";
        }

        if (direction[0] == 1) {
            rCount++;
        }
        if (direction[0] == -1) {
            lCount++;
        }

        if (rCount > 100) {
            moveTaken = true;
            oldDirection[0] = 1;
            lCount = 0;
        }
        else if (lCount > 100) {
            rCount = 0;
            oldDirection[0] = -1;
            moveTaken = true;
        } else {
            moveTaken = false;
        }

    if (moveTaken && oldDirection[0] != direction[0]) {
        oldDirection[0] = 0;
        rCount = 0;
        lCount = 0;
        //playSound
        //pressed[0] + pressed[1] + pressed[2]//
    }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }
}