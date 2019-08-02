package com.netease.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.faceunity.beautycontrolview.BeautyControlView;
import com.faceunity.beautycontrolview.EffectEnum;
import com.faceunity.beautycontrolview.FURenderer;
import com.netease.demo.utils.PreferenceUtil;
import com.netease.demo.widget.MixAudioDialog;
import com.netease.transcoding.demo.R;
import com.netease.transcoding.record.AudioCallback;
import com.netease.transcoding.record.MediaRecord;
import com.netease.transcoding.record.MessageHandler;
import com.netease.transcoding.record.VideoCallback;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.render.NeteaseView;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


//由于直播推流的URL地址较长，可以直接在代码中的mliveStreamingURL设置直播推流的URL
public class LiveStreamingActivity extends Activity implements MessageHandler {

    private static final String TAG = "LiveStreamingActivity";
    private DateFormat formatter_file_name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
    //Demo控件
    private View filterLayout;
    private ImageView mRecordBtn;
    //视频缩放相关变量
    private int mMaxZoomValue = 0;
    private int mCurrentZoomValue = 0;
    private float mCurrentDistance;
    private float mLastDistance = -1;
    private int cameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;

    //伴音广播
    private audioMixVolumeMsgReceiver audioMixVolumeMsgReceiver;

    /**
     * SDK 相关参数
     **/
    private MediaRecord mMediaRecord = null;
    private volatile boolean mRecording = false;

    //第三方滤镜
    private BeautyControlView mBeautyControlView;
    private FURenderer mFURenderer; //FU的滤镜
    private String isOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "activity onCreate");

        setContentView(R.layout.activity_livestreaming);
        //应用运行时，保持屏幕高亮，不锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 0.7f;
        getWindow().setAttributes(params);

        //以下为SDK调用主要步骤，请用户参考使用
        //1、创建录制实例
        MediaRecord.MediaRecordPara mediaRecordPara = new MediaRecord.MediaRecordPara();
        mediaRecordPara.setAppKey(TestAppkey.APP_KEY);  //APPKEY
        mediaRecordPara.setContext(getApplicationContext()); //APP上下文
        mediaRecordPara.setMessageHandler(this); //消息回调
        mMediaRecord = new MediaRecord(mediaRecordPara);

        boolean frontCamera = true; // 是否前置摄像头
        boolean scale_16x9 = false; //是否强制16:9

        //设置第三方滤镜，需要在开启相机前设置，即在startVideoPreview 前设置
        mBeautyControlView = (BeautyControlView) findViewById(R.id.faceunity_control);
        fuLiveEffect(); //FU滤镜

        //麦克风采集回调，需在startRecord之前设置
//        audioEffect();

        //2、 预览参数设置
        NeteaseView videoView = (NeteaseView) findViewById(R.id.videoview);

        MediaRecord.VideoQuality videoQuality = MediaRecord.VideoQuality.SUPER_HIGH; //视频模板（SUPER_HIGH 1280*720、SUPER 960*540、HIGH 640*480、MEDIUM 480*360）
        mMediaRecord.startVideoPreview(videoView, frontCamera, videoQuality, scale_16x9);
        mMediaRecord.setBeautyLevel(0); //磨皮强度为5,共5档，0为关闭
        mMediaRecord.setFilterStrength(0.0f); //滤镜强度
        mMediaRecord.setFilterType(VideoEffect.FilterType.none);

        // SDK 默认提供 /** 标清 480*360 */MEDIUM, /** 高清 640*480 */HIGH,
        // /** 超清 960*540 */SUPER,/** 超高清 (1280*720) */SUPER_HIGH  五个模板，
        // 用户如果需要自定义分辨率可以调用startVideoPreviewEx 接口并参考以下参数
        // 码率计算参考公式为 bitrate = width * height * fps * 11 /100;
//		MediaRecord.VideoPara para = new MediaRecord.VideoPara();
//		para.setHeight(720);
//		para.setWidth(1280);
//		para.setFps(30);
//		para.setBitrate(1200 * 720 * 30 * 11 /100);
//		mMediaRecord.startVideoPreviewEx(videoView,frontCamera,useFilter,para);


        //Demo控件的初始化（Demo层实现，用户不需要添加该操作）
        UIInit();

        audioMixVolumeMsgReceiver = new audioMixVolumeMsgReceiver();
        IntentFilter audioMixVolumeIntentFilter = new IntentFilter();
        audioMixVolumeIntentFilter.addAction("AudioMixVolume");
        audioMixVolumeIntentFilter.addAction("AudioMix");
        registerReceiver(audioMixVolumeMsgReceiver, audioMixVolumeIntentFilter);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Activity onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Activity onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "activity onDestroy");
        if (mMediaRecord != null) {
            if (mRecording) {
                mMediaRecord.stopRecord();
            }
            mMediaRecord.stopVideoPreview();
            //消耗第三方滤镜
            releaseFuEffect();

            mMediaRecord.destroyVideoPreview();
            mMediaRecord.unInit();
        }
        unregisterReceiver(audioMixVolumeMsgReceiver);
        super.onDestroy();
    }

    //处理SDK抛上来的异常和事件，用户需要在这里监听各种消息，进行相应的处理。
    @Override
    public void handleMessage(int msg, Object object) {
        switch (msg) {
            case MSG_INIT_RECORD_VERIFY_ERROR:
                showToast("鉴权失败，请检查APPkey");
                finish();
                break;
            case MSG_START_PREVIEW_FINISHED:
                Log.d(TAG, "开启预览成功");
                break;
            case MSG_START_CAMERA_ERROR:
                showToast("开启相机失败，请检查相机权限");
                finish();
                break;
            case MSG_START_AUDIO_ERROR:
                showToast("开启录音失败，请检查麦克风权限");
                finish();
                break;
            case MSG_START_RECORD_ERROR:
                showToast("开启录制失败");
                break;
            case MSG_START_RECORD_FINISHED:
                showToast("录制已开启");
                mRecording = true;
                mRecordBtn.setClickable(true);
                break;
            case MSG_STOP_RECORD_FINISHED:
                if (!(Boolean) object) {
                    showToast("录制停止失败，删除录制文件");
                } else {
                    showToast("录制已停止");
                }
                mRecording = false;
                mRecordBtn.setClickable(true);
                break;
            case MSG_SWITCH_CAMERA_FINISHED:
                showToast("相机切换成功");
                cameraType = cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
                break;
            case MSG_CAMERA_NOT_SUPPORT_FLASH:
                showToast("不支持闪光灯");
                break;
            case MSG_START_CAPTURE_FINISHED:
                final Bitmap bitmap = (Bitmap) object;
                new Thread() {
                    public void run() {
                        if (bitmap != null) {
                            FileOutputStream outStream = null;
                            String screenShotFilePath = Environment.getExternalStorageDirectory() + "/transcode/" +
                                    formatter_file_name.format(new Date()) + "_" +
                                    bitmap.getWidth() + "x" + bitmap.getHeight() +
                                    ".png";
                            try {
                                outStream = new FileOutputStream(String.format(screenShotFilePath));
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                                showToast("截图已保存到：" + screenShotFilePath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (outStream != null) {
                                    try {
                                        outStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }.start();

                break;
            default:
                break;
        }
    }


    private Toast mToast;

    private void showToast(final String text) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                }
                mToast.setText(text);
                mToast.show();
            }
        });
    }

    long clickTime = 0L;

    //按钮初始化
    private void UIInit() {

        //开始直播按钮初始化
        mRecordBtn = (ImageView) findViewById(R.id.live_start_btn);
        mRecordBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                long time = System.currentTimeMillis();
                if (time - clickTime < 1000) {
                    return;
                }
                clickTime = time;
                mRecordBtn.setClickable(false);
                if (!mRecording) {
                    mMediaRecord.startRecord(Environment.getExternalStorageDirectory() + "/transcode/" + formatter_file_name.format(new Date()) + ".mp4");
                    mRecordBtn.setImageResource(R.drawable.stop);
                } else {
                    mMediaRecord.stopRecord();
                    mRecordBtn.setImageResource(R.drawable.restart);
                }
            }
        });

        View capture = findViewById(R.id.live_capture_btn);
        capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startCapture();
            }
        });

        //切换前后摄像头按钮初始化
        View switchBtn = findViewById(R.id.live_camera_btn);
        switchBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        View flashBtn = findViewById(R.id.live_flash_btn);
        flashBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                flashCamera();
            }
        });

        //伴音按钮初始化
        View mix_audio_button = findViewById(R.id.live_music_btn);
        mix_audio_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showMixAudioDialog();
            }
        });

        View change = findViewById(R.id.live_camera_change);
        change.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFormat();
            }
        });

    }

    private void showMixAudioDialog() {
        MixAudioDialog dialog = new MixAudioDialog(this);
        dialog.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    //拍照，拍照成功后通过SDK消息回调中的MSG_START_CAPTURE_FINISHED 获取bitmap
    private void startCapture() {
        if (mMediaRecord != null) {
            boolean capture = mMediaRecord.startCapture();
            if (capture) {
                showToast("截图成功，正在保存图片");
            } else {
                showToast("截图失败，上次截图未完成或SDK没有初始化");
            }
        }
    }

    //切换前后摄像头
    private void switchCamera() {
        if (mMediaRecord != null) {
            mMediaRecord.switchCamera();
        }
    }

    private boolean mFlashOn = false;

    private void flashCamera() {
        if (mMediaRecord != null) {
            mFlashOn = !mFlashOn;
            mMediaRecord.setCameraFlashPara(mFlashOn);
        }
    }

    //切换分辨率
    int count = 0;

    private void changeFormat() {
        if (mMediaRecord == null) {
            return;
        }
        int index = count % 4;
        count++;
        boolean is16x9 = true;
        switch (index) {
            case 0:
                mMediaRecord.changeCaptureFormat(MediaRecord.VideoQuality.SUPER_HIGH, is16x9);
                break;
            case 1:
                mMediaRecord.changeCaptureFormat(MediaRecord.VideoQuality.SUPER, is16x9);
                break;
            case 2:
                mMediaRecord.changeCaptureFormat(MediaRecord.VideoQuality.HIGH, is16x9);
                break;
            case 3:
                mMediaRecord.changeCaptureFormat(MediaRecord.VideoQuality.MEDIUM, is16x9);
                break;
        }
    }


    //Demo层视频缩放和摄像头对焦操作相关方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Log.i(TAG, "test: down!!!");
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.i(TAG, "test: move!!!");
                /*
                 * 首先判断按下手指的个数是不是大于两个。
                 * 如果大于两个则执行以下操作（即图片的缩放操作）。
                 */
                if (event.getPointerCount() >= 2) {

                    float offsetX = event.getX(0) - event.getX(1);
                    float offsetY = event.getY(0) - event.getY(1);
                    /*
                     * 原点和滑动后点的距离差
                     */
                    mCurrentDistance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
                    if (mLastDistance < 0) {
                        mLastDistance = mCurrentDistance;
                    } else {
                        if (mMediaRecord != null) {
                            mMaxZoomValue = mMediaRecord.getCameraMaxZoomValue();
                            mCurrentZoomValue = mMediaRecord.getCameraZoomValue();
                        }

                        /*
                         * 如果当前滑动的距离（currentDistance）比最后一次记录的距离（lastDistance）相比大于5英寸（也可以为其他尺寸），
                         * 那么现实图片放大
                         */
                        if (mCurrentDistance - mLastDistance > 5) {
                            //Log.i(TAG, "test: 放大！！！");
                            mCurrentZoomValue += 2;
                            if (mCurrentZoomValue > mMaxZoomValue) {
                                mCurrentZoomValue = mMaxZoomValue;
                            }

                            if (mMediaRecord != null) {
                                mMediaRecord.setCameraZoomPara(mCurrentZoomValue);
                            }

                            mLastDistance = mCurrentDistance;
                            /*
                             * 如果最后的一次记录的距离（lastDistance）与当前的滑动距离（currentDistance）相比小于5英寸，
                             * 那么图片缩小。
                             */
                        } else if (mLastDistance - mCurrentDistance > 5) {
                            //Log.i(TAG, "test: 缩小！！！");
                            mCurrentZoomValue -= 2;
                            if (mCurrentZoomValue < 0) {
                                mCurrentZoomValue = 0;
                            }
                            if (mMediaRecord != null) {
                                mMediaRecord.setCameraZoomPara(mCurrentZoomValue);
                            }

                            mLastDistance = mCurrentDistance;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //Log.i(TAG, "test: up!!!");
                if (filterLayout != null) {
                    filterLayout.setVisibility(View.GONE);
                }

                //调用摄像头对焦操作相关API
                if (mMediaRecord != null) {
                    mMediaRecord.setCameraFocus(event.getRawX(), event.getRawY(), 200);
                    //区域对焦回调函数，开发者可根据返回的区域自行绘制聚焦框， 如果不需要绘制，则无需设置
//                    mMediaRecord.setAreaFocusCallback(new CameraVideoCapturer.AreaFocusCallback() {
//                        @Override
//                        public void FocusArea(Rect rect) {
//                            Log.d(TAG,String.format(Locale.getDefault(),"area focus = (%d,%d,%d,%d)",rect.left,rect.top,rect.right,rect.bottom));
//                        }
//                    });
                }

                break;
            default:
                break;
        }
        return true;
    }


    //用于接收Service发送的消息，伴音音量
    public class audioMixVolumeMsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //音量
            float audioMixVolumeMsg = intent.getFloatExtra("AudioMixVolumeMSG", -1);
            if (audioMixVolumeMsg != -1 && mMediaRecord != null) {
                mMediaRecord.setMusicVolume(audioMixVolumeMsg);
            }

            //伴音文件
            int audioMixMsg = intent.getIntExtra("AudioMixMSG", 0);
            String fileName = intent.getStringExtra("AudioMixFilePathMSG");

            //伴音开关的控制
            if (audioMixMsg == 1) {
                if (mMediaRecord != null) {
                    try {
                        AssetFileDescriptor descriptor = context.getAssets().openFd("mixAudio/" + fileName);
                        FileDescriptor fd = descriptor.getFileDescriptor();
                        mMediaRecord.startPlayMusic(fd, descriptor.getStartOffset(), descriptor.getLength(), false);
                        mMediaRecord.setMusicVolume(0.2f);
//                        mMediaRecord.musicSeekTo(1000); //伴音seek
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else if (audioMixMsg == 2) {
                if (mMediaRecord != null) {
                    mMediaRecord.resumePlayMusic();
                }
            } else if (audioMixMsg == 3) {
                if (mMediaRecord != null) {
                    mMediaRecord.pausePlayMusic();
                }
            } else if (audioMixMsg == 4) {
                if (mMediaRecord != null) {
                    mMediaRecord.stopPlayMusic();
                }
            }
        }
    }

    private void audioEffect() {
        mMediaRecord.setAudioRawDataCB(new AudioCallback() {
            int i = 0;

            @Override
            public void onAudioCapture(byte[] data, int len, int sampleRateInHz, int channelConfig, int audioFormat) {
                // 这里将data直接修改，SDK根据修改后的data数据编码合成
                if (i % 10 == 0) {
                    for (int j = 0; j < 1000; j++) {
                        data[j] = 0;
                    }
                }
                i++;
            }
        });
    }

    //FU的滤镜
    private void fuLiveEffect() {
        isOpen = PreferenceUtil.getString(CrashApplication.getInstance(),
                PreferenceUtil.KEY_FACEUNITY_ISON);
        if (isOpen.equals("false")) {
            mBeautyControlView.setVisibility(View.GONE);
        }
        mMediaRecord.setCameraBufferNum(1);//将相机采集的buffer数量设置为1，防止faceu在部分性能差的手机上出现闪屏
        if (isOpen.equals("true")) {
            mMediaRecord.setCaptureRawDataCB(new VideoCallback() {
                @Override
                public int onVideoCapture(byte[] data, int textureId, int width, int height, int orientation) {
                    //SDK回调的线程已经创建了GLContext
                    int fuTex = textureId;
                    if (mFURenderer == null) {
                        mFURenderer = new FURenderer
                                .Builder(LiveStreamingActivity.this)
                                .inputTextureType(1)
                                .createEGLContext(false)
                                .needReadBackImage(false)
                                .setNeedFaceBeauty(true)
                                .build();
                        mBeautyControlView.setOnFaceUnityControlListener(mFURenderer);
                        mFURenderer.onSurfaceCreated();
                    }
                    mFURenderer.onCameraChange(cameraType, orientation);
                    fuTex = mFURenderer.onDrawFrameDoubleInput(data, textureId, width, height);
                    return fuTex;
                }
            });
        }

    }

    private void releaseFuEffect() {
        if (mFURenderer != null) {
            mMediaRecord.postOnGLThread(new Runnable() {
                @Override
                public void run() {
                    mFURenderer.onSurfaceDestroyed();
                    mFURenderer = null;
                }
            });
        }
    }

}
