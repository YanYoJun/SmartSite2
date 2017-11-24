package com.isoftstone.smartsite.model.dirtcar.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

/**
 * Created by zhang on 2017/11/24.
 */

public class DisplayPhotoActivity extends BaseActivity implements View.OnClickListener{
    String mImageUrl = "";

    private ImageView mImageUrlView;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_display_photo;
    }

    @Override
    protected void afterCreated(Bundle bundle) {
		/*获取Intent中的Bundle对象*/
        if(bundle == null) {
            bundle = this.getIntent().getExtras();
        }
        /*获取Bundle中的数据，注意类型和key*/
        mImageUrl = bundle.getString("imageUrl");

        initToolbar();
        initView();
    }

    private void initView() {
        mImageUrlView = (ImageView) findViewById(R.id.show_photo_view);
        showPhoto();
    }

    private void initToolbar(){
        TextView tv_title = (TextView) findViewById(R.id.toolbar_title);
        tv_title.setText(R.string.camera_details_title);

        findViewById(R.id.btn_back).setOnClickListener(DisplayPhotoActivity.this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                DisplayPhotoActivity.this.finish();
                break;
            default:
                break;
        }
    }

    private void showPhoto() {
        //创建网络请求对象
        AsyncHttpClient client= new AsyncHttpClient();
        client.get(mImageUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200) {
                    //创建工厂对象
                    BitmapFactory bitmapFactory = new BitmapFactory();
                    //工厂对象的decodeByteArray把字节转换成Bitmap对象
                    Bitmap bitmap = bitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
                    //设置图片
                    mImageUrlView.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {
                error.printStackTrace();
            }
        });
    }

}
