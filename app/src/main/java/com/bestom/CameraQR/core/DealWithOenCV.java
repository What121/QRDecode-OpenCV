package com.bestom.CameraQR.core;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bestom.CameraQR.R;
import com.bestom.CameraQR.bean.imgbyte;
import com.bestom.CameraQR.utils.OpenCVUtils;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.RotatedRect;

public class DealWithOenCV {
//    private byte[] data;
    private Bitmap data;
    private Handler mHandler;
    private Mat baseMat1,baseMat2,userMat,resultMat;

    private static OpenCVUtils mOpenCVUtils;
    private static DealWithOenCV instance;

    private DealWithOenCV() {
    }

    public static DealWithOenCV getInstance() {
        if (instance == null) {
            synchronized (DealWithOenCV.class) {
                if (instance == null) {
                    instance = new DealWithOenCV();
                    mOpenCVUtils=new OpenCVUtils();
                }
            }
        }
        return instance;
    }

//    public void setData(byte[] data) {
//        this.data = data;
//    }

    public void setData( Bitmap data) {
        this.data = data;
    }

    public void setData(android.os.Handler handler, Bitmap data) {
        this.mHandler=handler;
        this.data = data;
    }

    public Bitmap deal(){
        baseMat1=new Mat();
        baseMat1.convertTo(baseMat1, CvType.CV_8U);
//        baseMat1.put(0,0,data);
        Utils.bitmapToMat(data,baseMat1);
//        dealimg(baseMat1,0);
        RotatedRect minRotatedRect1,minRotatedRect2;
        //1.对原始图片做opencv基本处理
        userMat=baseMat1.clone();
        userMat=mOpenCVUtils.gray(userMat);
//        dealimg(userMat,1);
        userMat=mOpenCVUtils.gslb(userMat);
//        dealimg(userMat,2);
        userMat=mOpenCVUtils.Sobel(userMat);
//        dealimg(userMat,3);
        userMat=mOpenCVUtils.jzlb(userMat);
//        dealimg(userMat,4);
        userMat=mOpenCVUtils.threshold(userMat);
//        dealimg(userMat,5);
        userMat=mOpenCVUtils.bcl(userMat);
//        dealimg(userMat,6);
        userMat=mOpenCVUtils.fscl(userMat);
//        dealimg(userMat,7);
        userMat=mOpenCVUtils.pzcl(userMat);
//        dealimg(userMat,8);
        //2.寻找边缘定位并 截取 得到mat
        minRotatedRect1=mOpenCVUtils.findContours(userMat);
        if (minRotatedRect1!=null) {
            userMat = baseMat1;
            baseMat2 = mOpenCVUtils.jqcl(userMat, minRotatedRect1);
            if (baseMat2 != null) {
//                dealimg(baseMat2,9);

                //region                 //3.对截取mat进行 open基本处理 + 边缘定位 校正
                userMat = baseMat2.clone();
                userMat = mOpenCVUtils.gray(userMat);
                userMat = mOpenCVUtils.gslb(userMat);
                userMat = mOpenCVUtils.Sobel(userMat);
                userMat = mOpenCVUtils.jzlb(userMat);
                userMat = mOpenCVUtils.threshold(userMat);
                userMat = mOpenCVUtils.bcl(userMat);
                userMat = mOpenCVUtils.fscl(userMat);
                userMat = mOpenCVUtils.pzcl(userMat);
                minRotatedRect2 = mOpenCVUtils.findContours(userMat);
                if (minRotatedRect2 != null) {
                    userMat = baseMat2;
                    resultMat = mOpenCVUtils.jzcl(userMat, minRotatedRect2);

//                    Log .e("dddddd","rows"+resultMat.rows()+"cols"+resultMat.cols());
//                    int size= (int) resultMat.total()*resultMat.channels();
//                    Log .e("dddddd","total"+resultMat.total()+"channels"+resultMat.channels());
//                    byte[] resultdata=new byte[size];
//                    resultMat.get(0,0,resultdata);
                    //endregion

//                int width = baseMat2.cols();
//                int height = baseMat2.rows();
//                int dims = baseMat2.channels();
//                byte[] resultData = new byte[width*height*dims];
//                Log.i("dddd","result img size:"+width+"*"+height);
//                baseMat2.get(0,0,resultData);
//                return  new imgbyte(width,height,resultData);

                    Bitmap resultbitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(resultMat, resultbitmap);
                    return resultbitmap;

//                    Bitmap resultbitmap = Bitmap.createBitmap(baseMat2.cols(), baseMat2.rows(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(baseMat2, resultbitmap);
//                    return resultbitmap;
                }
            }
        }
        return null;
    }

    private void dealimg(Mat valuemat,int i){
        Bitmap resultbitmap=Bitmap.createBitmap(valuemat.cols(),valuemat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(valuemat,resultbitmap);
        Message mainmessage = mHandler.obtainMessage(R.id.setimg,2, i, resultbitmap);
        mainmessage.sendToTarget();
    }


}
