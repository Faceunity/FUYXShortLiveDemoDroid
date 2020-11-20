package com.netease.demo;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.netease.transcoding.TranscodingAPI;
import com.netease.transcoding.TranscodingNative;
import com.netease.transcoding.demo.R;
import com.netease.vcloud.video.effect.VideoEffect;

import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.netease.transcoding.TranscodingAPI.TRAN_MIX_FILE_PARSE_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_OUT_FILE_CREATE_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_PARA_NULL;
import static com.netease.transcoding.TranscodingAPI.TRAN_PRE_IS_NOT_FINISH;
import static com.netease.transcoding.TranscodingAPI.TRAN_PROCESS_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_SOURCE_FILE_PARSE_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_SOURCE_NO_VIDEO_OR_AUDIO;


public class ShortVideoProcessFragment extends BaseFragment {

    public static final String ARG_BRIGHTNESS = "brightness";
    public static final String ARG_CONTRAST = "contrast";
    public static final String ARG_SATURATION = "saturation";
    public static final String ARG_SHARPEN = "sharpen";
    public static final String ARG_HUE = "hue";

    private static final String ARG_ADJUST = "adjust";

    //输入视频文件
    private final int ShortVideoProcess_2_CHOOSE = 106;
    private final int ShortVideoProcess_3_CHOOSE = 107;
    private final int ShortVideoProcess_4_CHOOSE = 108;


    //混音文件
    private final int ShortVideoProcess_6_CHOOSE = 110;

    //输出视频文件
    private final int ShortVideoProcess_OUT_CHOOSE = 115;

    private EditText mShortVideoProcess_multi_source_1;
    private EditText mShortVideoProcess_multi_source_2;
    private EditText mShortVideoProcess_multi_source_3;
    private EditText mMulti_input_file_fade_time;
    private EditText mMulti_input_file_target_width;
    private EditText mMulti_input_file_target_height;

    private EditText mCrop_x_pos;
    private EditText mCrop_y_pos;
    private EditText mCrop_width;
    private EditText mCrop_height;

    private EditText mScale_ratio;

    private EditText mWatermark_x_pos;
    private EditText mWatermark_y_pos;
    private EditText mWatermark_offset;
    private EditText mWatermark_out_time;
    private EditText mWatermark_rect;

    private EditText mChartlet_frquency;
    private EditText mChartlet_x_pos;
    private EditText mChartlet_y_pos;
    private EditText mChartlet_offset;
    private EditText mChartlet_duration;
    private EditText mChartlet_rect;

    private EditText mFile_offset;
    private EditText mFile_duration;

    private EditText mAudioMerge_file;
    private EditText mVideo_adjust_volume;
    private EditText mAudioMerge_audio_volume;
    private EditText mAudioMerge_fade_time;

    private EditText mShortVideoProcess_out_file;
    private AsyncTask mShortVideoProcessTask;

    public ShortVideoProcessFragment() {
        // Required empty public constructor
    }

    public static ShortVideoProcessFragment newInstance(float brightness, float contrast, float saturation, float sharpen, float hue,boolean adjust){
        ShortVideoProcessFragment fragment = new ShortVideoProcessFragment();
        Bundle args = new Bundle();
        args.putFloat(ARG_BRIGHTNESS, brightness);
        args.putFloat(ARG_CONTRAST, contrast);
        args.putFloat(ARG_SATURATION, saturation);
        args.putFloat(ARG_SHARPEN, sharpen);
        args.putFloat(ARG_HUE, hue);
        args.putBoolean(ARG_ADJUST,adjust);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View parent = inflater.inflate(R.layout.fragment_shortvideo, container, false);
        initUI(parent);
        return parent;
    }



    @Override
    public void onDetach() {
        if(mShortVideoProcessTask != null && mShortVideoProcessTask.getStatus() != AsyncTask.Status.FINISHED){
            mShortVideoProcessTask.cancel(true);
        }
        if(mShortVideoProcessTask != null &&!mShortVideoProcessTask.isCancelled()){
            mShortVideoProcessTask.cancel(true);
        }
        mShortVideoProcessTask = null;
        mShortVideoProcess_out_file = null;
        mShortVideoProcess_multi_source_1 = null;
        mShortVideoProcess_multi_source_2 = null;
        mShortVideoProcess_multi_source_3 = null;
        mMulti_input_file_fade_time = null;
        mMulti_input_file_target_width = null;
        mMulti_input_file_target_height = null;
        mCrop_x_pos = null;
        mCrop_y_pos = null;
        mCrop_width = null;
        mCrop_height = null;
        mScale_ratio = null;
        mWatermark_x_pos = null;
        mWatermark_y_pos = null;
        mWatermark_offset = null;
        mWatermark_out_time = null;
        mWatermark_rect = null;
        mChartlet_frquency = null;
        mChartlet_x_pos = null;
        mChartlet_y_pos = null;
        mChartlet_offset = null;
        mChartlet_duration = null;
        mChartlet_rect = null;
        mFile_offset = null;
        mFile_duration = null;
        mAudioMerge_file = null;
        mVideo_adjust_volume = null;
        mAudioMerge_audio_volume = null;
        mAudioMerge_fade_time = null;
        TranscodingAPI.getInstance().unInit();

        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data.getData() == null) {
            return;
        }
        String path = FileUtil.getPath(this.getContext(), data.getData());
        switch (requestCode){
            case ShortVideoProcess_2_CHOOSE:      //多输入视频文件1
                mShortVideoProcess_multi_source_1.setText(path);
                break;
            case ShortVideoProcess_3_CHOOSE:      //多输入视频文件2
                mShortVideoProcess_multi_source_2.setText(path);
                break;
            case ShortVideoProcess_4_CHOOSE:      //多输入视频文件3
                mShortVideoProcess_multi_source_3.setText(path);
                break;
            case ShortVideoProcess_6_CHOOSE:      //混音文件
                mAudioMerge_file.setText(path);
                break;
            case ShortVideoProcess_OUT_CHOOSE:    //输出视频文件
                mShortVideoProcess_out_file.setText(path);
                break;
            default:
                break;
        }
    }
    
    private void initUI(View view){

        mShortVideoProcess_multi_source_1 = (EditText) view.findViewById(R.id.multi_input_file_source_1);
        View shortvideo_2_choose = view.findViewById(R.id.multi_input_file_source_1_choose);
        shortvideo_2_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoose(ShortVideoProcess_2_CHOOSE);
            }
        });
        mShortVideoProcess_multi_source_1.setText("/sdcard/transcode/media1.mp4");

        mShortVideoProcess_multi_source_2 = (EditText) view.findViewById(R.id.multi_input_file_source_2);
        View shortvideo_3_choose = view.findViewById(R.id.multi_input_file_source_2_choose);
        shortvideo_3_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoose(ShortVideoProcess_3_CHOOSE);
            }
        });
        mShortVideoProcess_multi_source_2.setText("/sdcard/transcode/media2.mp4");

        mShortVideoProcess_multi_source_3 = (EditText) view.findViewById(R.id.multi_input_file_source_3);
        View shortvideo_4_choose = view.findViewById(R.id.multi_input_file_source_3_choose);
        shortvideo_4_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoose(ShortVideoProcess_4_CHOOSE);
            }
        });
        mShortVideoProcess_multi_source_3.setText("/sdcard/transcode/media3.mp4");

        mMulti_input_file_fade_time = (EditText) view.findViewById(R.id.multi_input_file_fade_time);
        mMulti_input_file_fade_time.setText("500");
        mMulti_input_file_target_width = (EditText) view.findViewById(R.id.multi_input_file_target_width);
        mMulti_input_file_target_width.setText("720");
        mMulti_input_file_target_height = (EditText) view.findViewById(R.id.multi_input_file_target_height);
        mMulti_input_file_target_height.setText("1280");

        mAudioMerge_file = (EditText) view.findViewById(R.id.audio_merge_source_file_name);
        View shortvideo_7_choose = view.findViewById(R.id.audio_merge_source_file_choose);
        shortvideo_7_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoose(ShortVideoProcess_6_CHOOSE);
            }
        });
        mAudioMerge_file.setText("/sdcard/transcode/music.mp3");
        mCrop_x_pos = (EditText) view.findViewById(R.id.crop_x_pos);
        mCrop_x_pos.setText("0");
        mCrop_y_pos = (EditText) view.findViewById(R.id.crop_y_pos);
        mCrop_y_pos.setText("0");
        mCrop_width = (EditText) view.findViewById(R.id.crop_width);
        mCrop_width.setText("0");
        mCrop_height = (EditText) view.findViewById(R.id.crop_height);
        mCrop_height.setText("0");

        mScale_ratio = (EditText) view.findViewById(R.id.scale_ratio);
        mScale_ratio.setText("0.0");

        mWatermark_x_pos = (EditText) view.findViewById(R.id.watermark_x_pos);
        mWatermark_x_pos.setText("20");
        mWatermark_y_pos = (EditText) view.findViewById(R.id.watermark_y_pos);
        mWatermark_y_pos.setText("20");
        mWatermark_offset = (EditText) view.findViewById(R.id.watermark_offset);
        mWatermark_offset.setText("0");
        mWatermark_out_time = (EditText) view.findViewById(R.id.watermark_out_time);
        mWatermark_out_time.setText("6000");
        mWatermark_rect = (EditText) view.findViewById(R.id.watermark_rect);
        mWatermark_rect.setText("1");

        mChartlet_frquency = (EditText) view.findViewById(R.id.dynamic_watermark_frqu);
        mChartlet_frquency.setText("15");
        mChartlet_x_pos = (EditText) view.findViewById(R.id.dynamic_watermark_x_pos);
        mChartlet_x_pos.setText("20");
        mChartlet_y_pos = (EditText) view.findViewById(R.id.dynamic_watermark_y_pos);
        mChartlet_y_pos.setText("20");
        mChartlet_offset = (EditText) view.findViewById(R.id.dynamic_watermark_offset);
        mChartlet_offset.setText("0");
        mChartlet_duration = (EditText) view.findViewById(R.id.dynamic_watermark_out_time);
        mChartlet_duration.setText("9000");
        mChartlet_rect = (EditText) view.findViewById(R.id.dynamic_watermark_rect);
        mChartlet_rect.setText("0");

        mFile_offset = (EditText) view.findViewById(R.id.file_offset);
        mFile_offset.setText("0");
        mFile_duration = (EditText) view.findViewById(R.id.file_duration);
        mFile_duration.setText("0");

        mVideo_adjust_volume = (EditText) view.findViewById(R.id.audio_adjust_para);
        mVideo_adjust_volume.setText("1");
        mAudioMerge_audio_volume = (EditText) view.findViewById(R.id.audio_merge_out_audio_volume);
        mAudioMerge_audio_volume.setText("0.6");
        mAudioMerge_fade_time = (EditText) view.findViewById(R.id.audio_merge_fade_time);
        mAudioMerge_fade_time.setText("2000");

        mShortVideoProcess_out_file = (EditText) view.findViewById(R.id.shortvideo_out_file_name);
        mShortVideoProcess_out_file.setText("/sdcard/111/out.mp4");

        View shortvideo_out_choose = view.findViewById(R.id.shortvideo_out_file_choose);
        shortvideo_out_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoose(ShortVideoProcess_OUT_CHOOSE);
            }
        });

        View shortvideo_start = view.findViewById(R.id.shortvideo_start);
        shortvideo_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startShortVideoProcess();
            }
        });
    }

    private void startShortVideoProcess(){
        TranscodingAPI.TranSource tranSource = new TranscodingAPI.TranSource();
        TranscodingAPI.TranWaterMark tranWaterMark = new TranscodingAPI.TranWaterMark();
        TranscodingAPI.TranWaterMark tranStringWaterMark = new TranscodingAPI.TranWaterMark(); // 字幕水印
        TranscodingAPI.TranDynamicWater tranDynamicWater = new TranscodingAPI.TranDynamicWater();
        TranscodingAPI.TranCrop tranCrop = new TranscodingAPI.TranCrop();
        TranscodingAPI.TranScale tranScale = new TranscodingAPI.TranScale();
        TranscodingAPI.TranTimeCut tranTimeCut = new TranscodingAPI.TranTimeCut();
        TranscodingAPI.TranMixAudio tranMixAudio = new TranscodingAPI.TranMixAudio();
        TranscodingAPI.TranFilter tranFilter = new TranscodingAPI.TranFilter();
        TranscodingAPI.TranSpeedRate tranSpeedRate = new TranscodingAPI.TranSpeedRate(); //对最后转码生成的视频进行加减速操作
        TranscodingAPI.TranOut tranOut = new TranscodingAPI.TranOut();

        try{
            ArrayList<String> list1 = new ArrayList<>();
            list1.add(mShortVideoProcess_multi_source_1.getText().toString());
            list1.add(mShortVideoProcess_multi_source_2.getText().toString());
            list1.add(mShortVideoProcess_multi_source_3.getText().toString());
            String[] stockArr1 = new String[list1.size()];
            stockArr1 = list1.toArray(stockArr1);
            tranSource.setFilePaths(stockArr1);  //转码文件数组

            // 对多个拼接视频进行单独加减速操作
//            float[] stockArr2 = new float[3];
//            stockArr2[0] = 1.0f;
//            stockArr2[1] = 2.0f;
//            stockArr2[2] = 2.0f;
//            tranSource.setChangeSpeedRates(stockArr2);

            tranSource.setVideoFadeDuration(Integer.parseInt(mMulti_input_file_fade_time.getText().toString()));
            tranSource.setAudioVolume(Float.parseFloat(mVideo_adjust_volume.getText().toString()));
            tranSource.setMergeWidth(Integer.parseInt(mMulti_input_file_target_width.getText().toString()));
            tranSource.setMergeHeight(Integer.parseInt(mMulti_input_file_target_height.getText().toString()));

            //裁剪
            tranCrop.setX(Integer.parseInt(mCrop_x_pos.getText().toString()));
            tranCrop.setY(Integer.parseInt(mCrop_y_pos.getText().toString()));
            tranCrop.setWidth(Integer.parseInt(mCrop_width.getText().toString()));
            tranCrop.setHeight(Integer.parseInt(mCrop_height.getText().toString()));

            //压缩
            tranScale.setRatio(Float.parseFloat(mScale_ratio.getText().toString()));

            //水印
            BitmapFactory.Options waterOption = new BitmapFactory.Options();
            waterOption.inScaled = false; //设置图片不缩放
            tranWaterMark.setBitmap(BitmapFactory.decodeResource(getResources(),R.raw.water_mark,waterOption));
            VideoEffect.Rect rect = VideoEffect.Rect.rightTop;
            try{
                int rectInt = Integer.parseInt(mWatermark_rect.getText().toString());
                switch (rectInt){
                    case 0:
                        rect = VideoEffect.Rect.leftTop;
                        break;
                    case 1:
                        rect = VideoEffect.Rect.rightTop;
                        break;
                    case 2:
                        rect = VideoEffect.Rect.leftBottom;
                        break;
                    case 3:
                        rect = VideoEffect.Rect.rightBottom;
                        break;
                    case 4:
                        rect = VideoEffect.Rect.center;
                        break;
                    default:
                        rect = VideoEffect.Rect.leftTop;
                        break;
                }
            }catch (Exception e){

            }
            tranWaterMark.setRect(rect);
            tranWaterMark.setX(Integer.parseInt(mWatermark_x_pos.getText().toString()));
            tranWaterMark.setY(Integer.parseInt(mWatermark_y_pos.getText().toString()));
            tranWaterMark.setStart(Integer.parseInt(mWatermark_offset.getText().toString()));
            tranWaterMark.setDuration(Integer.parseInt(mWatermark_out_time.getText().toString()));

            //文字转图片，然后进行贴图，用于实现字幕功能
            Bitmap bitmap = addTextToBitmap("这是测试字幕,测试换行",30);
            if(bitmap != null){
                tranStringWaterMark.setBitmap(bitmap);
                tranStringWaterMark.setRect(VideoEffect.Rect.center);
                tranStringWaterMark.setStart(0);
                tranStringWaterMark.setDuration(10000);
            }

            String[] waters;
            Bitmap[] bitmaps;
            try {
                waters = getResources().getAssets().list("dynamicWater");
                bitmaps = new Bitmap[waters.length];
                for(int i = 0; i< waters.length;i++){
                    waters[i] = "dynamicWater/" + waters[i];
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap tmp = BitmapFactory.decodeStream(getResources().getAssets().open(waters[i]));
                    bitmaps[i] = tmp;
                }

                tranDynamicWater.setBitmapArray(bitmaps);
                VideoEffect.Rect rectDy = VideoEffect.Rect.leftTop;
                try{
                    int rectInt = Integer.parseInt(mChartlet_rect.getText().toString());
                    switch (rectInt){
                        case 0:
                            rectDy = VideoEffect.Rect.leftTop;
                            break;
                        case 1:
                            rectDy = VideoEffect.Rect.rightTop;
                            break;
                        case 2:
                            rectDy = VideoEffect.Rect.leftBottom;
                            break;
                        case 3:
                            rectDy = VideoEffect.Rect.rightBottom;
                            break;
                        case 4:
                            rectDy = VideoEffect.Rect.center;
                            break;
                        default:
                            rectDy = VideoEffect.Rect.leftTop;
                            break;
                    }
                }catch (Exception e){

                }
                tranDynamicWater.setRect(rectDy);
                tranDynamicWater.setX(Integer.parseInt(mChartlet_x_pos.getText().toString()));
                tranDynamicWater.setY(Integer.parseInt(mChartlet_y_pos.getText().toString()));
                tranDynamicWater.setFps(Integer.parseInt(mChartlet_frquency.getText().toString()));
                tranDynamicWater.setStart(Integer.parseInt(mChartlet_offset.getText().toString()));
                tranDynamicWater.setDuration(Integer.parseInt(mChartlet_duration.getText().toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //时长裁剪
            tranTimeCut.setStart(Integer.parseInt(mFile_offset.getText().toString()));
            tranTimeCut.setDuration(Integer.parseInt(mFile_duration.getText().toString()));

            //伴音
            tranMixAudio.setFilePath(mAudioMerge_file.getText().toString());
            tranMixAudio.setMixVolume(Float.parseFloat(mAudioMerge_audio_volume.getText().toString()));
            tranMixAudio.setFadeDuration(Integer.parseInt(mAudioMerge_fade_time.getText().toString()));

            //滤镜
            if(getArguments() != null && getArguments().getBoolean(ARG_ADJUST)){
                tranFilter.setBrightness(getArguments().getFloat(ARG_BRIGHTNESS));
                tranFilter.setContrast(getArguments().getFloat(ARG_CONTRAST));
                tranFilter.setSaturation(getArguments().getFloat(ARG_SATURATION));
                tranFilter.setSharpenness(getArguments().getFloat(ARG_SHARPEN));
                tranFilter.setHue(getArguments().getFloat(ARG_HUE));
            }

//            tranSpeedRate.setSpeedRate(0.5f); //对最后转码生成的视频进行加减速操作

            tranOut.setFilePath(mShortVideoProcess_out_file.getText().toString());

        }catch (Exception e){
            showToast("请填写正确的参数");
            return;
        }

        boolean init = TranscodingAPI.getInstance().init(ShortVideoProcessFragment.this.getActivity().getApplicationContext(),TestAppkey.APP_KEY);
        if(!init){
            showToast("鉴权失败，请仔细检查appkey 或保持手机联网");
            return;
        }

        //*********  转码参数，demo为演示SDK功能，因此设置了全部的值，实际使用中可根据需求选择需要的参数  *******/
        TranscodingAPI.TranscodePara transcodePara = new TranscodingAPI.TranscodePara();
        transcodePara.setSource(tranSource);  //必须
        transcodePara.setOut(tranOut); //必须

        //以下参数为非必须参数，用户可根据需要决定设置哪些参数
        transcodePara.setWaterMarks(new TranscodingAPI.TranWaterMark[]{tranWaterMark,tranStringWaterMark}); //水印，需要时添加，否则不用设置
        transcodePara.setDynamicWater(tranDynamicWater); //动态水印，需要时添加，否则不用设置
        transcodePara.setCrop(tranCrop);//视频宽高裁剪，需要时添加，否则不用设置
        transcodePara.setChangeSpeed(tranSpeedRate);//音视频加减速播放，需要时添加，否则不用设置
        transcodePara.setScale(tranScale);//视频等比例缩放，需要时添加，否则不用设置
        transcodePara.setTimeCut(tranTimeCut);//媒体文件时长剪辑，需要时添加，否则不用设置
        transcodePara.setMixAudio(tranMixAudio);//混音，需要时添加，否则不用设置
        transcodePara.setFilter(tranFilter);//转码滤镜，需要时添加，否则不用设置

        mShortVideoProcessTask = new AsyncTask<TranscodingAPI.TranscodePara,Integer,Integer>(){

            ProgressDialog dialog;
            @Override
            protected Integer doInBackground(TranscodingAPI.TranscodePara... params) {
                TranscodingAPI.TranscodePara transcodePara = params[0];
                transcodePara.getOut().setCallBack(new TranscodingNative.NativeCallBack(){
                    @Override
                    public void progress(int progress, int total) {
                        dialog.setMax(total);
                        publishProgress(progress);
                    }
                });

                return TranscodingAPI.getInstance().VODProcess(transcodePara);
            }

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(ShortVideoProcessFragment.this.getActivity());
                dialog.setMessage("开始短视频处理");
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        TranscodingAPI.getInstance().stopVODProcess();
                    }
                });
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.show();
            }

            @Override
            protected void onCancelled() {
                if(dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                    dialog = null;
                }
            }

            @Override
            protected void onProgressUpdate(Integer[] values) {
                dialog.setMessage("短视频处理中，请稍后...");
                dialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Integer ret) {
                dialog.dismiss();
                dialog = null;
                switch (ret){
                    case TRAN_PARA_NULL:
                        showToast("短视频处理失败，输入文件为空");
                        break;
                    case TRAN_OUT_FILE_CREATE_ERROR:
                        showToast("短视频处理失败，无法创建目标文件，请检查目标文件地址或SD卡权限");
                        break;
                    case TRAN_PRE_IS_NOT_FINISH:
                        showToast("短视频处理失败，上一次未处理完毕");
                        break;
                    case TRAN_SOURCE_FILE_PARSE_ERROR:
                        showToast("短视频处理失败，原始文件解析失败");
                        break;
                    case TRAN_SOURCE_NO_VIDEO_OR_AUDIO:
                        showToast("短视频处理失败，原始文件没有视频或音频");
                        break;
                    case TRAN_MIX_FILE_PARSE_ERROR:
                        showToast("短视频处理失败，混音文件解析失败");
                        break;
                    case TRAN_PROCESS_ERROR:
                        showToast("短视频处理失败，媒体文件不支持，或参数设置错误");
                        break;
                    default:
                        showToast("转码已完成");
                        break;
                }
            }
        }.execute(transcodePara);
    }

    private Bitmap textAsBitmap(String text, float textSize,int width) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(text, textPaint, width,
                Layout.Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 10,
                layout.getHeight() + 10, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.TRANSPARENT);
        layout.draw(canvas);
        return bitmap;
    }

    //在图片上加文字
    private Bitmap addTextToBitmap(String text, float textSize) {

        BitmapFactory.Options waterOption = new BitmapFactory.Options();
        waterOption.inScaled = false; //设置图片不缩放
        Bitmap source = BitmapFactory.decodeResource(getResources(),R.drawable.ic_bubble_edit,waterOption);
        int width = source.getWidth(), height = source.getHeight();
        Bitmap icon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //建立一个空的BItMap

        Canvas canvas = new Canvas(icon);//初始化画布 绘制的图像到icon上
        Paint photoPaint = new Paint(); //建立画笔
        photoPaint.setDither(true); //获取跟清晰的图像采样
        photoPaint.setFilterBitmap(true);//过滤一些

        Rect src = new Rect(0, 0, width, height);//创建一个指定的新矩形的坐标
        Rect dst = new Rect(0, 0, width, height);//创建一个指定的新矩形的坐标
        canvas.drawBitmap(source, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

        Bitmap textBitmap = textAsBitmap(text,textSize,width - 20);
        width = textBitmap.getWidth();
        height = textBitmap.getHeight();
        src = new Rect(0, 0, width, height);//创建一个指定的新矩形的坐标
        dst = new Rect(0, 0, width, height);//创建一个指定的新矩形的坐标
        canvas.drawBitmap(textBitmap, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

//        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔
//        textPaint.setTextSize(textSize);//字体大小
//        textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
//        textPaint.setColor(Color.RED);//采用的颜色
//        canvas.drawText(text, 0,50, textPaint);//绘制上去 字，开始未知x,y采用那只笔绘制

        canvas.save();
        canvas.restore();
        return icon;
    }


}
