package com.example.prueba;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;


public class MainActivity extends Activity {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int duration = 5; // seconds
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 440; // hz
    private short CHUNK = 32767;
    private ArrayList<Pair<Double, Double>> puntosEnvolvente = new ArrayList<>();
    private double last;
    private float[] envSamples;

    private final byte generatedSnd[] = new byte[2 * numSamples];

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        puntosEnvolvente.add(new Pair<Double, Double>(0.0,0.0));
        puntosEnvolvente.add(new Pair<Double, Double>(0.05,0.9));
        puntosEnvolvente.add(new Pair<Double, Double>(0.1,0.3));
        puntosEnvolvente.add(new Pair<Double, Double>(2.6,0.2));
        puntosEnvolvente.add(new Pair<Double, Double>(4.3,0.0));
        last=puntosEnvolvente.size()-1;
        envSamples = env();


    }

    public void suena(View view) {
        // Use a new tread as this can take a while
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
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = sample[i]*envSamples[i];
        }


        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * CHUNK));
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

    private float[] env(){
        last = puntosEnvolvente.get(puntosEnvolvente.size()-1).first*sampleRate;
        last = last+CHUNK;
        float[] resul = new float[(int)last];

        for(int i = 0; i<puntosEnvolvente.size()-1; i++){
            int f1,f2;
            double v1,v2;

            f1 = (int) (puntosEnvolvente.get(i).first*sampleRate);
            f2 = (int) (puntosEnvolvente.get(i+1).first*sampleRate);

            v1 = puntosEnvolvente.get(i).second;
            v2 = puntosEnvolvente.get(i+1).second;

            for(int j= f1; j<f2; j++){

                resul[j] = (float) (v1+(j-f1)*(v2-v1)/(f2-f1));
            }
        }

        return resul;
    }
}