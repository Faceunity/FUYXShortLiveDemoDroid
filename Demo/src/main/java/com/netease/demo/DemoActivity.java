package com.netease.demo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.netease.transcoding.demo.R;


public class DemoActivity extends AppCompatActivity {

    private boolean mSetAdjust = false;
    private float mSharpenAdjustLevel;
    private float mContrastAdjustLevel;
    private float mBrightnessAdjustLevel;
    private float mSaturationAdjustLevel;
    private float mHueAdjustLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        mBrightnessAdjustLevel = getIntent().getFloatExtra(ShortVideoProcessFragment.ARG_BRIGHTNESS, mBrightnessAdjustLevel);
        mContrastAdjustLevel = getIntent().getFloatExtra(ShortVideoProcessFragment.ARG_CONTRAST, mContrastAdjustLevel);
        mSaturationAdjustLevel = getIntent().getFloatExtra(ShortVideoProcessFragment.ARG_SATURATION, mSaturationAdjustLevel);
        mSharpenAdjustLevel = getIntent().getFloatExtra(ShortVideoProcessFragment.ARG_SHARPEN, mSharpenAdjustLevel);
        mHueAdjustLevel = getIntent().getFloatExtra(ShortVideoProcessFragment.ARG_HUE, mHueAdjustLevel);
        mSetAdjust = getIntent().getBooleanExtra("setAdjust",false);
        if(mSetAdjust){
            viewPager.setCurrentItem(1);
        }
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
                    fragment = new SnapshotFragment();
                    break;
                case 1:
                    if(mSetAdjust){
                        fragment = ShortVideoProcessFragment.newInstance(mBrightnessAdjustLevel, mContrastAdjustLevel, mSaturationAdjustLevel, mSharpenAdjustLevel, mHueAdjustLevel,true);
                    }else {
                        fragment = new ShortVideoProcessFragment();
                    }

                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "截图";
                case 1:
                    return "短视频处理";
            }
            return null;
        }
    }
}
