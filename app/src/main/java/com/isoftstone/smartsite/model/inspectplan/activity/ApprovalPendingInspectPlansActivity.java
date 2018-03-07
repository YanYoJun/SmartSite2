package com.isoftstone.smartsite.model.inspectplan.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.common.NetworkStateService;
import com.isoftstone.smartsite.common.widget.PullToRefreshListView;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.pageable.PageableBean;
import com.isoftstone.smartsite.http.patrolplan.PatrolPlanBean;
import com.isoftstone.smartsite.http.patrolplan.PatrolPlanBeanPage;
import com.isoftstone.smartsite.http.user.BaseUserBean;
import com.isoftstone.smartsite.model.inspectplan.adapter.ApprovalPendingInspectPlansAdapter;
import com.isoftstone.smartsite.model.inspectplan.bean.InspectPlanBean;
import com.isoftstone.smartsite.utils.NetworkUtils;
import com.isoftstone.smartsite.utils.ToastUtils;
import com.isoftstone.smartsite.utils.Utils;
import java.util.ArrayList;

/**
 * Created by zhang on 2017/11/18.
 */

public class ApprovalPendingInspectPlansActivity extends BaseActivity implements View.OnClickListener, NetworkStateService.NetEventHandler, ApprovalPendingInspectPlansAdapter.AdapterViewOnClickListener{
    private static final String TAG = "zzz_InspectPlans";
    private PullToRefreshListView mListView = null;
    private ArrayList<InspectPlanBean> mListData = new ArrayList<InspectPlanBean>();
    private HttpPost mHttpPost;
    private TextView mNetWorkMsgView;
    private Context mContext;
    private ApprovalPendingInspectPlansAdapter mAdapter;

    /* 查询请求识别码 查询成功*/
    private static final int QUERY_RESULTS_SUCCESSFUL_CODE = 1;
    /* 查询请求识别码 查询失败*/
    private static final int QUERY_RESULTS_FAILED_CODE = 2;
    /* 查询请求识别码 查询异常*/
    private static final int QUERY_RESULTS_EXCEPTION_CODE = 3;
    /* 查询请求识别码 已查询出所有*/
    private static final int QUERY_RESULTS_MAX_PAGE_CODE = 4;

    /* 请求识别码*/
    public static final int REQUEST_CODE = 1;

    //listview分页参数
    private int mCurPageNum = -1;
    public boolean isLoading = false;


    private void setListViewData() {
        mAdapter = new ApprovalPendingInspectPlansAdapter(ApprovalPendingInspectPlansActivity.this, mListData);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_approval_pending_inspect_plans;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {

        NetworkStateService.NetworkConnectChangedReceiver.mListeners.add(this);

        initToolbar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //QueryDataTask queryDataTask = new QueryDataTask(this);
        //queryDataTask.execute();
    }

    private void initView() {
        mHttpPost = new HttpPost();
        mContext = getApplicationContext();

        mListView = (PullToRefreshListView) findViewById(R.id.list_view);
        //mNetWorkMsgView = (TextView) findViewById(R.id.net_work_msg);
        //mNetWorkMsgView.setVisibility(NetworkUtils.isConnected() ? View.GONE : View.VISIBLE);

        PullToRefreshListView.OnRefreshListener refreshListener = new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isRefreshing()) {
                    Log.e(TAG,".... onRefresh    mCurPageNum "+ mCurPageNum);
                    new QueryDataTask(mContext, false).execute();
                }
            }

            @Override
            public void onLoadMore() {
                if(!isRefreshing()) {
                    Log.e(TAG,".... onLoadMore    mCurPageNum "+ mCurPageNum);
                    new QueryDataTask(mContext, false).execute();
                }
            }
        };

        mListView.setOnRefreshListener(refreshListener);
        setListViewData();
        new QueryDataTask(mContext, true).execute();
    }

    private void initToolbar(){
        TextView tv_title = (TextView) findViewById(R.id.toolbar_title);
        tv_title.setText(R.string.approval_pending_inspect_plans_title);

        findViewById(R.id.btn_back).setOnClickListener(ApprovalPendingInspectPlansActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Log.i("zzz","aaaaaaaaa...aaaaaaaaaaaaaaaa..." +  intent);

        if (resultCode == Activity.RESULT_OK) {
            if (mListData != null) {
                mListData.clear();
                mCurPageNum = -1;
            }
            Log.i("zzz","aaaaaaaaa...aaaaaaaaaaaaa..." +  intent.getData());
            new QueryDataTask(mContext, true).execute();
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_back:
                ApprovalPendingInspectPlansActivity.this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNetChange(boolean isConnected) {
        //mNetWorkMsgView.setText("当前网络不可用，请检查您的网络设置");
        Log.i("zzz","onNetChange...........");
        if (isConnected) {
            //mNetWorkMsgView.setVisibility(View.GONE);
        } else {
            //mNetWorkMsgView.setVisibility(View.VISIBLE);
        }
    }

    public void setListViewRefreshStatus(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isRefreshing() {
        return this.isLoading;
    }

    @Override
    public void viewOnClickListener(InspectPlanBean inspectPlanBean) {
        if (!NetworkUtils.isConnected()){
            ToastUtils.showShort(mContext.getText(R.string.network_can_not_be_used_toast).toString());
            return;
        }
        enterPatrolPlan(inspectPlanBean);
    }

    private void enterPatrolPlan(InspectPlanBean inspectPlanBean) {

        PatrolPlanBean patrolPlanBean = new PatrolPlanBean();
        patrolPlanBean.setId(inspectPlanBean.getUserId());
        patrolPlanBean.setEndDate(inspectPlanBean.getTaskTimeEnd());
        patrolPlanBean.setStart(inspectPlanBean.getTaskTimeStart());
        patrolPlanBean.setStatus(inspectPlanBean.getTaskStatus());
        BaseUserBean baseUserBean = new BaseUserBean();
        baseUserBean.setId(inspectPlanBean.getBaseUserBean().getId());
        patrolPlanBean.setCreator(baseUserBean);
        Intent intent = new Intent(mContext, PatrolPlanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("patrolplan",patrolPlanBean);
        startActivityForResult(intent, REQUEST_CODE);
    }

    class QueryDataTask extends AsyncTask<Void,Void,Integer> {
        private Context sContext;
        private boolean isReLoading;
        QueryDataTask(Context context, boolean isFirstLoading) {
            this.sContext = context;
            this.isReLoading = isFirstLoading;
        }

        /**
         * 运行在UI线程中，在调用doInBackground()之前执行
         */
        @Override
        protected void onPreExecute() {
            setListViewRefreshStatus(true);
            if (isReLoading) {
                showDlg(getText(R.string.dialog_load_messgae).toString());
            }
        }
        /**
         * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法
         */
        @Override
        protected Integer doInBackground(Void... params) {

            int totalPages = 0;

            try {
                PatrolPlanBean patrolPlanBean = new PatrolPlanBean();
                PageableBean pageableBean = new PageableBean();
                //pageableBean.setSize(2 + "");
                pageableBean.setPage((mCurPageNum + 1) + "");
                PatrolPlanBeanPage patrolPlanBeanPage = mHttpPost.getPlanPaging(patrolPlanBean,pageableBean,"date,desc");
                ArrayList<PatrolPlanBean> arrayList = patrolPlanBeanPage.getContent();
                Log.i("zzz","AAAAAAAAAAAAAAAAAAAAA     patrolPlanBeanPage = " + patrolPlanBeanPage.toString());
                if (arrayList == null || arrayList.size() == 0) {
                    totalPages = patrolPlanBeanPage.getTotalPages();

                    if (totalPages != 0 && totalPages == mCurPageNum + 1) {
                        return  QUERY_RESULTS_MAX_PAGE_CODE;
                    } else {
                        return  QUERY_RESULTS_FAILED_CODE;
                    }
                } else {
                    mCurPageNum++;
                }

                for (int i=0; i< arrayList.size(); i++) {
                    Log.i("zzz","arrayList.size() = " + arrayList.size() + "  & " + i + "  && " + arrayList.get(i).toString());
                    InspectPlanBean inspectPlanBean = new InspectPlanBean();
                    inspectPlanBean.setUserId(arrayList.get(i).getId());
                    inspectPlanBean.setTaskName(arrayList.get(i).getTitle());
                    inspectPlanBean.setTaskTimeStart(arrayList.get(i).getStart());
                    inspectPlanBean.setTaskTimeEnd(arrayList.get(i).getEndDate());
                    inspectPlanBean.setTaskCreateTime(arrayList.get(i).getDate());
                    inspectPlanBean.setUserName(arrayList.get(i).getCreator().getName());
                    inspectPlanBean.setUserCompany(Utils.isEmptyStr(arrayList.get(i).getCreator().getDepartmentId()) ?  "" : mHttpPost.getCompanyNameByid(Integer.parseInt(arrayList.get(i).getCreator().getDepartmentId())));
                    inspectPlanBean.setTaskStatus(arrayList.get(i).getStatus());
                    inspectPlanBean.setBaseUserBean(arrayList.get(i).getCreator());
                    //inspectPlanBean.setAddress(arrayList.get(i));
                    mListData.add(inspectPlanBean);
                }

            } catch (Exception e) {
                Log.e(TAG,"e : " + e.getMessage());
                e.printStackTrace();
                return QUERY_RESULTS_EXCEPTION_CODE;
            }

            return QUERY_RESULTS_SUCCESSFUL_CODE;
        }

        /**
         * 运行在ui线程中，在doInBackground()执行完毕后执行
         */
        @Override
        protected void onPostExecute(Integer resultsCode) {
            super.onPostExecute(resultsCode);

            setListViewRefreshStatus(false);

            //Toast.makeText(context,"执行完毕",Toast.LENGTH_SHORT).show();
            if (resultsCode == QUERY_RESULTS_SUCCESSFUL_CODE) {
                //setListViewData();
            } else if (resultsCode == QUERY_RESULTS_FAILED_CODE){
                ToastUtils.showLong(getString(R.string.device_list_null));
            } else if (resultsCode == QUERY_RESULTS_EXCEPTION_CODE) {
                ToastUtils.showLong(getString(R.string.device_list_failure));
            } else if (resultsCode == QUERY_RESULTS_MAX_PAGE_CODE) {
                ToastUtils.showLong(getString(R.string.arrive_max));
            }

            if (isReLoading) {
                closeDlg();
            }
            mListView.onLoadMoreComplete();
            mListView.onRefreshComplete();
            mAdapter.notifyDataSetChanged();
        }

        /**
         * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
         */
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}
