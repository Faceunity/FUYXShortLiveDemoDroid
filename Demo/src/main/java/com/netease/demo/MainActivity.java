package com.netease.demo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.netease.transcoding.demo.R;
import com.netease.transcoding.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	private OnClickListener OnClickEvent;
	private Button mBtnStartRecordBtn;
	private Button mBtnStartCompileBtn;
	private Button mBtnStartPlayerBtn;
	private MsgReceiver msgReceiver;

	/**   6.0权限处理     **/
	private boolean bPermission = false;
	private final int WRITE_PERMISSION_REQ_CODE = 100;
	private boolean checkPublishPermission() {
		if (Build.VERSION.SDK_INT >= 23) {
			List<String> permissions = new ArrayList<>();
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			}
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)) {
				permissions.add(Manifest.permission.CAMERA);
			}
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)) {
				permissions.add(Manifest.permission.RECORD_AUDIO);
			}
			if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)) {
				permissions.add(Manifest.permission.READ_PHONE_STATE);
			}
			if (permissions.size() != 0) {
				ActivityCompat.requestPermissions(MainActivity.this,
						(String[]) permissions.toArray(new String[0]),
						WRITE_PERMISSION_REQ_CODE);
				return false;
			}
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case WRITE_PERMISSION_REQ_CODE:
				for (int ret : grantResults) {
					if (ret != PackageManager.PERMISSION_GRANTED) {
						return;
					}
				}
				bPermission = true;
				break;
			default:
				break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LogUtil.debug = true; //TODO: 删除测试
		/**   6.0权限申请     **/
		bPermission = checkPublishPermission();

		setContentView(R.layout.activity_main);

		copyTestFile();

		//动态注册广播接收器，接收Service的消息
		msgReceiver = new MsgReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("LiveStreamingStopFinished");
		registerReceiver(msgReceiver, intentFilter);

		mBtnStartRecordBtn = (Button) findViewById(R.id.StartRecordBtn);
		mBtnStartRecordBtn.setEnabled(true);

		mBtnStartCompileBtn = (Button) findViewById(R.id.StartCompileBtn);
		mBtnStartCompileBtn.setEnabled(true);

		mBtnStartPlayerBtn = (Button) findViewById(R.id.StartPlayerBtn);
		mBtnStartPlayerBtn.setEnabled(true);

		View imageBtn = findViewById(R.id.StartImageBtn);

		View reverseBtn = findViewById(R.id.StartReverseBtn);


		OnClickEvent = new OnClickListener() {

			public void onClick(View v) {
				Intent intent;
				switch (v.getId()) {
					case R.id.StartRecordBtn:
						intent = new Intent(MainActivity.this, LiveStreamingActivity.class);
						if(!bPermission){
							Toast.makeText(getApplication(),"请先允许app所需要的权限",Toast.LENGTH_LONG).show();
							bPermission = checkPublishPermission();
							return;
						}

						startActivity(intent);
						break;
					case R.id.StartCompileBtn:
						intent = new Intent(MainActivity.this, com.netease.demo.DemoActivity.class);

						if(!bPermission){
							Toast.makeText(getApplication(),"请先允许app所需要的权限",Toast.LENGTH_LONG).show();
							bPermission = checkPublishPermission();
							return;
						}

						//默认参数，关闭视频色彩调节
						intent.putExtra("brightness", 1.0f);
						intent.putExtra("contrast", 1.0f);
						intent.putExtra("saturation", 1.0f);
						intent.putExtra("sharpen", 1.0f);
						intent.putExtra("vignette", 1.0f);

						startActivity(intent);
						break;
					case R.id.StartPlayerBtn:
						intent = new Intent(MainActivity.this, com.netease.demo.PlayerActivity.class);

						if(!bPermission){
							Toast.makeText(getApplication(),"请先允许app所需要的权限",Toast.LENGTH_LONG).show();
							bPermission = checkPublishPermission();
							return;
						}

						startActivity(intent);
						break;

					case R.id.StartImageBtn:
						intent = new Intent(MainActivity.this, com.netease.demo.ImageEditActivity.class);
						startActivity(intent);
						break;

					case R.id.StartReverseBtn:
						intent = new Intent(MainActivity.this, com.netease.demo.VideoReverseActivity.class);
						startActivity(intent);
						break;
					default:
						break;
				}
			}
		};
		mBtnStartRecordBtn.setOnClickListener(OnClickEvent);
		mBtnStartCompileBtn.setOnClickListener(OnClickEvent);
		mBtnStartPlayerBtn.setOnClickListener(OnClickEvent);
		reverseBtn.setOnClickListener(OnClickEvent);
		imageBtn.setOnClickListener(OnClickEvent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data ==null || data.getExtras() == null || TextUtils.isEmpty(data.getExtras().getString("result"))) {
			return;
		}
		String result = data.getExtras().getString("result");
	}
	
	//用于接收Service发送的消息
    public class MsgReceiver extends BroadcastReceiver{  
  
        @Override  
        public void onReceive(Context context, Intent intent) {  
 
            int value = intent.getIntExtra("LiveStreamingStopFinished", 0);   
            if(value == 1)//finished
            {
            	mBtnStartRecordBtn.setEnabled(true);
				mBtnStartRecordBtn.setText("进 入 文 件 录 制");
            }
            else//not yet finished
            {
				mBtnStartRecordBtn.setEnabled(false);
				mBtnStartRecordBtn.setText("录制停止中...");
            }
        }          
    } 
    
    @Override
	protected void onDestroy() {
        unregisterReceiver(msgReceiver);
		msgReceiver = null;
        super.onDestroy();
    }

	private void copyTestFile(){
		String[] testFiles;
		try {
			testFiles = getResources().getAssets().list("tranTestFile");
			String dir = Environment.getExternalStorageDirectory() + "/transcode/";
			File fileDir = new File(dir);
			if(!fileDir.exists()){
				fileDir.mkdir();
			}
			for(String name : testFiles){
				String outPath = fileDir.getAbsolutePath() + "/" + name;
				File file = new File(outPath);
				if(!file.exists()){
					InputStream in = getResources().getAssets().open("tranTestFile/" + name);
					FileOutputStream out = new FileOutputStream(file);
					copyFile(in, out);
					in.close();
					out.flush();
					out.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
}

