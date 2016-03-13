package com.example.ihavenoidea.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    TextView textView;
    LinkedList<String> queue = new LinkedList<String>();
    boolean moveTaken = false;

    HashMap<String, Airhorn> buttonMapping = new HashMap<String, Airhorn>();

    float [] history = new float[3];
    int[] direction = {0};
    String[] pressed = {"0", "0", "0"};
    int[] oldDirection = {0};
    long lastUpdate = -1;
    int rCount = 0;
    int lCount = 0;
    int currentPos = 0;
    int player = 0;
    ArrayList<String> players = new ArrayList<String>();

    final Context context = this;
    private Button button;
    NumberPicker playerPicker;
    TextView defPlayer;
    Dialog alertdialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        //buttonMapping.put("000", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn, this));
        buttonMapping.put("001", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn5, this));
        buttonMapping.put("010", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn3, this));
        buttonMapping.put("011", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn35, this));
        buttonMapping.put("100", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn, this));
        buttonMapping.put("101", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn15, this));
        buttonMapping.put("110", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn13, this));
        buttonMapping.put("111", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn135, this));

        //prompt for player totals (2-4)

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v1 = inflater.inflate(R.layout.numpicker, null);
        playerPicker = (NumberPicker) v1.findViewById(R.id.playerPicker);
        playerPicker.setMaxValue(4);
        playerPicker.setMinValue(2);
        playerPicker.setValue(2);
        playerPicker.setWrapSelectorWheel(true);

        //alertdialog.setContentView(R.layout.custom);
        //alertdialog.setTitle("How many are playing?");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setView(v1);
        builder.setTitle("How many are playing?");
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                player = playerPicker.getValue();
            }
        });

        //TextView text = (TextView) alertdialog.findViewById(R.id.text);
        //text.setText("How many are playing?");

//        Button dialogButton = (Button) alertdialog.findViewById(R.id.dialogButtonOK);
//        dialogButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertdialog.dismiss();
//            }
//        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertdialog.dismiss();

            }
        });

        alertdialog = builder.create();
        alertdialog.show();

    }

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
        if (buttonMapping.get("" + pressed[0] + pressed[1] + pressed[2]) != null) {
            buttonMapping.get("" + pressed[0] + pressed[1] + pressed[2]).play();
            //if it is not your turn to set one
            if (currentPos < queue.size()) {
                if (queue.get(currentPos).equals("" + pressed[0] + pressed[1] + pressed[2])) {
                    //doNothing - you were correct - display toast
                } else {
                    //not matching, display dialogue saying you messed up and give letter
                }
            } else {
                //making your own action
                queue.add("" + pressed[0] + pressed[1] + pressed[2]);
            }
        }

    }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }
}