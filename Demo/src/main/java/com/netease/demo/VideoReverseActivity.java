package com.netease.demo;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.netease.transcoding.TranscodingAPI;
import com.netease.transcoding.TranscodingNative;
import com.netease.transcoding.demo.R;

import static com.netease.transcoding.TranscodingAPI.TRAN_OUT_FILE_CREATE_ERROR;
import static com.netease.transcoding.TranscodingAPI.TRAN_PARA_NULL;
import static com.netease.transcoding.TranscodingAPI.TRAN_PRE_IS_NOT_FINISH;
import static com.netease.transcoding.TranscodingAPI.TRAN_SOURCE_FILE_PARSE_ERROR;
import static com.netease.transcoding.TranscodingAPI.VERIFY_FAILED;

public class VideoReverseActivity extends Activity {

    private static final String TAG = "VideoReverseActivity";

    private final int REVERSE_CHOOSE = 106;
    private EditText mIn_file;
    private EditText mOut_file;
    private AsyncTask mReverseTask;

    public VideoReverseActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_videoreverse);
        initUI();
    }


    @Override
    public void onDestroy() {
        if(mReverseTask != null && mReverseTask.getStatus() != AsyncTask.Status.FINISHED){
            mReverseTask.cancel(true);
            mReverseTask = null;
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data.getData() == null) {
            return;
        }
        String path = FileUtil.getPath(VideoReverseActivity.this, data.getData());
        switch (requestCode){
            case REVERSE_CHOOSE:
                mIn_file.setText(path);
                break;
            default:
                break;
        }
    }
    
    private void initUI(){

        /***********  视频截图   ********/

        mIn_file = (EditText)findViewById(R.id.videoreverse_in);
        View snapshot_choose = findViewById(R.id.videoreverse_in_choose);
        snapshot_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChoose(REVERSE_CHOOSE);
            }
        });
        mIn_file.setText("/sdcard/transcode/media1.mp4");

        mOut_file = (EditText) findViewById(R.id.videoreverse_out);
        mOut_file.setText("/sdcard/111/reverse.mp4");

        View com_start = findViewById(R.id.videoreverse_start);
        com_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

    }

    private void start(){
        final TranscodingAPI.VideoReversePara para = new TranscodingAPI.VideoReversePara();
        try{
            String fileInPath = mIn_file.getText().toString();
            String fileOutPath = mOut_file.getText().toString();
            para.setFileInPath(fileInPath); //原始文件
            para.setFileOutPath(fileOutPath);

        }catch (Exception e){
            showToast("请填写正确的参数");
            return;
        }

        boolean init =  TranscodingAPI.getInstance().init(VideoReverseActivity.this.getApplicationContext(),TestAppkey.APP_KEY);
        if(!init){
            showToast("鉴权失败，请仔细检查appkey 或保持手机联网");
            return;
        }
        mReverseTask = new AsyncTask<TranscodingAPI.VideoReversePara,Integer,Integer>(){

            ProgressDialog dialog;
            @Override
            protected Integer doInBackground(TranscodingAPI.VideoReversePara... params) {
                final TranscodingAPI.VideoReversePara reversePara = params[0];
                reversePara.setCallBack(new TranscodingNative.NativeCallBack() {
                    @Override
                    public void progress(int progress, int total) {
                        dialog.setMax(total);
                        publishProgress(progress);
                    }
                });
                return TranscodingAPI.getInstance().videoReverse(reversePara);
            }

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(VideoReverseActivity.this);
                dialog.setMessage("开始反转");
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
                dialog.setMessage("反转中，请稍后...");
                dialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Integer ret) {
                dialog.dismiss();
                dialog = null;
                switch (ret){
                    case VERIFY_FAILED:
                        showToast("反转失败，APPkey 未授权");
                        break;
                    case TRAN_PARA_NULL:
                        showToast("反转失败，参数为空");
                        break;
                    case TRAN_PRE_IS_NOT_FINISH:
                        showToast("反转失败，上一次还未结束");
                        break;
                    case TRAN_SOURCE_FILE_PARSE_ERROR:
                        showToast("反转失败，原始文件不支持");
                        break;
                    case TRAN_OUT_FILE_CREATE_ERROR:
                        showToast("反转失败，目标文件无法生成");
                        break;
                    default:
                        showToast("反转已结束");
                        break;
                }
            }
        }.execute(para);
    }




    private Toast mToast;
    void showToast(final String text){
        if(mToast == null){
            mToast = Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mToast.setText(text);
                    mToast.show();
                }
            });
        }else {
            mToast.setText(text);
            mToast.show();
        }
    }

    void openFileChoose(int requestCode){

        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT){
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }
}
