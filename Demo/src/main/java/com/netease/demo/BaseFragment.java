package com.netease.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by hzzhujinbo on 2017/1/11.
 */

public class BaseFragment extends Fragment {

    private Toast mToast;
    void showToast(final String text){
        Activity activity = this.getActivity();
        if(activity == null){
            return;
        }
        if(mToast == null){
            mToast = Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        if(Thread.currentThread() != Looper.getMainLooper().getThread()){
            activity.runOnUiThread(new Runnable() {
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
