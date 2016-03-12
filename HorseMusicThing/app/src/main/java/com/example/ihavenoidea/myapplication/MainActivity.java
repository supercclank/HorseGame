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

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView textView;
    StringBuilder builder = new StringBuilder();
    SoundPool sp;
    int soundfile;

    float [] history = new float[3];
    String[] direction = {"NONE", "NONE", "NONE"};
    String[] pressed = {"NO", "NO", "NO"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        sp = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        soundfile = sp.load(this, R.raw.airhorn, 0);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];
        float zChange = history[2] - event.values[2];
        history[0] = event.values[0];
        history[1] = event.values[1];
        history[2] = event.values[2];

        if (xChange > 2){
            direction[0] = "Left";
        }
        else if (xChange < -2){
            direction[0] = "Right";
        }

        if (yChange > 2){
            direction[1] = "Away";
        }
        else if (yChange < -2){
            direction[1] = "Towards";
        }

        if (zChange < -2) {
            direction[2] = "Above";
        }
        else if (zChange > 2) {
            direction[2] = "Below";
        }

        if (findViewById(R.id.greenButton).isPressed()) {
            pressed[0] = "YES";
        } else {
            pressed[0] = "NO";
        }
        if (findViewById(R.id.redButton).isPressed()) {
            pressed[1] = "YES";
        } else {
            pressed[1] = "NO";
        }
        if (findViewById(R.id.yellowButton).isPressed()) {
            pressed[2] = "YES";
        } else {
            pressed[2] = "NO";
        }

        if (pressed[0].equals("YES")) {
            if (soundfile != 0){
                sp.play(soundfile, 1,1,0,0,1f); // play at original frequency
            }
        }
        if (pressed[1].equals("YES")) {
            if (soundfile != 0){
                sp.play(soundfile, 1,1,0,0,2f); // play at 2x frequency
            }
        }
        if (pressed[2].equals("YES")) {
            if (soundfile != 0){
                sp.play(soundfile, 1,1,0,0,3f); // play at 3x frequency
            }
        }

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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }
}