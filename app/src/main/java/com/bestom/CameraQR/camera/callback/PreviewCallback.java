package com.bestom.CameraQR.camera.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bestom.CameraQR.R;
import com.bestom.CameraQR.bean.imgbyte;
import com.bestom.CameraQR.core.DealWithOenCV;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;

import static com.bestom.CameraQR.R.id.restart_preview;

/**
 * desc:二维码解码的回调类
 * Author: znq
 * Date: 2016-11-03 16:24
 */

public class PreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = "PreviewCallback";
    private Handler mainHandler;
    private Handler childHandler;
    private int messageWhat;

    public PreviewCallback() {
    }

    public void setHandler(Handler mainHandler, Handler childHandler, int messageWhat) {
        this.mainHandler=mainHandler;
        this.messageWhat = messageWhat;
        this.childHandler = childHandler;
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        synchronized (this){
            Handler theChildHandler = childHandler;
            Camera.Size size = null;
            Message message;
            try {
                size = camera.getParameters().getPreviewSize();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (size != null && theChildHandler != null) {
//            Log.i("dddd","rawImage[] size:"+size.width+"*"+size.height);
                byte[] yuvImage;
                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                newOpts.inJustDecodeBounds = true;
                YuvImage yuvimage = new YuvImage(
                        data,
                        ImageFormat.NV21,
                        size.width,
                        size.height,
                        null);
                yuvImage = yuvimage.getYuvData();
                Log.i(TAG, "onPreviewFrame size:" + size.width + "*" + size.height);

                message = theChildHandler.obtainMessage(messageWhat, size.width, size.height, yuvImage);
                message.sendToTarget();
//                    Message message = theChildHandler.obtainMessage(messageWhat, size.width, size.height, data);
//                    message.sendToTarget();
                childHandler = null;
            }
                //Log.i(TAG,"onPreviewFrame data[]"+data.length);
        }
    }

}
