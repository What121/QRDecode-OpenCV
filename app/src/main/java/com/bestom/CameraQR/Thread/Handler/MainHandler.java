package com.bestom.CameraQR.Thread.Handler;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.bestom.CameraQR.CameraActivity;
import com.bestom.CameraQR.R;
import com.bestom.CameraQR.Thread.DecodeThread;

import static com.bestom.CameraQR.R.id.restart_preview;


/**
 * desc:主线程的Handler
 * Author: znq
 * Date: 2016-11-03 15:55
 */
public class MainHandler extends Handler {
    private static final String TAG = "MainHandler";

    private final CameraActivity activity;

    /**
     * 真正负责扫描任务的核心线程
     */
    private final DecodeThread decodeThread0,decodeThread1;

    private State state0,state1;

    public MainHandler(CameraActivity activity) {
        this.activity = activity;
        // 启动扫描线程
        decodeThread0 = new DecodeThread(activity);
        decodeThread1=new DecodeThread(activity) ;
        decodeThread0.start();
        decodeThread1.start();
        state0 = State.SUCCESS;
        state1 = State.SUCCESS;

        restartPreviewAndDecode();
    }

    /**
     * 当前扫描的状态
     */
    private enum State {
        /**
         * 预览
         */
        PREVIEW,
        /**
         * 扫描成功
         */
        SUCCESS,
        /**
         * 结束扫描
         */
        DONE
    }


    @Override
    public void handleMessage(Message msg) {
        if (msg.what == R.id.decode_succeeded0 ||msg.what == R.id.decode_succeeded1 ) {
            int decodeid=msg.what;
            String result = (String) msg.obj;
            if (!TextUtils.isEmpty(result)) {
                if (msg.what==R.id.decode_succeeded0){
                    state0=State.SUCCESS;
                }
                if (msg.what==R.id.decode_succeeded1){
                    state1=State.SUCCESS;
                }
                activity.checkResult(decodeid,result);
            }
        } else if (msg.what == restart_preview) {
            restartPreviewAndDecode();
        } else if (msg.what == R.id.decode_failed0) {
            // We're decoding as fast as possible, so when one decode fails,
            // start another.
            state0 = State.PREVIEW;
            activity.requestPreview0Frame(decodeThread0.getHandler(),
                    R.id.decode0);
//            state0=State.SUCCESS;
//            restartPreviewAndDecode();
        }else if (msg.what== R.id.decode_failed1){
            state1 = State.PREVIEW;
            activity.requestPreview1Frame(decodeThread1.getHandler(),
                    R.id.decode1);
        }else if (msg.what==R.id.setimg){
            int decodeid=msg.what;
            Bitmap bitmap = (Bitmap) msg.obj;
            activity.setbitmap(bitmap);
        }
    }

    /**
     * 完成一次扫描后，只需要再调用此方法即可
     */
    public void restartPreviewAndDecode() {
        state0 = State.SUCCESS;
        state1=State.SUCCESS;
        if (state0 == State.SUCCESS) {
            if (activity.camera0flag){
                state0 = State.PREVIEW;
                // 向decodeThread绑定的handler（DecodeHandler)发送解码消息
                activity.requestPreview0Frame(decodeThread0.getHandler(),
                        R.id.decode0);
            }
        }
        if (state1 == State.SUCCESS) {
            if (activity.camera1flag){
                state1 = State.PREVIEW;
                // 向decodeThread绑定的handler（DecodeHandler)发送解码消息
                activity.requestPreview1Frame(decodeThread1.getHandler(),
                        R.id.decode1);
            }
        }
    }

    public void quitSynchronously() {
        state0 = State.DONE;
        state1= State.DONE;
        Message quit0 = Message.obtain(decodeThread0.getHandler(), R.id.quit);
        quit0.sendToTarget();
        Message quit1=Message.obtain(decodeThread1.getHandler(), R.id.quit);
        quit1.sendToTarget();

        try {
            // Wait at most half a second; should be enough time, and onPause()
            // will timeout quickly
            decodeThread0.join(500L);
            decodeThread1.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded0);
        removeMessages(R.id.decode_succeeded1);
        removeMessages(R.id.decode_failed0);
        removeMessages(R.id.decode_failed1);
    }



}
