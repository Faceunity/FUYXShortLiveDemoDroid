package com.netease.demo;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.netease.transcoding.MediaMetadata;
import com.netease.transcoding.TranscodingAPI;
import com.netease.transcoding.demo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static com.netease.transcoding.TranscodingAPI.SNAPSHOT_FILE_NOT_EXIST;
import static com.netease.transcoding.TranscodingAPI.SNAPSHOT_FILE_NOT_SUPPORT;
import static com.netease.transcoding.TranscodingAPI.SNAPSHOT_PARA_NULL;
import static com.netease.transcoding.TranscodingAPI.VERIFY_FAILED;

public class SnapshotFragment extends BaseFragment {

    private static final String TAG = "SnapshotFragment";

    private final int SNAPSHOT_CHOOSE = 105;
    private EditText mSnapshot_source_file;
    private EditText mSnapshot_offset;
    private EditText mSnapshot_interval;
    private EditText mSnapshot_pic_width;
    private EditText mSnapshot_pic_height;
    private EditText mSnapshot_pic_duration;
    private AsyncTask mSnapshotTask;

    public SnapshotFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_snapshot, container, false);
        initUI(parent);
        return parent;
    }

    @Override
    public void onDetach() {
        if(mSnapshotTask != null && mSnapshotTask.getStatus() != AsyncTask.Status.FINISHED){
            mSnapshotTask.cancel(true);
            mSnapshotTask = null;
        }
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data.getData() == null) {
            return;
        }
        String path = FileUtil.getPath(this.getContext(), data.getData());
        switch (requestCode){
            case SNAPSHOT_CHOOSE:
                mSnapshot_source_file.setText(path);
                break;
            default:
                break;
        }
    }
    
    private void initUI(View view){

        /***********  视频截图   ********/

        mSnapshot_source_file = (EditText) view.findViewById(R.id.snapshot_source);
        View snapshot_choose = view.findViewById(R.id.snapshot_source_choose);
        snapshot_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoose(SNAPSHOT_CHOOSE);
            }
        });
        mSnapshot_source_file.setText("/sdcard/transcode/media1.mp4");

        mSnapshot_offset = (EditText) view.findViewById(R.id.snapshot_offset);
        mSnapshot_offset.setText("1000");

        mSnapshot_interval = (EditText) view.findViewById(R.id.snapshot_interval);
        mSnapshot_interval.setText("2000");

        mSnapshot_pic_width = (EditText) view.findViewById(R.id.snapshot_pic_width);
        mSnapshot_pic_width.setText("0");

        mSnapshot_pic_height = (EditText) view.findViewById(R.id.snapshot_pic_height);
        mSnapshot_pic_height.setText("0");

        mSnapshot_pic_duration = (EditText) view.findViewById(R.id.snapshot_pic_duration);
        mSnapshot_pic_duration.setText("0");

        View com_start = view.findViewById(R.id.snapshot_start);
        com_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSnapshot();
            }
        });

    }

    private void startSnapshot(){
        final TranscodingAPI.SnapshotPara para = new TranscodingAPI.SnapshotPara();
        try{
            String filePath = mSnapshot_source_file.getText().toString();
            para.setFilePath(filePath); //原始文件
            para.setStart(Integer.parseInt(mSnapshot_offset.getText().toString())); //截图开始时间，单位ms
            para.setInterval(Integer.parseInt(mSnapshot_interval.getText().toString()));//截图时间间隔，单位ms 若不设置，则按 1s一张截图
            para.setDuration(Integer.parseInt(mSnapshot_pic_duration.getText().toString()));//截图持续时间，单位ms，0表示一直截图到文件结束
            para.setOutWidth(Integer.parseInt(mSnapshot_pic_width.getText().toString()));//截图输出宽 0，表示原始视频宽
            para.setOutHeight(Integer.parseInt(mSnapshot_pic_height.getText().toString()));//截图输出高 0，表示原始视频高

            para.setSdk_record(true); //TODO: 这里需要注意，如果是SDK录制的文件设置此参数支持快速截图，如果非SDK录制文件设置为true，可能导致截图的时间不准确

            //Demo为了测试，所以参数要求外部输入，用户在使用时可以先用获取文件信息后再决定设置哪些参数
            MediaMetadata.MetaData metaData = TranscodingAPI.getInstance().getMediaInfo(filePath);
            if(metaData != null){
                Log.d(TAG,metaData.toString());
            }

        }catch (Exception e){
            showToast("请填写正确的参数");
            return;
        }

        boolean init =  TranscodingAPI.getInstance().init(SnapshotFragment.this.getActivity().getApplicationContext(),TestAppkey.APP_KEY);
        if(!init){
            showToast("鉴权失败，请仔细检查appkey 或保持手机联网");
            return;
        }
        mSnapshotTask = new AsyncTask<TranscodingAPI.SnapshotPara,Integer,Integer>(){

            ProgressDialog dialog;
            @Override
            protected Integer doInBackground(TranscodingAPI.SnapshotPara... params) {
                final TranscodingAPI.SnapshotPara snapshotPara = params[0];
                snapshotPara.setCallBack(new TranscodingAPI.SnapshotPara.SnapshotCallback() {
                    @Override
                    public void result(Bitmap bitmap, int current, int total) {
                        Log.d(TAG,"current = " + current + " total = " + total + " bitmap = " + bitmap);
                        testSave(bitmap,current);

                        dialog.setMax(total);
                        publishProgress(current);
                    }
                });
                return TranscodingAPI.getInstance().snapShot(snapshotPara);
            }

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(SnapshotFragment.this.getActivity());
                dialog.setMessage("开始截图");
                dialog.setCancelable(false);
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
                dialog.setMessage("截图中，请稍后...");
                dialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Integer ret) {
                dialog.dismiss();
                dialog = null;
                switch (ret){
                    case VERIFY_FAILED:
                        showToast("截图失败，APPkey 未授权");
                        break;
                    case SNAPSHOT_PARA_NULL:
                        showToast("截图失败，截图参数为空");
                        break;
                    case SNAPSHOT_FILE_NOT_EXIST:
                        showToast("截图失败，原始文件不存在");
                        break;
                    case SNAPSHOT_FILE_NOT_SUPPORT:
                        showToast("截图失败，原始文件不支持");
                        break;
                    default:
                        showToast("截图已结束");
                        break;
                }
            }
        }.execute(para);
    }

    private void testSave(Bitmap bitmap,int i){
        File file = new File(Environment.getExternalStorageDirectory() + "/111/");
        if(!file.exists()){
            file.mkdir();
        }
        String path =  file.getAbsolutePath() + "/" + i + ".png";
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
