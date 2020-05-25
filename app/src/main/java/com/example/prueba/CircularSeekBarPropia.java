package com.example.prueba;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.akaita.android.circularseekbar.CircularSeekBar;

public class CircularSeekBarPropia extends CircularSeekBar {
    public CircularSeekBarPropia(Context context) {
        super(context);
    }

    public CircularSeekBarPropia(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularSeekBarPropia(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean setMin(float min, float progreso) {
        super.setMin(min);
        super.setProgress(Math.max(min,progreso));
        if (this.getProgress()!=progreso)
            return true;
        return false;
    }
}
