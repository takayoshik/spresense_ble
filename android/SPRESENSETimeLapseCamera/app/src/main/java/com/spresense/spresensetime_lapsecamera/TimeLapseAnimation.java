package com.spresense.spresensetime_lapsecamera;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class TimeLapseAnimation extends Thread {
    ArrayList<Bitmap> bitmaps;
    TimeLapseFull timeLapseFull;

    public TimeLapseAnimation(ArrayList<Bitmap> bitmaps, TimeLapseFull timeLapseFull) {
        this.bitmaps = bitmaps;
        this.timeLapseFull = timeLapseFull;
    }

    public void run() {
        while (true) {
            for(int i = 0; i < bitmaps.size() &&  i < 1; i ++) {
                timeLapseFull.setImage(bitmaps.get(bitmaps.size() - i - 1));
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            }
        }
    }
}
