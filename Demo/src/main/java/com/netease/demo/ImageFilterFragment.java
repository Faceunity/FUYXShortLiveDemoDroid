package com.netease.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.netease.transcoding.demo.R;
import com.netease.transcoding.image.ImageEditer;
import com.netease.vcloud.video.effect.VideoEffect;

/**
 * 图片滤镜
 */
public class ImageFilterFragment extends BaseFragment implements View.OnClickListener {


    private ImageView mDstImage;
    private ImageView mSrcImage;
    private Bitmap mSrcBitmap;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_image_filter, container, false);
        initUI(parent);
        return parent;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initUI(View root){
        mSrcBitmap = BitmapFactory.decodeResource(getResources(),R.raw.koala);
        mSrcImage = (ImageView) root.findViewById(R.id.image_filter_src);
        mSrcImage.setImageBitmap(mSrcBitmap);
        mDstImage = (ImageView) root.findViewById(R.id.image_filter_dst);

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

        initFilter();

        View getFilterView = root.findViewById(R.id.image_filter_get);
        getFilterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFilter();
            }
        });

    }

    private void initFilter(){
        ImageEditer.ImageFilterParam param = new ImageEditer.ImageFilterParam();
        param.filterType = VideoEffect.FilterType.tender;
        param.beautyLevel = 5;
        param.filterStrength = 0.5f;
        ImageEditer.getInstance().initFilter(mSrcBitmap,null,null);
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

        mSrcImage.setImageBitmap(ImageEditer.getInstance().getFilterBitmap());
    }

    private void getFilter(){
        Bitmap bitmap = ImageEditer.getInstance().getFilterBitmap();
        if(bitmap != null){
            mDstImage.setImageBitmap(bitmap);
        }else {
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
        }
    }
}
