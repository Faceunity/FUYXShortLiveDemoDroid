package com.netease.demo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.netease.transcoding.demo.R;


public class WelcomeActivity extends Activity {

	private ImageView mWelcomeImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_welcome);
		
		mWelcomeImage = (ImageView) findViewById(R.id.welcome_image);
		mWelcomeImage.setImageResource(R.drawable.welcome);
		
		// 延迟2秒，然后跳转到登录页面
        new Handler().postDelayed(r, 2000);
	}
	
	 Runnable r = new Runnable() {
	        @Override
	        public void run() {
	            Intent intent = new Intent();

				intent.setClass(WelcomeActivity.this, com.netease.demo.MainActivity.class);

	            startActivity(intent);
	            finish();
	        }
	    };
}

