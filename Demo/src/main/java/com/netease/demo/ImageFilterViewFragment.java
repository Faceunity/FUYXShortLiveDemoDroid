package com.netease.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.netease.transcoding.demo.R;
import com.netease.transcoding.image.ImageEditer;
import com.netease.transcoding.util.LogUtil;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.render.NeteaseView;

/**
 * 图片滤镜
 */
public class ImageFilterViewFragment extends BaseFragment implements View.OnClickListener {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.instance().w("ImageFilterViewFragment","onCreateView");
        View parent = inflater.inflate(R.layout.fragment_image_filter_view, container, false);
        initUI(parent);
        return parent;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        LogUtil.instance().w("ImageFilterViewFragment","onDestroyView");
        ImageEditer.getInstance().unInitFilter();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        LogUtil.instance().w("ImageFilterViewFragment","onDetach");
        super.onDetach();
    }

    private void initUI(View root){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.raw.koala);
        NeteaseView filterView = (NeteaseView) root.findViewById(R.id.image_filter_view);

        View brooklyn = root.findViewById(R.id.image_filter_brooklyn);
        brooklyn.setOnClickListener(this);

        View calm = root.findViewById(R.id.image_filter_clean);
        calm.setOnClickListener(this);

        View nature = root.findViewById(R.id.image_filter_nature);
        nature.setOnClickListener(this);

        View healthy = root.findViewById(R.id.image_filter_healthy);
        healthy.setOnClickListener(this);

        View pixar = root.findViewById(R.id.image_filter_pixar);
        pixar.setOnClickListener(this);

        View tender = root.findViewById(R.id.image_filter_tender);
        tender.setOnClickListener(this);

        View whiten = root.findViewById(R.id.image_filter_whiten);
        whiten.setOnClickListener(this);
        initFilter(bitmap,filterView);

        final ImageView showView = (ImageView) root.findViewById(R.id.image_filter_dst);
        View getFilterView = root.findViewById(R.id.image_filter_get);
        getFilterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFilter(showView);
            }
        });

        SeekBar filterSeekBar = ((SeekBar) root.findViewById(R.id.image_filter_seekbar));
        filterSeekBar.setVisibility(View.VISIBLE);
        filterSeekBar.setProgress(50);
        filterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float param = (float) progress / 100;
                ImageEditer.getInstance().setFilterStrength(param);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar beautySeekBar = ((SeekBar) root.findViewById(R.id.image_beauty_seekbar));
        beautySeekBar.setVisibility(View.VISIBLE);
        beautySeekBar.setProgress(100);
        beautySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int param = progress / 20;
                ImageEditer.getInstance().setBeautyLevel(param);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initFilter(Bitmap bitmap,NeteaseView filterView){
        ImageEditer.ImageFilterParam param = new ImageEditer.ImageFilterParam();
        param.filterType = VideoEffect.FilterType.tender;
        param.beautyLevel = 5;
        param.filterStrength = 0.5f;
        ImageEditer.getInstance().initFilter(bitmap,param,filterView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_filter_brooklyn:
                ImageEditer.getInstance().setFilterType(VideoEffect.FilterType.brooklyn);
                break;
            case R.id.image_filter_clean:
                ImageEditer.getInstance().setFilterType(VideoEffect.FilterType.clean);
                break;
            case R.id.image_filter_nature:
                ImageEditer.getInstance().setFilterType(VideoEffect.FilterType.nature);
                break;
            case R.id.image_filter_healthy:
                ImageEditer.getInstance().setFilterType(VideoEffect.FilterType.healthy);
                break;
            case R.id.image_filter_pixar:
                ImageEditer.getInstance().setFilterType(VideoEffect.FilterType.pixar);
                break;
            case R.id.image_filter_tender:
                ImageEditer.getInstance().setFilterType(VideoEffect.FilterType.tender);
                break;
            case R.id.image_filter_whiten:
                ImageEditer.getInstance().setFilterType(VideoEffect.FilterType.whiten);
                break;
            default:
                break;

        }
    }

    private void getFilter(ImageView imageView){
        Bitmap bitmap = ImageEditer.getInstance().getFilterBitmap();
        if(bitmap == null){
            int errorCode = ImageEditer.getInstance().getLastErrorCode();
            switch (errorCode){
                case ImageEditer.VERIFY_FAILED:
                    showToast("appkey 验证失败");
                    break;

                case ImageEditer.UNINITIALIZED:
                    showToast("未初始化");
                    break;

                case ImageEditer.ILLEGAL_PARAMETERS:
                    showToast("参数非法");
                    break;

                default:
                    break;
            }
        }else {
            imageView.setImageBitmap(bitmap);
        }
    }
}
