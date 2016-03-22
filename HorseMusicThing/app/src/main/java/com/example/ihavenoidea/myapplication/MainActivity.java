package com.example.ihavenoidea.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //Queue used to process the set of played commands
    LinkedList<String> queue = new LinkedList<String>();

    //Boolean to test whether a slide action occured
    boolean moveTaken = false;

    //Map used to store the mapping from button presses to Airhorn sounds
    // {###} -> Airhorn sound
    HashMap<String, Airhorn> buttonMapping = new HashMap<String, Airhorn>();

    //array used to store the history of the values picked up by the sensor
    //  length 3 for forwards compatibility (addition of y / z values)
    // [0] = x
    // [1] = y
    // [2] = z
    float[] history = new float[3];

    //array used to store the direction determined from sensor readigns
    //  length 3 for forwards compatibility (addition of y / z values)
    //  [0] = x
    //  [1] = y
    //  [2] = z
    int[] direction = {0, 0, 0};

    //array used to store button presses
    //  [0] = green
    //  [1] = red
    //  [2] = yellow
    String[] pressed = {"0", "0", "0"};

    //array used to store the oldDirection determined from sensor readigns
    //  length 3 for forwards compatibility (addition of y / z values)
    //  [0] = x
    //  [1] = y
    //  [2] = z
    int[] oldDirection = {0, 0, 0};

    //time to keep track of the last sensor reading used as an update
    long lastUpdate = -1;

    //count to see for how long the sensor was reading right movement
    int rCount = 0;

    //count to see for how long the sensor was reading right movement
    int lCount = 0;

    //the currentPostion in the queue of commands being processed
    int currentPos = 0;

    //the currentPlayer whose turn it is
    int currentPlayer = 0;

    //ArrayList to keep track of the players
    ArrayList<String> players = new ArrayList<String>();

    //the NumberPicked used to pick the number of players
    NumberPicker playerPicker;

    //the Dialog used to prompt for the number of players
    Dialog alertdialog;

    //ArrayList to keep track of whether the players are still in the game
    ArrayList<Boolean> playersStillIn = new ArrayList<Boolean>();

    //ArrayList to keep track of the textViews that contain the player letters
    ArrayList<TextView> pViews = new ArrayList<TextView>();

    //ArrayList to keep track of the number of letters a player has
    ArrayList<Integer> playerLetters = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the sensor manager and set the current sensor to the accelerometer
        SensorManager manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        //populate the button mapping
        buttonMapping.put("001", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn5, this));
        buttonMapping.put("010", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn3, this));
        buttonMapping.put("011", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn35, this));
        buttonMapping.put("100", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn, this));
        buttonMapping.put("101", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn15, this));
        buttonMapping.put("110", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn13, this));
        buttonMapping.put("111", new Airhorn(com.example.ihavenoidea.myapplication.R.raw.airhorn135, this));

        //make the numberPicker used to select the number of players (bounded to 2-4)
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v1 = inflater.inflate(R.layout.numpicker, null);
        playerPicker = (NumberPicker) v1.findViewById(R.id.playerPicker);
        playerPicker.setMaxValue(4);
        playerPicker.setMinValue(2);
        playerPicker.setValue(2);
        playerPicker.setWrapSelectorWheel(true);

        //build the alert dialog that prompts for the number of players
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(v1);
        builder.setTitle("How many are playing?");

        //set up the okay button for the dialog
        v1.findViewById(R.id.playerPickerConfirmButton).setOnClickListener(new OnClickListener() {
            //if okay is clicked, initialize everything to the correct sizes and update screen
            public void onClick(View v) {
                //gets the number of players from the picker
                //currentPlayer = playerPicker.getValue();
                // add the correct number of starting values to playerLetters, players and playersStillIn
                for (int x = 0; x < currentPlayer; x++) {
                    playerLetters.add(0);
                    players.add("" + x);
                    playersStillIn.add(true);
                }

                //add then remove the correct number of views to be tracked to the pViews
                pViews.add((TextView) findViewById(R.id.p1));
                pViews.add((TextView) findViewById(R.id.p2));
                pViews.add((TextView) findViewById(R.id.p3));
                pViews.add((TextView) findViewById(R.id.p4));
                for (int x = 3; x >= currentPlayer; x--) {
                    pViews.remove(x).setText("");
                }

                //set the currentPlayer to 0 (the first player)
                currentPlayer = 0;
            }
        });

        //create and show the dialog
        alertdialog = builder.create();
        alertdialog.show();

        //create and add the reset button to the screen (simply restarts the app for now)
        Button reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //get the current time of the system
        long currTime = System.currentTimeMillis();

        //detects if it has been 100ms since the lastUpdate
        //  if it has not been it does nothing
        if (currTime - lastUpdate < 100) {
            lastUpdate = currTime;
            return;
        }

        //gets the xChange and then updates history
        float xChange = history[0] - event.values[1];
        history[0] = event.values[0];

        //updates the x direction if needed (threshold of 3 for now)
        if (xChange > 3) {
            direction[0] = -1;
        } else if (xChange < -3) {
            direction[0] = 1;
        }

        //sets the values for the GRY button presses
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

        //if the current direction is right (or has been right) bump right count
        if (direction[0] == 1) {
            rCount++;
        }

        //if the current direction is left (or has been right) bump right count
        if (direction[0] == -1) {
            lCount++;
        }

        //if the current direction has 'trended' for long enough (100) it marks a move has haven been taken
        //  saves the direction and resets the value of the opposing count
        //  otherwise no move has been taken
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

        //if a move has been taken and the direction has changed from the previously recorded value
        if (moveTaken && oldDirection[0] != direction[0]) {
            //update the old direction as 0 (neutral) and reset the counts
            oldDirection[0] = 0;
            rCount = 0;
            lCount = 0;
            //ensure that a combo of the three buttons was held
            if (buttonMapping.get("" + pressed[0] + pressed[1] + pressed[2]) != null) {
                //Player's turn to copy
                if (currentPos < queue.size()) {
                    Context context = getApplicationContext();
                    //If player copies correctly
                    if (queue.get(currentPos).equals("" + pressed[0] + pressed[1] + pressed[2])) {
                        buttonMapping.get("" + pressed[0] + pressed[1] + pressed[2]).play(1);

                        currentPos++;
                    //Player copies incorrectly, play the negative sound and then penalize
                    } else {
                        Airhorn wrong = new Airhorn(R.raw.scream, context);
                        wrong.play(1);
                        currentPos = 0;
                        queue.clear();
                        playerLetters.set(currentPlayer, playerLetters.get(currentPlayer) + 1);
                        pViews.get(currentPlayer).setText(Html.fromHtml("P" + (currentPlayer + 1) + " <font color='#EE0000'>" + "HORSE".substring(0, playerLetters.get(currentPlayer)) + "</font>" + " HORSE".substring(playerLetters.get(currentPlayer) + 1)));

                        //If this player has spelt HORSE, eliminate them
                        if (playerLetters.get(currentPlayer) == 5) {
                            playersStillIn.set(currentPlayer, false);
                        }

                        //if there is only one player remaining, game is over
                        //  make dialog and display winner and allow game restart
                        if (Collections.frequency(playersStillIn, true) == 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Game over!");
                            TextView tv = new TextView(this);
                            tv.setText("Player " +  (playersStillIn.indexOf(true) + 1) +" Wins!");
                            tv.setGravity(Gravity.CENTER);
                            tv.setTextSize(25);
                            builder.setView(tv);
                            builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = getBaseContext().getPackageManager()
                                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                }
                            });
                            builder.create().show();
                        }
                        // if the player is still in the game direct them to pass
                        else {
                            CharSequence text = "Incorrect! Next player's turn to set.";
                            int duration = Toast.LENGTH_SHORT;
                            Toast burntToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                            burntToast.setGravity(Gravity.CENTER, 0, 0);
                            burntToast.show();
                        }
                        //skip over players who were eliminated
                        currentPlayer += 1;
                        currentPlayer = currentPlayer % players.size();
                        while (!playersStillIn.get(currentPlayer)) {
                            currentPlayer += 1;
                            currentPlayer = currentPlayer % players.size();
                        }
                        ((TextView) findViewById(R.id.currPLayer)).setText("Player " + (currentPlayer + 1) + " Turn");
                    }
                }
                //player has correctly done all of the commands, they get to set a command and then prompted to pass
                else {
                    //adding a players action to the queue
                    buttonMapping.get("" + pressed[0] + pressed[1] + pressed[2]).play(1);
                    queue.addLast("" + pressed[0] + pressed[1] + pressed[2]);
                    currentPlayer += 1;
                    currentPlayer = currentPlayer % players.size();

                    //skip over players who were eliminated
                    while (!playersStillIn.get(currentPlayer)) {
                        currentPlayer += 1;
                        currentPlayer = currentPlayer % players.size();
                    }

                    //prompt the player to pass the phone to the next player
                    ((TextView) findViewById(R.id.currPLayer)).setText("Player " + (currentPlayer + 1) + " Turn");
                    currentPos = 0;
                    Context context = getApplicationContext();
                    CharSequence text = "Pass the phone! Next player's turn to copy.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast burntToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    burntToast.setGravity(Gravity.CENTER, 0, 0);
                    burntToast.show();
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
