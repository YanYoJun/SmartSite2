package com.isoftstone.smartsite.model.tripartite.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.google.gson.Gson;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.patrolreport.ReportBean;
import com.isoftstone.smartsite.model.tripartite.fragment.PictureSlideFragment;
import com.isoftstone.smartsite.model.tripartite.fragment.ReadImgFragment;
import com.isoftstone.smartsite.utils.FilesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by issuser on 2018/2/7.
 */

public class ReadImageActivity extends BaseActivity {
    private HttpPost mHttpPost=new HttpPost();
    private ViewPager viewPager;
    private List mFragList;
    private int position;
    private ReportBean data;
    private List list=new ArrayList<>();
    private ArrayList<String> reportFiles=new ArrayList<>();

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_read_msg;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        position = getIntent().getIntExtra("position",0);
        mFragList = new ArrayList<ReadImgFragment>();
        String reportData = getIntent().getStringExtra("reportBean");
        Gson gson=new Gson();
        data = gson.fromJson(reportData,ReportBean.class);
        reportFiles = data.getReportFiles();
        initDate();
//        viewPager.setAdapter(new SamplePagerAdapter());
        viewPager.setAdapter(new PictureSlidePagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(position);
    }

    private void initDate() {
        list.clear();
        for (String absPath: reportFiles) {
            String formatPath = FilesUtils.getFormatString(absPath);
            if (TripartiteActivity.mImageList.contains(formatPath)) {
                String filePath = mHttpPost.getReportPath(data.getId(), absPath);
                String fileUrl = mHttpPost.getFileUrl(absPath);
                list.add(fileUrl);
            }
        }
    }

    private class SamplePagerAdapter extends PagerAdapter {

        private PhotoView photoView;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
                photoView = new PhotoView(container.getContext());
                photoView.setImageBitmap(BitmapFactory.decodeFile((String) list.get(position)) );
                container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return photoView;
        }
    }
    private  class PictureSlidePagerAdapter extends FragmentStatePagerAdapter {

        public PictureSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PictureSlideFragment.newInstance((String) list.get(position));
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
