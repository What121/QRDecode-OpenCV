package com.bestom.CameraQR;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bestom.CameraQR.Thread.Handler.MainHandler;
import com.bestom.CameraQR.camera.CameraConfigurationManager;
import com.bestom.CameraQR.camera.callback.AutoFocusManager;
import com.bestom.CameraQR.camera.callback.PreviewCallback;
import com.bestom.CameraQR.utils.BeepManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private Activity mActivity;

    SurfaceView mSurfaceView0,mSurfaceView1;
    private SurfaceHolder mSurfaceHolder0,mSurfaceHolder1;
    RelativeLayout scanContainer0,scanContainer1;
    RelativeLayout scanCropView0,scanCropView1;
    ImageView scanLine0,scanLine1;
    TextView tv_title0,tv_title1;

    ImageView mImageView;

    private BeepManager beepManager;


    private boolean permissionCamera=false;

    public boolean camera0flag=false;
    private boolean isHasSurface0=false;
    public boolean camera1flag=false;
    private boolean isHasSurface1=false;

    private Rect mCropRect = null;

    private TranslateAnimation animation=null;

    private MainHandler mainHandler;
    private Camera mCamera0,mCamera1;
    private CameraConfigurationManager mCameraConfigurationManager0,mCameraConfigurationManager1;
    private AutoFocusManager mAutoFocusManager0,mAutoFocusManager1;
    private PreviewCallback previewCallback0,previewCallback1;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 26;
    private final String TAG= CameraActivity.class.getSimpleName();

    //region SurfaceHolder0+SurfaceHolder1 SurfaceHolder.Callback
    private SurfaceHolder.Callback surfaceholdercall0=new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if (surfaceHolder == null) {
                Log.e(TAG, "*** 没有添加SurfaceHolder的Callback0");
            }
            if (!isHasSurface0) {
                isHasSurface0 = true;
                openCamera(0);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Log.e(TAG, "surface0Changed: ");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            isHasSurface0 = false;
        }
    };

    private SurfaceHolder.Callback surfaceholdercall1=new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if (surfaceHolder == null) {
                Log.e(TAG, "*** 没有添加SurfaceHolder的Callback1");
            }
            if (!isHasSurface1) {
                isHasSurface1 = true;
                openCamera(1);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Log.e(TAG, "surface1Changed: ");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            isHasSurface1 = false;
        }
    };
    //endregion

    //region BaseLoaderCallback OpenCV.loader 的回调函数
    private BaseLoaderCallback mLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //    surfaceView.setVisibility(View.VISIBLE);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        //垂直显示
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        init();
        initview();
        checkPermissionCamera();
    }

    private void init(){
        mContext=this;
        mActivity=this;
        beepManager=new BeepManager(mContext);
        mCameraConfigurationManager0=new CameraConfigurationManager(mContext);

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void initview(){
        scanContainer0=findViewById(R.id.capture0_container);
        scanContainer1=findViewById(R.id.capture1_container);
        scanCropView0=findViewById(R.id.capture0_crop_view);
        scanCropView0.setOnClickListener(this);
        scanCropView1=findViewById(R.id.capture1_crop_view);
        scanCropView1.setOnClickListener(this);
        mSurfaceView0=findViewById(R.id.capture0_preview);
        mSurfaceView1=findViewById(R.id.capture1_preview);
        mImageView=findViewById(R.id.preview_img);
        tv_title0=findViewById(R.id.tv_title0);
        tv_title1=findViewById(R.id.tv_title1);
        findViewById(R.id.capture0_imageview_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.capture1_imageview_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        scanLine0=findViewById(R.id.capture0_scan_line);
        scanLine1=findViewById(R.id.capture1_scan_line);

        //补间动画，平移
        setAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionCamera)
            initCamera();
        else
            Toast.makeText(mContext,"请打开camera权限",Toast.LENGTH_SHORT).show();
    }

    private void initCamera(){
        mCamera0=null;
        mAutoFocusManager0=null;
        mCamera1=null;
        mAutoFocusManager1=null;
        mSurfaceHolder0=mSurfaceView0.getHolder();
        mSurfaceHolder1=mSurfaceView1.getHolder();
        mSurfaceHolder0.addCallback(surfaceholdercall0);
        mSurfaceHolder1.addCallback(surfaceholdercall1);

        mainHandler=new MainHandler(this);
    }

    @Override
    protected void onPause() {
        releaseCamera();
        super.onPause();
        if (scanLine0 != null) {
            scanLine0.clearAnimation();
            scanLine0.setVisibility(View.GONE);
        }
        if (scanLine1 != null) {
            scanLine1.clearAnimation();
            scanLine1.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //remove SurfaceCallback
        if (isHasSurface0) {
            mSurfaceView0.getHolder().removeCallback(surfaceholdercall0);
        }
        if (isHasSurface1){
            mSurfaceView1.getHolder().removeCallback(surfaceholdercall1);
        }
    }

    public Handler getHandler() {
        return mainHandler;
    }

    //region camera初始化及资源回收
    private void openCamera(int CameraID) {
        if (CameraID==0){
            try {
                mCamera0=Camera.open(0);
                mCamera0.setPreviewDisplay(mSurfaceHolder0);
                mCameraConfigurationManager0.initFromCameraParameters(mCamera0);
                mCameraConfigurationManager0.setDesiredCameraParameters(mCamera0,true);
                Camera.Parameters parameters=mCamera0.getParameters();
                Log.i(TAG,"getExposureCompensationStep() "+parameters.getExposureCompensationStep() );
                Log.i(TAG,"getMinExposureCompensation() "+parameters.getMinExposureCompensation() );
                Log.i(TAG,"getMaxExposureCompensation() "+parameters.getMaxExposureCompensation() );
                Log.i(TAG,"getExposureCompensation() "+parameters.getExposureCompensation() );
                Log.i(TAG,"isSmoothZoomSupported() "+parameters.isSmoothZoomSupported() );
                Log.i(TAG,"getMaxZoom() "+parameters.getMaxZoom() );
//                parameters.setZoom(20);
                parameters.setExposureCompensation(-3);
                mCamera0.setParameters(parameters);
//                void setExposureCompensation(int)

                mCamera0.startPreview();
                mAutoFocusManager0 = new AutoFocusManager(mContext, mCamera0);
                camera0flag=true;
                scanLine0.startAnimation(animation);
                mainHandler.restartPreviewAndDecode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (CameraID==1){
//            try {
//                mCamera1=Camera.open(1);
//                mCamera1.setPreviewDisplay(mSurfaceHolder1);
//                mCamera1.startPreview();
//                mAutoFocusManager1 = new AutoFocusManager(mContext, mCamera1);
//                camera1flag=true;
//                scanLine1.startAnimation(animation);
//                mainHandler.restartPreviewAndDecode();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private void closeCamera(int CameraID) {
        if (CameraID==0){
            try {
                mCamera0.stopPreview();
                mCamera0.setPreviewDisplay(null);
                mAutoFocusManager0 =null;
                mCamera0.release();
                camera0flag=false;
                scanLine0.clearAnimation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (CameraID==1){
            try {
                mCamera1.stopPreview();
                mCamera1.setPreviewDisplay(null);
                mAutoFocusManager1 =null;
                mCamera1.release();
                camera1flag=false;
                scanLine1.clearAnimation();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseCamera() {
        if (null != mainHandler) {
            //关闭聚焦,停止预览,清空预览回调,quit子线程looper
            mainHandler.quitSynchronously();
            mainHandler = null;
        }
        //关闭声音
        if (null != beepManager) {
            Log.e(TAG, "releaseCamera: beepManager release" );
            beepManager.releaseRing();
            beepManager = null;
        }
        //关闭相机
        if (mAutoFocusManager0 != null) {
            mAutoFocusManager0.stop();
            mAutoFocusManager0 = null;
        }
        if (mAutoFocusManager1 !=null){
            mAutoFocusManager1.stop();
            mAutoFocusManager1=null;
        }
        if (mCamera0!=null){
            mCamera0.setPreviewCallback(null);
            mCamera0.stopPreview();
            mCamera0.release();
            previewCallback0.setHandler( mainHandler,null, 0);
        }
        if (mCamera1!=null){
            mCamera1.setPreviewCallback(null);
            mCamera1.stopPreview();
            mCamera1.release();
            previewCallback1.setHandler(mainHandler,null, 0);
        }
    }
    //endregion

    //region 补间动画
    private void setAnimation(){
        //补间动画主要包含平移，旋转，缩放，渐变四种动画效果
        //TranslateAnimation 为平移动画
        animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation
                .RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(10000);// 设置动画执行时间
        animation.setRepeatCount(-1);//重复次数 -1 为无数次
        animation.setRepeatMode(Animation.RESTART);//动画要重复的模式 restart 为重新开始
//        scanLine0.startAnimation(animation);
//        scanLine1.startAnimation(animation);
    }
    //endregion

    //region 检查权限
    private void checkPermissionCamera() {
        int checkPermission = 0;
        if (Build.VERSION.SDK_INT >= 23) {
            // checkPermission =ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
            checkPermission = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            } else {
                permissionCamera = true;
            }

        } else {
            checkPermission = checkPermission(26);
            if (checkPermission == AppOpsManager.MODE_ALLOWED) {
                permissionCamera = true;
            } else if (checkPermission == AppOpsManager.MODE_IGNORED) {
                permissionCamera = false;
                displayFrameworkBugMessageAndExit();
            }
        }
    }

    /**
     * 反射调用系统权限,判断权限是否打开
     *
     * @param permissionCode 相应的权限所对应的code
     * @see {@link AppOpsManager }
     */
    private int checkPermission(int permissionCode) {
        int checkPermission = 0;
        if (Build.VERSION.SDK_INT >= 19) {
            AppOpsManager _manager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class<?>[] types = new Class[]{int.class, int.class, String.class};
                Object[] args = new Object[]{permissionCode, Binder.getCallingUid(), getPackageName()};
                Method method = _manager.getClass().getDeclaredMethod("noteOp", types);
                method.setAccessible(true);
                Object _o = method.invoke(_manager, args);
                if ((_o instanceof Integer)) {
                    checkPermission = (Integer) _o;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            checkPermission = 0;
        }
        return checkPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();

                } else {
                    finish();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void displayFrameworkBugMessageAndExit() {
        String per = String.format(getString(R.string.permission), getString(R.string.camera), getString(R.string.camera));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.qr_name));
        builder.setMessage(per);
        builder.setPositiveButton(getString(R.string.i_know), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mActivity.finish();
            }
        });
        builder.show();
    }
    //endregion

    //region  初始化截取的矩形区域
//    public Rect initCrop(int decodeid) {
//        if (decodeid==R.id.decode0){
//            int cameraWidth = 0;
//            int cameraHeight = 0;
//            if (null != mCameraManager) {
//                cameraWidth = mCameraManager.getCameraResolution(0).y;
//                cameraHeight = mCameraManager.getCameraResolution(0).x;
//            }
//
//            /** 获取布局中扫描框的位置信息 */
//            int[] location = new int[2];
//            scanCropView0.getLocationInWindow(location);
//
//            int cropLeft = location[0];
//            int cropTop = location[1] - getStatusBarHeight();
//
//            int cropWidth = scanCropView0.getWidth();
//            int cropHeight = scanCropView0.getHeight();
//
//            /** 获取布局容器的宽高 */
//            int containerWidth = scanContainer0.getWidth();
//            int containerHeight = scanContainer0.getHeight();
//
//            /** 计算最终截取的矩形的左上角顶点x坐标 */
//            int x = cropLeft * cameraWidth / containerWidth;
//            /** 计算最终截取的矩形的左上角顶点y坐标 */
//            int y = cropTop * cameraHeight / containerHeight;
//
//            /** 计算最终截取的矩形的宽度 */
//            int width = cropWidth * cameraWidth / containerWidth;
//            /** 计算最终截取的矩形的高度 */
//            int height = cropHeight * cameraHeight / containerHeight;
//
//            /** 生成最终的截取的矩形 */
//            mCropRect = new Rect(x, y, width + x, height + y);
//            return new Rect(x, y, width + x, height + y);
//        }
//        if (decodeid==R.id.decode1){
//            int cameraWidth = 0;
//            int cameraHeight = 0;
//            if (null != mCameraManager) {
//                cameraWidth = mCameraManager.getCameraResolution(1).y;
//                cameraHeight = mCameraManager.getCameraResolution(1).x;
//            }
//
//            /** 获取布局中扫描框的位置信息 */
//            int[] location = new int[2];
//            scanCropView0.getLocationInWindow(location);
//
//            int cropLeft = location[0];
//            int cropTop = location[1] - getStatusBarHeight();
//
//            int cropWidth = scanCropView1.getWidth();
//            int cropHeight = scanCropView1.getHeight();
//
//            /** 获取布局容器的宽高 */
//            int containerWidth = scanContainer1.getWidth();
//            int containerHeight = scanContainer1.getHeight();
//
//            /** 计算最终截取的矩形的左上角顶点x坐标 */
//            int x = cropLeft * cameraWidth / containerWidth;
//            /** 计算最终截取的矩形的左上角顶点y坐标 */
//            int y = cropTop * cameraHeight / containerHeight;
//
//            /** 计算最终截取的矩形的宽度 */
//            int width = cropWidth * cameraWidth / containerWidth;
//            /** 计算最终截取的矩形的高度 */
//            int height = cropHeight * cameraHeight / containerHeight;
//
//            /** 生成最终的截取的矩形 */
//            mCropRect = new Rect(x, y, width + x, height + y);
//            return new Rect(x, y, width + x, height + y);
//        }
//        return null;
//    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    //endregion

    //region 获取扫描数据
    /* 两个绑定操作：<br/>
     * 1：将handler与回调函数绑定；<br/>
     * 2：将相机与回调函数绑定<br/>
     * 综上，该函数的作用是当相机的预览界面准备就绪后就会调用hander向其发送传入的message
     * @param handler     解码的子线程.
     * @param message     R.id.decode.
     */
    public synchronized void requestPreview0Frame(Handler handler, int message) {
        if ( camera0flag) {
            previewCallback0=new PreviewCallback();
            previewCallback0.setHandler(mainHandler,handler, message);
            // 绑定相机回调函数，当预览界面准备就绪后会回调Camera.PreviewCallback.onPreviewFrame
            mCamera0.setOneShotPreviewCallback(previewCallback0);
        }
    }

    public synchronized void requestPreview1Frame(Handler handler, int message) {
        if ( camera1flag ) {
            previewCallback1=new PreviewCallback();
            previewCallback1.setHandler(mainHandler,handler, message);
            // 绑定相机回调函数，当预览界面准备就绪后会回调Camera.PreviewCallback.onPreviewFrame
            mCamera1.setOneShotPreviewCallback(previewCallback1);
        }
    }
    //endregion

    //region 扫描结果

    public void setbitmap(Bitmap bitmap){
        mImageView.setImageBitmap(bitmap);
    }

    public void checkResult(int decodeid, final String result) {
        final int decodid=decodeid;
        if (beepManager != null) {
            beepManager.startRing();
        }
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                activityResult(decodid,result.trim());
            }
        }, beepManager.getTimeDuration());
    }

    private void activityResult(int decodeid, String result) {
        if (decodeid== R.id.decode_succeeded0){
            synchronized (this){
                Log.i(TAG,"success_decode 0");
                tv_title0.setText(result);
                mainHandler.postDelayed((new Runnable() {
                    @Override
                    public void run() {
                        tv_title0.setText(R.string.title_0);
                        mainHandler.restartPreviewAndDecode();
                    }
                }),2000 );
            }
        }

        if (decodeid== R.id.decode_succeeded1){
            synchronized (this){
                Log.i(TAG,"success_decode 1");
                tv_title1.setText(result);
                mainHandler.postDelayed((new Runnable() {
                    @Override
                    public void run() {
                        tv_title1.setText(R.string.title_1);
                        mainHandler.restartPreviewAndDecode();
                    }
                }),2000 );
            }
        }
    }
    //endregion

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.capture0_crop_view:
                Log.i(TAG,"click*****0");
                if (camera0flag){
                    closeCamera(0);
                }else {
                    openCamera(0);
                }
                break;
            case R.id.capture1_crop_view:
                Log.i(TAG,"click*****1");
                if (camera1flag){
                    closeCamera(1);
                }else {
                    openCamera(1);
                }
                break;
            default:
                break;
        }
    }
}
