import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.example.ihavenoidea.myapplication.R;

public class PlaySound extends Activity {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int duration = 0.5; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = 48;
    private final double sample[] = new double[numSamples];
    private final int[] freqOfTone = {466, 523, 587, 659, 698, 784, 880, 932}; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TextView(this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Use a new thread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                handler.post(new Runnable() {

                    public void run() {
                        playSound();
                    }
                });
            }
        });
        thread.start();
    }

    void genTone(){
        // fill out the array
        for (int i = 0; i < freqOfTone.length; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone[i]));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    void playSound(){
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }
}