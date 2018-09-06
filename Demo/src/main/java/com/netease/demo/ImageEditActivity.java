package com.netease.demo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.netease.transcoding.demo.R;
import com.netease.transcoding.image.ImageEditer;

public class ImageEditActivity extends AppCompatActivity {

    private boolean mInited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        mInited = ImageEditer.getInstance().init(getApplicationContext(),TestAppkey.APP_KEY);
        if(!mInited){
            Toast.makeText(this,"初始化失败",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if(mInited){
            ImageEditer.getInstance().unInit();
        }
        super.onDestroy();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment = null;
            switch (position){
                case 0:
                    fragment = new ImageCropFragment();
                    break;
                case 1:
                    fragment = new ImageWaterFragment();
                    break;
                case 2:
                    fragment = new ImageFilterFragment();
                    break;
                case 3:
                    fragment = new ImageMosaicFragment();
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "裁剪";
                case 1:
                    return "水印";
                case 2:
                    return "滤镜";
                case 3:
                    return "马赛克";
            }
            return null;
        }
    }

}
