//package com.bestom.CameraQR.camera;
//
//import android.content.Context;
//import android.graphics.Point;
//import android.hardware.Camera;
//import android.os.Handler;
//import android.util.Log;
//import android.view.SurfaceHolder;
//
//import com.bestom.CameraQR.camera.callback.AutoFocusManager;
//import com.bestom.CameraQR.camera.callback.PreviewCallback;
//
//import java.io.IOException;
//
//
//public final class CameraManager {
//    private final String TAG= CameraManager.class.getSimpleName();
//
//    private Camera mCamera0,mCamera1;
//    private final CameraConfigurationManager configManager0,configManager1;
//    private AutoFocusManager mAutoFocusManager0,mAutoFocusManager1;
//    private final Context mContext;
//    private final PreviewCallback previewCallback0,previewCallback1;
//    private boolean previewing;
//    private boolean initialized0,initialized1;
//
//    private SurfaceHolder mHolder0,mHolder1;
//
//    public CameraManager(Context context) {
//        mContext = context;
//        configManager0=new CameraConfigurationManager(context);
//        configManager1=new CameraConfigurationManager(context);
//        previewCallback0=new PreviewCallback();
//        previewCallback1=new PreviewCallback();
//    }
//
//    public synchronized void openDriver(int CameraID, SurfaceHolder holder) throws IOException {
//        Camera theCamera0 ;
//        Camera theCamera1 ;
//        if (CameraID==0){
//            theCamera0 = null;
//            theCamera0=mCamera0;
//            mHolder0=holder;
//            if (theCamera0 == null) {
//                theCamera0 = Camera.open(0);
//                if (theCamera0 == null) {
//                    throw new IOException();
//                }
//                mCamera0 = theCamera0;
//            }
//            theCamera0.setPreviewDisplay(holder);
//
//            if (!initialized0) {
//                initialized0 = true;
//                configManager0.initFromCameraParameters(theCamera0);
//            }
//
//            Camera.Parameters parameters = theCamera0.getParameters();
//            String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save
//            // temporarily
//            try {
//                configManager0.setDesiredCameraParameters(theCamera0, false);
//            } catch (RuntimeException re) {
//                // Driver failed
//                Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
//                Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
//                // Reset:
//                if (parametersFlattened != null) {
//                    parameters = theCamera0.getParameters();
//                    parameters.unflatten(parametersFlattened);
//                    try {
//                        theCamera0.setParameters(parameters);
//                        configManager0.setDesiredCameraParameters(theCamera0, true);
//                    } catch (RuntimeException re2) {
//                        // Well, darn. Give up
//                        Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
//                    }
//                }
//            }
//        }
//        else if (CameraID==1){
//            theCamera1 = null;
//            theCamera1=mCamera1;
//            mHolder1=holder;
//            if (theCamera1 == null) {
//                theCamera1 = Camera.open(1);
//                if (theCamera1 == null) {
//                    throw new IOException();
//                }
//                mCamera1 = theCamera1;
//            }
//            theCamera1.setPreviewDisplay(holder);
//
//            if (!initialized1) {
//                initialized1 = true;
//                configManager1.initFromCameraParameters(theCamera1);
//            }
//
//            Camera.Parameters parameters = theCamera1.getParameters();
//            String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save
//            // temporarily
//            try {
//                configManager1.setDesiredCameraParameters(theCamera1, false);
//            } catch (RuntimeException re) {
//                // Driver failed
//                Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
//                Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
//                // Reset:
//                if (parametersFlattened != null) {
//                    parameters = theCamera1.getParameters();
//                    parameters.unflatten(parametersFlattened);
//                    try {
//                        theCamera1.setParameters(parameters);
//                        configManager1.setDesiredCameraParameters(theCamera1, true);
//                    } catch (RuntimeException re2) {
//                        // Well, darn. Give up
//                        Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
//                    }
//                }
//            }
//        }
//        else {
//            Log.e(TAG,"CameraID erro!");
//        }
//
//
//    }
//
//    public synchronized void closeDriver() {
//        if (mCamera0!=null){
//            mCamera0.release();
//            mCamera0=null;
//        }
//        if (mCamera1!=null){
//            mCamera1.release();
//            mCamera1=null;
//        }
//    }
//
//    public synchronized void closeDriver(int CameraID) {
//        if (CameraID==0){
//            if (mCamera0!=null){
//                mCamera0.release();
//                mCamera0=null;
//            }
//        }
//        if (CameraID==1){
//            if (mCamera1!=null){
//                mCamera1.release();
//                mCamera1=null;
//            }
//        }
//    }
//
//    public synchronized boolean isOpen(int CameraID) {
//        if (CameraID==0)
//            return mCamera0!=null;
//        if (CameraID==1)
//            return mCamera1 != null;
//        return false;
//    }
//
//    public Camera getCamera(int CameraID) {
//        if (CameraID==0)
//            return mCamera0;
//        if (CameraID==1)
//            return mCamera1;
//        return null;
//    }
//
//    /**
//     * Asks the camera hardware to begin drawing preview frames to the screen.
//     */
//    public synchronized void startPreview() {
//        Camera theCamera0 = mCamera0;
//        Camera theCamera1 = mCamera1;
//        if (theCamera0 != null && theCamera1!=null && !previewing) {
//            // Starts capturing and drawing preview frames to the screen
//            // Preview will not actually start until a surface is supplied with
//            // setPreviewDisplay(SurfaceHolder) or
//            // setPreviewTexture(SurfaceTexture).
//            try {
//                theCamera0.setPreviewDisplay(mHolder0);
//                theCamera0.startPreview();
//                theCamera1.setPreviewDisplay(mHolder1);
//                theCamera1.startPreview();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            previewing = true;
//            mAutoFocusManager0 = new AutoFocusManager(mContext, mCamera0);
//            mAutoFocusManager1 = new AutoFocusManager(mContext, mCamera1);
//        }
//    }
//
//    public synchronized void stopPreview() {
//        if (mAutoFocusManager0 != null) {
//            mAutoFocusManager0.stop();
//            mAutoFocusManager0 = null;
//        }
//        if (mAutoFocusManager1 !=null){
//            mAutoFocusManager1.stop();
//            mAutoFocusManager1=null;
//        }
//        if (mCamera0 != null && mCamera1 != null&& previewing) {
//            mCamera0.setPreviewCallback(null);
//            mCamera0.stopPreview();
//            previewCallback0.setHandler(null, 0);
//            mCamera1.setPreviewCallback(null);
//            mCamera1.stopPreview();
//            previewCallback1.setHandler(null, 0);
//            previewing = false;
//        }
//    }
//
//    public Point getCameraResolution(int CameraID) {
//        if (CameraID==0){
//            return configManager0.getCameraResolution();
//        }
//        if (CameraID==1){
//            return configManager1.getCameraResolution();
//        }
//        return null;
//    }
//
//    /* 两个绑定操作：<br/>
//     * 1：将handler与回调函数绑定；<br/>
//     * 2：将相机与回调函数绑定<br/>
//     * 综上，该函数的作用是当相机的预览界面准备就绪后就会调用hander向其发送传入的message
//     * @param handler     解码的子线程.
//     * @param message     R.id.decode.
//     */
//    public synchronized void requestPreview0Frame(Handler handler, int message) {
//        Camera theCamera = mCamera0;
//        if (theCamera != null && previewing) {
//            previewCallback0.setHandler(handler, message);
//            // 绑定相机回调函数，当预览界面准备就绪后会回调Camera.PreviewCallback.onPreviewFrame
//            theCamera.setOneShotPreviewCallback(previewCallback0);
//        }
//    }
//
//    public synchronized void requestPreview1Frame(Handler handler, int message) {
//        Camera theCamera = mCamera1;
//        if (theCamera != null && previewing) {
//            previewCallback1.setHandler(handler, message);
//            // 绑定相机回调函数，当预览界面准备就绪后会回调Camera.PreviewCallback.onPreviewFrame
//            theCamera.setOneShotPreviewCallback(previewCallback1);
//        }
//    }
//
//}
