package com.example.ihavenoidea.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView textView;
    StringBuilder builder = new StringBuilder();
    StringBuilder keyBuilder = new StringBuilder();
    SoundPool sp;
    int soundfile;
    HashMap<String, String> buttonMapping = new HashMap<String, String>();
    String lastDirection = "NONE";

    float [] history = new float[3];
    String[] direction = {"NONE", "NONE", "NONE"};
    String[] pressed = {"0", "0", "0"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        buttonMapping.put("Left|000", "1");
        buttonMapping.put("Left|001", "2");
        buttonMapping.put("Left|010", "3");
        buttonMapping.put("Left|011", "4");
        buttonMapping.put("Left|100", "5");
        buttonMapping.put("Left|101", "6");
        buttonMapping.put("Left|110", "7");
        buttonMapping.put("Left|111", "8");
        buttonMapping.put("Right|000", "9");
        buttonMapping.put("Right|001", "10");
        buttonMapping.put("Right|010", "11");
        buttonMapping.put("Right|011", "12");
        buttonMapping.put("Right|100", "13");
        buttonMapping.put("Right|101", "14");
        buttonMapping.put("Right|110", "15");
        buttonMapping.put("Right|111", "16");

        sp = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        soundfile = sp.load(this, R.raw.airhorn, 0);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //sp = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        //soundfile = sp.load("/location/of/airhorn.m4a",0);

        float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];
        float zChange = history[2] - event.values[2];
        history[0] = event.values[0];
        history[1] = event.values[1];
        history[2] = event.values[2];

        if (xChange > 1){
            direction[0] = "Left";
        }
        else if (xChange < -1){
            direction[0] = "Right";
        }

        if (yChange > 1){
            direction[1] = "Towards";
        }
        else if (yChange < -1){
            direction[1] = "Away";
        }

        if (zChange < -5) {
            direction[2] = "Above";
        }
        else if (zChange > 5) {
            direction[2] = "Below";
        }

        if (findViewById(R.id.greenButton).isPressed()) {
            pressed[0] = "1";
            //if (soundfile != 0){
                //sp.play(soundfile, 1,1,0,0,1f); // play at original frequency
            //}

        } else {
            pressed[0] = "0";
        }
        if (findViewById(R.id.redButton).isPressed()) {
            pressed[1] = "1";
            //if (soundfile != 0){
                //sp.play(soundfile, 1,1,0,0,2f); // play at 2x frequency
            //}
        } else {
            pressed[1] = "0";
        }
        if (findViewById(R.id.yellowButton).isPressed()) {
            pressed[2] = "1";
            //if (soundfile != 0){
                //sp.play(soundfile, 1,1,0,0,3f); // play at 3x frequency
            //}
        } else {
            pressed[2] = "0";
        }
        
        xChange = Math.abs(xChange);
        yChange = Math.abs(yChange);
        zChange = Math.abs(zChange);

        builder.setLength(0);
        builder.append("x: ");
        builder.append(direction[0]);
        builder.append(",    y: ");
        builder.append(direction[1]);
        builder.append(",    z: ");
        builder.append(direction[2]);
        builder.append("\n");
        builder.append("Green: ");
        builder.append(pressed[0]);
        builder.append(",    Red: ");
        builder.append(pressed[1]);
        builder.append(",    Yellow: ");
        builder.append(pressed[2]);

        textView.setText(builder.toString());

        keyBuilder.setLength(0);
        if (xChange >= yChange && (xChange > 1 || xChange < -1) ) {
            lastDirection = direction[0];
        }

        keyBuilder.append(lastDirection);
        keyBuilder.append("|");
        keyBuilder.append(pressed[0]);
        keyBuilder.append(pressed[1]);
        keyBuilder.append(pressed[2]);
        textView.setText(keyBuilder.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }
}