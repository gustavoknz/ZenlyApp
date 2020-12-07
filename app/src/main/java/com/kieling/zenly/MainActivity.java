package com.kieling.zenly;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int NUMBER_PICKER_MIN_VALUE = 0;
    private static final int NUMBER_PICKER_MAX_VALUE = 9999;
    private static final int NUMBER_PICKER_SCROLL_Y = -100;
    private static final int NUMBER_PICKER_PERIOD_BETWEEN_EXECUTIONS_MILLISECONDS = 100;
    private static final int NUMBER_PICKER_ITEMS_TO_AUTO_SCROLL_ALLOWED = 10;
    private GradientDrawable[] mGradientDrawables;
    private ConstraintLayout mConstraintLayout;
    private Timer mBackgroundTimer;
    private Timer mNumberPickerTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_transition);
        mConstraintLayout = (ConstraintLayout) findViewById(R.id.main_layout);

        int redColor = 0xffff0000;
        int blueColor = 0xff0000ff;
        GradientDrawable gradientDrawableInit = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{redColor, blueColor});

        int greenColor = 0xff00ff00;
        int yellowColor = 0xffffff00;
        GradientDrawable gradientDrawableFinal = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{greenColor, yellowColor});

        mGradientDrawables = new GradientDrawable[]{gradientDrawableInit, gradientDrawableFinal};
        usingTransition();

        // NUMBER PICKER
        NumberPicker numberPicker = findViewById(R.id.main_number_picker);
        numberPicker.setMinValue(NUMBER_PICKER_MIN_VALUE);
        numberPicker.setMaxValue(NUMBER_PICKER_MAX_VALUE);
        AtomicInteger initialValue = new AtomicInteger();
        numberPicker.setOnScrollListener((numberPickerInner, scrollState) -> {
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                initialValue.set(numberPicker.getValue());
                Log.d(TAG, "Number selected: " + initialValue);
                mNumberPickerTimer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Log.d(TAG, "numberPickerInner.getValue(): " + numberPickerInner.getValue());
                        if (initialValue.get() + NUMBER_PICKER_ITEMS_TO_AUTO_SCROLL_ALLOWED < numberPickerInner.getValue()) {
                            Log.d(TAG, "Cancelling timerTask");
                            mNumberPickerTimer.cancel();
                            mNumberPickerTimer.purge();
                            mNumberPickerTimer = null;
                            Log.d(TAG, "NumberPickerTimer cancelled");
                        }
                        runOnUiThread(() -> numberPickerInner.scrollBy(0, NUMBER_PICKER_SCROLL_Y));
                    }
                };
                mNumberPickerTimer.schedule(timerTask, 0, NUMBER_PICKER_PERIOD_BETWEEN_EXECUTIONS_MILLISECONDS);
            }
        });
    }

    private void usingTransition() {
        mConstraintLayout.setBackground(new TransitionDrawable(mGradientDrawables));
        long delay = 0;
        long period = 2000;
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new MyTimerTask(), delay, period);
    }

    @Override
    protected void onDestroy() {
        if (mBackgroundTimer != null) {
            mBackgroundTimer.cancel();
            mBackgroundTimer.purge();
            mBackgroundTimer = null;
        }
        if (mNumberPickerTimer != null) {
            mNumberPickerTimer.cancel();
            mNumberPickerTimer.purge();
            mNumberPickerTimer = null;
        }
        super.onDestroy();
    }

    class MyTimerTask extends TimerTask {
        private final TransitionDrawable mLayoutBackground = (TransitionDrawable) mConstraintLayout.getBackground();
        private int mCount = 0;

        @Override
        public void run() {
            runOnUiThread(() -> {
                if (mCount++ % 2 == 0) {
                    mLayoutBackground.startTransition(2000);
                } else {
                    mLayoutBackground.reverseTransition(2000);
                }
            });
        }
    }
}
