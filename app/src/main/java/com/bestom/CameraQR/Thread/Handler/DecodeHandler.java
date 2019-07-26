/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bestom.CameraQR.Thread.Handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.bestom.CameraQR.CameraActivity;
import com.bestom.CameraQR.R;
import com.bestom.CameraQR.core.DealWithOenCV;
import com.dtr.zbar.build.ZBarDecoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static com.bestom.CameraQR.R.id.restart_preview;

public final class DecodeHandler extends Handler {

    private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CameraActivity activity;
    private ZBarDecoder zBarDecoder;

    private boolean running = true;

    public  DecodeHandler(CameraActivity activity) {
        zBarDecoder = new ZBarDecoder();
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        if (message.what == R.id.decode0||message.what == R.id.decode1) {
            int decode=message.what;
            decode((byte[]) message.obj, message.arg1, message.arg2,decode);
        }else if (message.what==R.id.zxing_decode0||message.what==R.id.zxing_decode1){
            int decode=message.what;
            zxingdecode((Bitmap) message.obj ,decode);
        } else if (message.what == R.id.quit) {
            running = false;
            Looper.myLooper().quit();
        }
    }

    /**
     * zxing解码
     */
    private synchronized void zxingdecode(Bitmap bitmap ,int decodeid) {
        if (decodeid== R.id.decode0){
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            final int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            RGBLuminanceSource luminanceSource = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
            Handler handler = activity.getHandler();
            try {
                final Map<DecodeHintType, Object> hints = new HashMap<>();
                hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
                hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);
                hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                Result result = new QRCodeReader().decode(binaryBitmap, hints);

                if (result.getText().equals("")) {
                    Log.e(TAG,"zxing result is null,restart_preview");
                    Message message = Message.obtain(handler, restart_preview);
                    message.sendToTarget();
                } else {
                    Log.i(TAG,"zxing decode result"+result.getText());
                    // long end = System.currentTimeMillis();
                    Message message = Message.obtain(handler,
                            R.id.decode_succeeded0, result.getText());
                    message.sendToTarget();
                }
            } catch (Exception e) {
                Log.e(TAG,"zxing error.....requestPreviewFrame");
                Message message = Message.obtain(handler, R.id.decode_failed0);
                message.sendToTarget();
                e.printStackTrace();
            }
        }else if (decodeid== R.id.decode1){

        }
    }

    /**
     * zbar解码
     */
    private synchronized void decode(byte[] data, int width, int height,int decodeid) {
        if (decodeid== R.id.decode0){
            Handler handler = activity.getHandler();
            long start = System.currentTimeMillis();
    //region             这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
//            byte[] rotatedData = new byte[data.length];
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++)
//                    rotatedData[x * height + height - y - 1] = data[x + y * width];
//            }
//
//            // 宽高也要调整
//            int tmp = width;
//            width = height;
//            height = tmp;
//            Rect mCropRect = activity.initCrop(decodeid);
            //endregion
            String result =null;
            if (zBarDecoder != null) {
                try {
                    //result = zBarDecoder.decodeCrop(rotatedData, width, height, mCropRect.left, mCropRect.top, mCropRect.width(), mCropRect.height());
//                    result = zBarDecoder.decodeCrop(rotatedData, width, height, 0, 0, width, height);
//                    result = zBarDecoder.decodeCrop(data, width, height, 0, 0, width, height);
                    result = zBarDecoder.decodeRaw(data,width,height);
                }catch (Exception ex){
                    zBarDecoder=null;
                    Message errmessage = Message.obtain(handler,
                            restart_preview, result);
                    errmessage.sendToTarget();
                    ex.printStackTrace();
                }

                if (result != null) {
                    // long end = System.currentTimeMillis();
                    if (handler != null) {
                        Message message = Message.obtain(handler,
                                R.id.decode_succeeded0, result);
                        message.sendToTarget();
                    }
                } else {
                    //zbar 解码失败，进行图像 处理，zxing 解码
                    Log.e(TAG,"zbar 解码失败，进行图像处理zxing解码！！！");
                    dealimg(data,width,height, decodeid);

//                    if (handler != null) {
//                        Message message = Message.obtain(handler, R.id.decode_failed0);
//                        message.sendToTarget();
//                    }
                }
            }
        }

        if (decodeid== R.id.decode1){
            // long start = System.currentTimeMillis();
            //region 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
//            byte[] rotatedData = new byte[data.length];
//            for (int y = 0; y < height; y++) {
//                for (int x = 0; x < width; x++)
//                    rotatedData[x * height + height - y - 1] = data[x + y * width];
//            }
//
//            // 宽高也要调整
//            int tmp = width;
//            width = height;
//            height = tmp;
            //Rect mCropRect = activity.initCrop(decodeid);
            //endregion
            String result =null;
            if (zBarDecoder != null) {
                try {
//                    result = zBarDecoder.decodeCrop(rotatedData, width, height, 0, 0, width, height);
                    result = zBarDecoder.decodeRaw(data,width,height);
                }catch (Exception ex){
                    ex.printStackTrace();
                    zBarDecoder=null;
                }
                Handler handler = activity.getHandler();
                if (result != null) {
                    // long end = System.currentTimeMillis();
                    if (handler != null) {
                        Message message = Message.obtain(handler,
                                R.id.decode_succeeded1, result);
                        message.sendToTarget();
                    }
                } else {
                    //zbar 解码失败，进行图像 处理，zxing 解码



//                    if (handler != null) {
//                        Message message = Message.obtain(handler, R.id.decode_failed1);
//                        message.sendToTarget();
//                    }
                }
            }
        }
    }

    //opencv 处理图像
    private synchronized void dealimg(byte[] data,int width,int height, int decodeid){
        Message mainmessage;
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                data,
                ImageFormat.NV21,
                width,
                height,
                null);
        byte[] yuvImage=yuvimage.getYuvData();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);// 80--JPG图片的质量[0-100],100最高
        byte[] jpegImage = baos.toByteArray();

        //将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegImage, 0, jpegImage.length, options);

        if (bitmap != null) {
            Log.e("dddd", "jpegImage size:" + bitmap.getWidth() + "*" + bitmap.getHeight());
            Log.e("dddd", "jpegImage Format" + bitmap.getConfig());

            DealWithOenCV.getInstance().setData( bitmap);
            Bitmap resultbitmap = DealWithOenCV.getInstance().deal();
            if (resultbitmap != null) {
                //zxing decode
                mainmessage = activity.getHandler().obtainMessage(R.id.setimg,resultbitmap);
                mainmessage.sendToTarget();
                zxingdecode(resultbitmap,decodeid);
            } else {
                Log.e(TAG, "dealimg 图像处理异常，重新取图像");
                mainmessage = activity.getHandler().obtainMessage(restart_preview);
                mainmessage.sendToTarget();
            }
        }else {
            Log.e(TAG, "dealimg 图像异常，重新取图像");
            mainmessage = activity.getHandler().obtainMessage(restart_preview);
            mainmessage.sendToTarget();
        }
    }


}
