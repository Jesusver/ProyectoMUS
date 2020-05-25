package com.example.prueba;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.akaita.android.circularseekbar.CircularSeekBar;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends Activity {
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int duration = 4; // seconds
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private double freqOfTone; // hz
    private CircularSeekBarPropia seekBarAttack;
    private CircularSeekBarPropia seekBarDecay;
    private CircularSeekBarPropia seekBarSustain;
    private CircularSeekBarPropia seekBarRelease;

    private short CHUNK = 32767;
    private Octavas octavaNotas;
    private final DecimalFormat FORMAT = new DecimalFormat("#.###");

    private ArrayList<Pair<Float, Float>> puntosEnvolvente = new ArrayList<>();
    private double last;
    private float[] envSamples;

    private final byte generatedSnd[] = new byte[2 * numSamples];

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        construyeSpinner();

        seekBarAttack = findViewById(R.id.seekbarAttack);
        seekBarDecay = findViewById(R.id.seekbarDecay);
        seekBarSustain = findViewById(R.id.seekbarSustain);
        seekBarRelease = findViewById(R.id.seekbarRelease);
        inicializaSeekBars();
        puntosEnvolvente.add(new Pair<>(new Float(0.0), new Float(0.0)));
        puntosEnvolvente.add(new Pair<>(new Float(0.05), new Float(0.9)));
        puntosEnvolvente.add(new Pair<>(new Float(0.1), new Float(0.3)));
        puntosEnvolvente.add(new Pair<>(new Float(2.6), new Float(0.2)));
        puntosEnvolvente.add(new Pair<>(new Float(4.3), new Float(0.0)));
        last=puntosEnvolvente.size()-1;
        envSamples = env();
    }

    private void inicializaSeekBars() {
        inicializaAttackbar();
        inicializaDecaybar();
        inicializaSustainbar();
        inicializaReleasebar();

    }

    private void inicializaAttackbar() {
        ((TextView)findViewById(R.id.valorAttack)).setText(FORMAT.format(seekBarAttack.getProgress()) + "ms");

        seekBarAttack.setRingColor(Color.BLUE);
        seekBarAttack.setMax((float) 0.5);
        seekBarAttack.setOnCenterClickedListener(new CircularSeekBar.OnCenterClickedListener() {
            @Override
            public void onCenterClicked(CircularSeekBar seekBar, float progress) {
                Snackbar.make(seekBar, "reset", Snackbar.LENGTH_SHORT).show();
            }
        });
        seekBarAttack.setOnCircularSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {
                ((TextView)findViewById(R.id.valorAttack)).setText(FORMAT.format(seekBar.getProgress()) + "ms");
                puntosEnvolvente.remove(1);
                puntosEnvolvente.add(1, new Pair<>(progress, new Float(0.9)));
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                seekBarDecay.setMin(seekBar.getProgress(), seekBarDecay.getProgress());

            }
        });

    }

    private void inicializaDecaybar() {
        seekBarDecay.setMax((float) 1.5);
        ((TextView)findViewById(R.id.valorDecay)).setText(FORMAT.format(seekBarDecay.getProgress()) + "ms");

        seekBarDecay.setRingColor(Color.BLUE);
        seekBarDecay.setOnCenterClickedListener(new CircularSeekBar.OnCenterClickedListener() {
            @Override
            public void onCenterClicked(CircularSeekBar seekBar, float progress) {
                Snackbar.make(seekBar, "reset", Snackbar.LENGTH_SHORT).show();
            }
        });
        seekBarDecay.setOnCircularSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {
                ((TextView)findViewById(R.id.valorDecay)).setText(FORMAT.format(seekBar.getProgress()) + "ms");
                puntosEnvolvente.remove(2);
                puntosEnvolvente.add(2, new Pair<>(progress, new Float(0.3)));

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                seekBarSustain.setMin(seekBar.getProgress(), seekBarSustain.getProgress());

            }
        });
    }

    private void inicializaSustainbar() {
        seekBarSustain.setMax((float) 3.5);
        ((TextView)findViewById(R.id.valorSustain)).setText(FORMAT.format(seekBarSustain.getProgress()) + "ms");
        seekBarSustain.setRingColor(Color.BLUE);
        seekBarSustain.setOnCenterClickedListener(new CircularSeekBar.OnCenterClickedListener() {
            @Override
            public void onCenterClicked(CircularSeekBar seekBar, float progress) {
                Snackbar.make(seekBar, "reset", Snackbar.LENGTH_SHORT).show();

            }
        });
        seekBarSustain.setOnCircularSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {
                ((TextView)findViewById(R.id.valorSustain)).setText(FORMAT.format(seekBar.getProgress()) + "ms");
                puntosEnvolvente.remove(3);
                puntosEnvolvente.add(3, new Pair<>(progress, new Float(0.2)));

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                seekBarRelease.setMin(seekBar.getProgress(), seekBarRelease.getProgress());
            }
        });
    }



    private void inicializaReleasebar() {
        seekBarRelease.setRingColor(Color.BLUE);
        seekBarRelease.setMax(4);
        ((TextView)findViewById(R.id.valorRelease)).setText(FORMAT.format(seekBarRelease.getProgress()) + "ms");

        seekBarRelease.setOnCenterClickedListener(new CircularSeekBar.OnCenterClickedListener() {
            @Override
            public void onCenterClicked(CircularSeekBar seekBar, float progress) {
                Snackbar.make(seekBar, "reset", Snackbar.LENGTH_SHORT).show();

            }
        });
        seekBarRelease.setOnCircularSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar seekBar, float progress, boolean fromUser) {
                ((TextView)findViewById(R.id.valorRelease)).setText(FORMAT.format(seekBar.getProgress()) + "ms");
                puntosEnvolvente.remove(4);
                puntosEnvolvente.add(4, new Pair<>(progress, new Float(0.0)));

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }
        });
    }


    private void construyeSpinner() {
        Spinner s = findViewById(R.id.spinnerOctavas);
        ArrayList<String> octavas = Octavas.devuelveNombreOctavas();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, octavas);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                octavaNotas = Octavas.devuelveOctavaPorNombre((String)parent.getItemAtPosition(position));
                actualizaBotones();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void actualizaBotones() {
        Button botonNota = findViewById(R.id.button);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button2);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button3);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button4);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button5);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button6);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button7);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button8);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button9);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button10);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button11);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });
        botonNota = findViewById(R.id.button12);
        botonNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                freqOfTone = GeneraNota.getInstance().devuelveFrecuenciaNota(Notas.devuelveNotaPorNombre(((Button)v).getText().toString()), octavaNotas.getNumero());
                suena(v);
            }
        });

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
        if(audioTrack.getState()==AudioTrack.STATE_INITIALIZED) {
            audioTrack.play();
            audioTrack.setNotificationMarkerPosition(duration * sampleRate);
            audioTrack.setNotificationMarkerPosition(audioTrack.getPlaybackHeadPosition() + (duration * sampleRate));
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

        //while(audioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {}

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