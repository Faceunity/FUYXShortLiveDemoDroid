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

/**
 * 图片裁剪
 */
public class ImageCropFragment extends BaseFragment {


    private EditText mCropX;
    private EditText mCropY;
    private EditText mCropWidth;
    private EditText mCropHeight;
    private ImageView mDstImage;
    private Bitmap mSrcBitmap;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_image_crop, container, false);
        initUI(parent);
        return parent;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initUI(View root){
        mSrcBitmap = BitmapFactory.decodeResource(getResources(),R.raw.koala);
        ImageView src = (ImageView) root.findViewById(R.id.image_crop_src);
        src.setImageBitmap(mSrcBitmap);
        TextView textView = (TextView) root.findViewById(R.id.image_crop_size);
        textView.setText(mSrcBitmap.getWidth() + "x" +mSrcBitmap.getHeight());

        mCropX = (EditText) root.findViewById(R.id.image_crop_x_pos);
        mCropY = (EditText) root.findViewById(R.id.image_crop_y_pos);
        mCropWidth = (EditText) root.findViewById(R.id.image_crop_width);
        mCropHeight = (EditText) root.findViewById(R.id.image_crop_height);
        mDstImage = (ImageView) root.findViewById(R.id.image_crop_dst);

        View start = root.findViewById(R.id.simage_crop_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mCropX.getText()) || TextUtils.isEmpty(mCropY.getText())
                        ||TextUtils.isEmpty(mCropWidth.getText()) || TextUtils.isEmpty(mCropHeight.getText())){
                    showToast("请填写正确的参数");
                    return;
                }
                int x = Integer.parseInt(mCropX.getText().toString());
                int y = Integer.parseInt(mCropY.getText().toString());
                int width = Integer.parseInt(mCropWidth.getText().toString());
                int height = Integer.parseInt(mCropHeight.getText().toString());
                crop(x,y,width,height);
            }
        });
    }

    private void crop(int x, int y,int width,int height){
        boolean paramCheck = mSrcBitmap.getWidth() >= width && mSrcBitmap.getHeight() >= height;
        if(paramCheck){
            Bitmap bitmap = ImageEditer.getInstance().crop(mSrcBitmap,x,y,width,height);
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
        }else {
            showToast("参数错误");
        }
    }
}
