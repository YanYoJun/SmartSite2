package com.isoftstone.smartsite.model.message.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.common.widget.PullToRefreshListView;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.message.BeforeNMessageBean;
import com.isoftstone.smartsite.http.message.MessageBean;
import com.isoftstone.smartsite.http.message.MessageBeanPage;
import com.isoftstone.smartsite.http.pageable.PageableBean;
import com.isoftstone.smartsite.http.patrolplan.PatrolPlanBean;
import com.isoftstone.smartsite.http.patrolreport.PatrolBean;
import com.isoftstone.smartsite.http.user.BaseUserBean;
import com.isoftstone.smartsite.http.video.DevicesBean;
import com.isoftstone.smartsite.jpush.MyReceiver;
import com.isoftstone.smartsite.model.inspectplan.activity.PatrolPlanActivity;
import com.isoftstone.smartsite.model.map.ui.VideoMonitorMapActivity;
import com.isoftstone.smartsite.model.message.MessageUtils;
import com.isoftstone.smartsite.model.message.adapter.MsgListAdapter;
import com.isoftstone.smartsite.model.message.data.MsgData;
import com.isoftstone.smartsite.model.muckcar.ui.SlagcarInfoActivity;
import com.isoftstone.smartsite.model.tripartite.data.ReportData;
import com.isoftstone.smartsite.model.tripartite.fragment.InspectReportMainFragment;
import com.isoftstone.smartsite.utils.LogUtils;
import com.isoftstone.smartsite.utils.MsgUtils;
import com.isoftstone.smartsite.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yanyongjun on 2017/10/15.
 */

public class MessageListActivity extends BaseActivity {
    private Activity mActivity = null;
    private PullToRefreshListView mListView = null;
    private ArrayList<MsgData> mDatas = new ArrayList<>();
    private ArrayList<MessageBean> mDataBeans = new ArrayList<>();
    private String planid;
    private HttpPost mHttpPost = null;
    private BaseAdapter mAdapter = null;

    private String mQueryMsgType = MessageUtils.SEARCH_CODE_ENVIRON;
    //分页开始
    private int mCurPageNum = -1;
    public boolean isLoading = false;
    ArrayList<DevicesBean> mData = new ArrayList<DevicesBean>();
    private OkHttpClient mClient=new OkHttpClient();
    private PatrolPlanBean patrolPlanBean;
    private String plan_url;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_msg_vcr;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        mActivity = this;
        mHttpPost = new HttpPost();
        Intent intent = getIntent();
        try {
            mQueryMsgType = intent.getStringExtra("type");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    PageableBean pageableBean = new PageableBean();
                    mData =  mHttpPost.getDevicesListPage("1","","","",pageableBean).getContent();
                } catch (Exception e) {
                    Log.i(TAG,"throw a exception: " + e.getMessage());
                }
            }
        };
        thread.start();
        initListView();
        initTitleName();
    }

    private void initListView() {
        mListView = (PullToRefreshListView) mActivity.findViewById(R.id.listview_message);

        PullToRefreshListView.OnRefreshListener listener = new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "yanlog onRefresh:" + isLoading);
                if (isLoading) {
                    //mListView.onRefreshComplete();
                } else {
                    mCurPageNum = -1;
                    new QueryMsgTask(true).execute();
                }
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "yanlog onLoadMore:" + isLoading);
                if (isLoading) {
                    // mListView.onLoadMoreComplete();
                } else {
                    new QueryMsgTask(false).execute();
                }
            }
        };
        mListView.setOnRefreshListener(listener);
        mAdapter = new MsgListAdapter(mActivity, mDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "yanlog postion:" + position);
                MessageBean bean=mDataBeans.get(position - 1);
                if (bean==null){
                    return;
                }
                String searchCode = bean.getInfoType().getSearchCode();
                if (searchCode==null){
                    return;
                }
                if (searchCode.equals(MessageUtils.SEARCH_CODE_VEDIO_OFFLINE)||searchCode.equals(MessageUtils.SEARCH_CODE_ENVIRON_PM10_LIMIT)
                        ||searchCode.equals(MessageUtils.SEARCH_CODE_DIRTCAR_ZUIZONG)){
                    Intent intent = new Intent();
                    intent.putExtra("type", VideoMonitorMapActivity.TYPE_CAMERA);
                    intent.putExtra("devices",mData);
                    intent.putExtra("position",position);
                    intent.setClass(mActivity,VideoMonitorMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                }
                else if (searchCode.equals(MessageUtils.SEARCH_CODE_PLAN_REJECT) || searchCode.equals(MessageUtils.SEARCH_CODE_PLAN_APPROVAL) ||
                        searchCode.equals(MessageUtils.SEARCH_CODE_PLAN_PASS)){
                        String extraParam=bean.getExtraParam();
                    try {
                        JSONObject jsonObject=new JSONObject(extraParam);
                        planid = (String) jsonObject.get("id");
                        Log.i("name", planid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //巡查消息详情
                    plan_url = HttpPost.URL + "/patrol/plan"+"/"+planid;
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            getData(plan_url,mClient);
//                        }
//                    }).start();
                    new QueryPlanDetailTask().execute();
                    Intent intent = new Intent(MessageListActivity.this, PatrolPlanActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("patrolplan",patrolPlanBean);
                    mActivity.startActivity(intent);
                }
                else {
                    MessageUtils.enterActivity(MessageListActivity.this, bean);
                }

            }
        });
        new QueryMsgTask(true).execute();
    }

    private void getData(String plan_url,OkHttpClient mClient) {
        Request request = new Request.Builder()
                .url(plan_url)
                .get()
                .build();
        Response response = null;
        try {
            response = mClient.newCall(request).execute();
            if (response.code() == HttpPost.HTTP_LOGIN_TIME_OUT) {
                HttpPost.autoLogin();
                getData(plan_url, mClient);
            }
            if (response.isSuccessful()) {
                String responsebody = response.body().string();
                LogUtils.i(TAG, " responsebody  " + responsebody);
                JSONObject jsonObject = new JSONObject(responsebody);
                patrolPlanBean = new PatrolPlanBean();
                patrolPlanBean.setId((Long) jsonObject.get("id"));
                patrolPlanBean.setEndDate((String) jsonObject.get("endDate"));
                patrolPlanBean.setStart((String) jsonObject.get("startDate"));
                patrolPlanBean.setStatus((Integer) jsonObject.get("status"));
                BaseUserBean baseUserBean = new BaseUserBean();
                baseUserBean.setId((Long) jsonObject.getJSONObject("creator").get("id"));
                patrolPlanBean.setCreator(baseUserBean);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initTitleName() {
        TextView title = (TextView) findViewById(R.id.lab_title_name);
        if (mQueryMsgType.equals(MessageUtils.SEARCH_CODE_ENVIRON)) {
            title.setText("环境监控消息");
        } else if (mQueryMsgType.equals(MessageUtils.SEARCH_CODE_VEDIO)) {
            title.setText("视频监控消息");
        } else if (mQueryMsgType.equals(MessageUtils.SEARCH_CODE_THREE_PARTY)) {
            title.setText("三方协同消息");
        } else if (mQueryMsgType.equals(MessageUtils.SEARCH_CODE_DIRTCAR)) {
            title.setText("渣土车监控消息");
        } else if (mQueryMsgType.equals(MessageUtils.SEARCH_CODE_TASK)) {
            title.setText("巡查任务消息");
        } else if (mQueryMsgType.equals(MessageUtils.SEARCH_CODE_PLAN)) {
            title.setText("巡查计划消息");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ReadMsgTask().execute();
    }


    private class QueryMsgTask extends AsyncTask<String, Integer, String> {
        private boolean mIsReLoad = true;

        public QueryMsgTask(boolean mIsReLoad) {
            this.mIsReLoad = mIsReLoad;
        }

        @Override
        protected void onPreExecute() {
            isLoading = true;
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                PageableBean page = new PageableBean();
                page.setSize(BaseActivity.DEFAULT_PAGE_SIZE);
                page.setPage(mCurPageNum + 1 + "");
                MessageBeanPage resultPage = mHttpPost.getMessagePage("", "", "", mQueryMsgType, page);
                ArrayList<MessageBean> msgs = resultPage.getContent();
                if (msgs != null) {
                    Collections.sort(msgs, new Comparator<MessageBean>() {
                        @Override
                        public int compare(MessageBean o1, MessageBean o2) {
                            try {
                                Date date1 = MsgData.format5.parse(o1.getUpdateTime());
                                Date date2 = MsgData.format5.parse(o2.getUpdateTime());
                                return date2.compareTo(date1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });
                    ArrayList<MsgData> temp = MsgUtils.toMsgData(msgs);
                     if (mIsReLoad) {
                        mDatas.clear();
                        mDataBeans.clear();
                    }
                    if (msgs != null && msgs.size() > 0) {
                        mCurPageNum++;
                    }
                    mDatas. addAll(temp);
                    mDataBeans.addAll(msgs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            isLoading = false;
            super.onPostExecute(s);
            Log.e(TAG, "defernotifyDatasetChanged");
            if (mDatas == null || mDatas.size() == 0) {
                ToastUtils.showShort("未获取到数据");
            }
            mListView.onLoadMoreComplete();
            mListView.onRefreshComplete();
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ReadMsgTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            for (MsgData temp : mDatas) {
                if (temp.getStatus() == MsgData.STATUS_UNREAD && temp.getId()!=null) {
                    mHttpPost.readMessage(temp.getId());
                }
            }
            return null;
        }
    }

    private class QueryPlanDetailTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getData(plan_url,mClient);
            return null;
        }
    }
}
