package com.bestom.CameraQR.bean;

import android.graphics.Bitmap;

public class imgbyte {
    private int w;
    private int h;
    private Bitmap mBitmap;

    public imgbyte() {
    }

    public imgbyte(int w, int h, Bitmap bitmap) {
        this.w = w;
        this.h = h;
        this.mBitmap = bitmap;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public Bitmap getData() {
        return mBitmap;
    }

    public void setData(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

}
