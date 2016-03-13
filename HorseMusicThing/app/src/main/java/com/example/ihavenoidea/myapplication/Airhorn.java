package com.example.ihavenoidea.myapplication;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class Airhorn {
//    private String resourceName;
//    private float freq;

//    public String getResourceName() {
//        return resourceName;
//    }

//    public float getFreq() {
//        return freq;
//    }

    private SoundPool sp;
    private int soundfile;

    public Airhorn(int sound, Context context) {
        sp = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        soundfile = sp.load(context, sound, 0);
    }

    public void play(float freq) {
        if (soundfile != 0) {
            sp.play(soundfile, 1, 1, 0, 0, freq);
        }
    }

//    @Override
//    public boolean equals(Object o) {
//        if (o != null && o instanceof ActionSound) {
//            ActionSound sound = (ActionSound) o;
//            return (sound.resourceName.equals(this.resourceName) && sound.freq == this.freq);
//        }
//        return false;
//    }
}