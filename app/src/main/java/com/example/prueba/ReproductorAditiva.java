package com.example.prueba;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Pair;

import java.util.ArrayList;

public class ReproductorAditiva {

    private double freqOfTone; // hz
    private float[] envSamples;
    private final int SAMPLE_RATE = 44100;
    private int numSamples;
    private double[] sample;
    private int duration;
    private final double[] VOLUMENES = {0.3, 0.2, 0.15, 0.15, 0.1, 0.1};
    private double[] arms = new double[VOLUMENES.length];
    private short CHUNK = 32767;
    private final byte generatedSnd[];
    private ArrayList<Pair<Float, Float>> puntosEnvolvente;
    private double last;
    private Handler handler = new Handler();

    public ReproductorAditiva(int duration, double frecuencia, ArrayList<Pair<Float, Float>> puntosEnvolvente){
        numSamples = SAMPLE_RATE*duration;
        this.duration = duration;
        freqOfTone = frecuencia;
        this.puntosEnvolvente = puntosEnvolvente;
        this.sample = new double[numSamples];
        generatedSnd = new byte[2 * numSamples];
        envSamples = env();
        genTone();


    }

    void genTone() {
        //Rellenamos el array de valores de armónicos
        for (int i = 0; i < VOLUMENES.length; ++i) {
            arms[i] = freqOfTone * (i + 1);
        }
        for (int i = 0; i < numSamples; ++i) {
            //Generamos el sample mediante síntesis aditiva
            for (int j = 0; j < VOLUMENES.length; ++j)
                sample[i] += Math.sin(2 * Math.PI * i / (SAMPLE_RATE / arms[j])) * VOLUMENES[j];
        }

        //Aplicamos la envolvente al sample generado previamente
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = sample[i] * envSamples[i];
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


    //Funcion que genera los samples de envolventes según los valores de las SeekBar
    private float[] env() {
        last = numSamples + CHUNK;
        float[] samplesEnvolvente = new float[(int) last];
        for (int i = 0; i < puntosEnvolvente.size() - 1; i++) {
            int f1, f2;
            double v1, v2;
            f1 = (int) (puntosEnvolvente.get(i).first * SAMPLE_RATE);
            f2 = (int) (puntosEnvolvente.get(i + 1).first * SAMPLE_RATE);
            v1 = puntosEnvolvente.get(i).second;
            v2 = puntosEnvolvente.get(i + 1).second;
            for (int j = f1; j < f2; j++) {
                samplesEnvolvente[j] = (float) (v1 + (j - f1) * (v2 - v1) / (f2 - f1));
            }
        }

        return samplesEnvolvente;
    }



    public void playSound() {
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);

        //Comprobamos que el audioTrack esté disponible antes de reproducir para no sobrecargar el dispositivo
        if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.play();
            audioTrack.setNotificationMarkerPosition(duration * SAMPLE_RATE);
            audioTrack.setNotificationMarkerPosition(audioTrack.getPlaybackHeadPosition() + (duration * SAMPLE_RATE));
            audioTrack.setPlaybackPositionUpdateListener(
                    new AudioTrack.OnPlaybackPositionUpdateListener() {
                        @Override
                        public void onMarkerReached(AudioTrack arg0) {
                            audioTrack.release();
                        }

                        @Override
                        public void onPeriodicNotification(AudioTrack arg0) {
                        }
                    }
            );
        }
    }

}
