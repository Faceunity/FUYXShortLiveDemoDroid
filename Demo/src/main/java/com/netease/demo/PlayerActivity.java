package com.netease.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.transcoding.player.MediaPlayerAPI;
import com.netease.transcoding.demo.R;
import com.netease.vcloud.video.render.NeteaseView;

import static com.netease.transcoding.TranscodingAPI.TranFilter.BRIGHTNESS_DEFAULT;
import static com.netease.transcoding.TranscodingAPI.TranFilter.CONTRAST_DEFAULT;
import static com.netease.transcoding.TranscodingAPI.TranFilter.HUE_DEFAULT;
import static com.netease.transcoding.TranscodingAPI.TranFilter.SATURATION_DEFAULT;
import static com.netease.transcoding.TranscodingAPI.TranFilter.SHARPEN_DEFAULT;


public class PlayerActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    private SeekBar seekBarHue, seekBarSharpen, seekBarContrast, seekBarBrightness, seekBarSaturation,seekBarVolume;
    private String[] mFileNames = {"/sdcard/transcode/media1.mp4","/sdcard/transcode/media2.mp4","/sdcard/transcode/media3.mp4"};
    private float mBrightnessAdjustLevel = BRIGHTNESS_DEFAULT;
    private float mContrastAdjustLevel = CONTRAST_DEFAULT;
    private float mSaturationAdjustLevel = SATURATION_DEFAULT;
    private float mSharpenAdjustLevel = SHARPEN_DEFAULT;
    private float mHueAdjustLevel = HUE_DEFAULT;
    private float mVolume = 0.5f;
    private MediaPlayerAPI mPlayer;
    private NeteaseView mPlayerView;

    private TextView mFpsView;
    private Handler mHandler;
    private final int MSG_FPS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        View start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPlayer != null){
                    boolean play = mPlayer.start();
                    if(!play){
                        Toast.makeText(getApplication(),"不支持的视频格式或分辨率",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        View save = findViewById(R.id.savebutton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayer();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(PlayerActivity.this, DemoActivity.class);
                        intent.putExtra(ShortVideoProcessFragment.ARG_BRIGHTNESS, mBrightnessAdjustLevel);
                        intent.putExtra(ShortVideoProcessFragment.ARG_CONTRAST, mContrastAdjustLevel);
                        intent.putExtra(ShortVideoProcessFragment.ARG_SATURATION, mSaturationAdjustLevel);
                        intent.putExtra(ShortVideoProcessFragment.ARG_SHARPEN, mSharpenAdjustLevel);
                        intent.putExtra(ShortVideoProcessFragment.ARG_HUE, mHueAdjustLevel);
                        intent.putExtra("setAdjust",true);
                        startActivity(intent);
                        PlayerActivity.this.finish();
                    }
                },500);

            }
        });

        seekBarHue = ((SeekBar) findViewById(R.id.seekBar_hue));
        seekBarHue.setOnSeekBarChangeListener(this);

        seekBarSharpen = ((SeekBar) findViewById(R.id.seekBar_sharpen));
        seekBarSharpen.setOnSeekBarChangeListener(this);

        seekBarContrast = ((SeekBar) findViewById(R.id.seekBar_contrast));
        seekBarContrast.setOnSeekBarChangeListener(this);

        seekBarBrightness = ((SeekBar) findViewById(R.id.seekBar_brightness));
        seekBarBrightness.setOnSeekBarChangeListener(this);

        seekBarSaturation = ((SeekBar) findViewById(R.id.seekBar_saturation));
        seekBarSaturation.setOnSeekBarChangeListener(this);

        seekBarVolume = (SeekBar) findViewById(R.id.seekBar_volume);
        seekBarVolume.setOnSeekBarChangeListener(this);

        //2、创建播放器显示的view
        mPlayerView = (NeteaseView) findViewById(R.id.playerView);
        mPlayer = MediaPlayerAPI.getInstance();
        mPlayer.init(getApplicationContext(),mFileNames,mPlayerView);


        mFpsView = (TextView) findViewById(R.id.fps_text);
        createFPSHandler();
        mHandler.sendEmptyMessageDelayed(MSG_FPS,1000);

    }


    private void stopPlayer(){
        if(mPlayer != null){
            mPlayer.stop();
            mPlayer.unInit();
        }
        mPlayer = null;
    }

    public void onDestroy() {
        stopPlayer();
        if(mHandler != null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    private void createFPSHandler(){ //测试用
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == MSG_FPS && mPlayerView != null){
                    mFpsView.setText("render fps: " + mPlayerView.getRenderFps());
                    sendEmptyMessageDelayed(MSG_FPS,2000);
                }
            }
        };
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if(seekBar == seekBarHue) {
            mHueAdjustLevel = (float)(progress*1.0);
            if(mPlayer != null){
                mPlayer.setHue(mHueAdjustLevel);//色相（0---360）
            }
        }
        else if(seekBar == seekBarSharpen) {
            mSharpenAdjustLevel = (float)((progress-50)*1.0/12.5);
            if(mPlayer != null){
                mPlayer.setSharpen(mSharpenAdjustLevel);//锐化（-4---4）
            }
        }
        else if(seekBar == seekBarContrast) {
            mContrastAdjustLevel = (float)(progress*1.0/25);
            if(mPlayer != null){
                mPlayer.setContrast(mContrastAdjustLevel);//对比度（0---4）
            }
        }
        else if(seekBar == seekBarBrightness) {
            mBrightnessAdjustLevel = (float)((progress-50)*1.0/50);
            if(mPlayer != null){
                mPlayer.setBrightness(mBrightnessAdjustLevel);//亮度（-1---1）
            }
        }
        else if(seekBar == seekBarSaturation) {
            mSaturationAdjustLevel = (float)(progress*1.0/50);
            if(mPlayer != null){
                mPlayer.setSaturation(mSaturationAdjustLevel);//饱和度（0---2）
            }
        }
        else if(seekBar == seekBarVolume){
            mVolume = (float)(progress*1.0/100);
            if(mPlayer != null){
                mPlayer.setVolume(mVolume);//原声大小（0---1）
            }
        }
    }

}
