package com.netease.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.transcoding.demo.R;
import com.netease.transcoding.image.ImageEditer;
import com.netease.vcloud.video.effect.VideoEffect;

/**
 * 图片水印
 */
public class ImageWaterFragment extends BaseFragment {


    private EditText mWaterX;
    private EditText mWaterY;
    private EditText mWaterRect;
    private ImageView mDstImage;
    private Bitmap mSrcBitmap;
    private Bitmap mWaterBitmap;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_image_water, container, false);
        initUI(parent);
        return parent;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initUI(View root){
        mSrcBitmap = BitmapFactory.decodeResource(getResources(),R.raw.koala);
        ImageView src = (ImageView) root.findViewById(R.id.image_water_src);
        src.setImageBitmap(mSrcBitmap);
        TextView textView = (TextView) root.findViewById(R.id.image_water_size);
        textView.setText(mSrcBitmap.getWidth() + "x" +mSrcBitmap.getHeight());

        mWaterBitmap = BitmapFactory.decodeResource(getResources(),R.raw.water_mark);

        mWaterX = (EditText) root.findViewById(R.id.image_water_x);
        mWaterY = (EditText) root.findViewById(R.id.image_water_y);
        mWaterRect = (EditText) root.findViewById(R.id.image_water_rect);
        mDstImage = (ImageView) root.findViewById(R.id.image_water_dst);

        View start = root.findViewById(R.id.simage_water_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(mWaterX.getText()) || TextUtils.isEmpty(mWaterY.getText())
                        ||TextUtils.isEmpty(mWaterRect.getText())){
                    showToast("请填写正确的参数");
                    return;
                }

                int x = Integer.parseInt(mWaterX.getText().toString());
                int y = Integer.parseInt(mWaterY.getText().toString());
                VideoEffect.Rect rect = VideoEffect.Rect.leftTop;
                try{
                    int rectInt = Integer.parseInt(mWaterRect.getText().toString());
                    switch (rectInt){
                        case 0:
                            rect = VideoEffect.Rect.leftTop;
                            break;
                        case 1:
                            rect = VideoEffect.Rect.rightTop;
                            break;
                        case 2:
                            rect = VideoEffect.Rect.leftBottom;
                            break;
                        case 3:
                            rect = VideoEffect.Rect.rightBottom;
                            break;
                        case 4:
                            rect = VideoEffect.Rect.center;
                            break;
                        default:
                            rect = VideoEffect.Rect.leftTop;
                            break;
                    }
                }catch (Exception e){

                }
                water(rect,x,y);
            }
        });
    }

    private void water(VideoEffect.Rect rect,int x, int y){
        Bitmap bitmap = ImageEditer.getInstance().addWaterMark(mSrcBitmap,mWaterBitmap,rect,x,y);
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
