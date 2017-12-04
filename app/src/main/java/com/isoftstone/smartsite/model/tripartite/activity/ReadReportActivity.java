package com.isoftstone.smartsite.model.tripartite.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.PatrolBean;
import com.isoftstone.smartsite.model.tripartite.fragment.ReadReportFrag;
import com.isoftstone.smartsite.model.tripartite.fragment.ViewReplyReportFragment;

/**
 * Created by yanyongjun on 2017/10/19.
 * 查看巡查报告界面
 */

public class ReadReportActivity extends BaseActivity {
    private int mId = 0;
    private HttpPost mHttpPost = null;
    private PatrolBean mData = null;
    private ReadReportFrag mReadReportFrag = null;
    private ViewReplyReportFragment mViewReplyReportFrag = null;
    private Handler mHandler = new Handler();

    public PatrolBean getReportData() {
        return mData;
    }


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_read_report;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        Intent i = getIntent();
        mId = i.getIntExtra("_id", 0);
        if (CheckReportActivity.isDebug) {
            mId = CheckReportActivity.debugMsg;
        }
        mReadReportFrag = (ReadReportFrag) getSupportFragmentManager().findFragmentById(R.id.frag_inspect_report_view);
        mViewReplyReportFrag = (ViewReplyReportFragment) getSupportFragmentManager().findFragmentById(R.id.frag_view_reply_inspect_report);
        mHttpPost = new HttpPost();
        new QueryBaseData().execute();
    }

    private class QueryBaseData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            mData = mHttpPost.getPatrolReport(mId + ""); //TODO
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mReadReportFrag.notifyDataChanged();
            mViewReplyReportFrag.notifyDataSetChanged();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScrollView scrool = (ScrollView) findViewById(R.id.scrollview);
                    if (scrool != null) {
                        scrool.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }
            }, 0);
        }
    }

}
