package com.netease.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.netease.transcoding.demo.R;
import com.netease.transcoding.image.ImageEditer;
import com.netease.transcoding.image.mosaic.MosaicView;

/**
 * 图片马赛克
 */
public class ImageMosaicFragment extends BaseFragment {


    private ImageView mDstImage;
    private final int mMosaicSize = 30;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_image_mosaic, container, false);
        initUI(parent);
        return parent;
    }

    @Override
    public void onDestroyView() {
        ImageEditer.getInstance().unInitMosaic();
        super.onDestroyView();
    }

    private void initUI(View root){
        Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(),R.raw.koala);
        final Bitmap customMosaic = BitmapFactory.decodeResource(getResources(),R.raw.flower);
        MosaicView mosaicView = (MosaicView) root.findViewById(R.id.image_mosaic_view);
        int initCode = ImageEditer.getInstance().initMosaic(mosaicView,srcBitmap,mMosaicSize);
        if(initCode != ImageEditer.SUCCESS){
            showErrorMsg(initCode);
        }

        final RadioGroup modeGroup = (RadioGroup) root.findViewById(R.id.image_mosaic_mode);
        modeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ImageEditer.getInstance().setMosaicMode(R.id.image_mosaic_mode_on == checkedId);
            }
        });

        RadioGroup typeGroup = (RadioGroup) root.findViewById(R.id.image_mosaic_type);
        typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.image_mosaic_type_base:
                        ImageEditer.getInstance().setMosaicType(ImageEditer.MosaicType.BASE,null);
                        break;
                    case R.id.image_mosaic_type_glass:
                        ImageEditer.getInstance().setMosaicType(ImageEditer.MosaicType.GLASS,null);
                        break;
                    case R.id.image_mosaic_type_red:
                        ImageEditer.getInstance().setMosaicType(ImageEditer.MosaicType.CUSTOM,customMosaic);
                        break;
                    default:
                        break;
                }
                modeGroup.check(R.id.image_mosaic_mode_on);
            }
        });



        mDstImage = (ImageView) root.findViewById(R.id.image_mosaic_dst);
        View start = root.findViewById(R.id.image_mosaic_get);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap dstBitmap = ImageEditer.getInstance().getMosaicBitmap();
                if(dstBitmap != null){
                    mDstImage.setImageBitmap(dstBitmap);
                }
            }
        });
    }

    private void showErrorMsg(int code){
        switch (code){
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
