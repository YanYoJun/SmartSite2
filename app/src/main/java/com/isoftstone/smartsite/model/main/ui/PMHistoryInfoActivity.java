package com.isoftstone.smartsite.model.main.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.common.widget.PullToRefreshListView;
import com.isoftstone.smartsite.http.aqi.DataQueryVoBean;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.aqi.DataQueryVoBeanPage;
import com.isoftstone.smartsite.http.pageable.PageableBean;
import com.isoftstone.smartsite.model.main.adapter.PMHistoryinfoAdapter;
import com.isoftstone.smartsite.model.map.ui.VideoMonitorMapActivity;

import java.util.ArrayList;

/**
 * Created by gone on 2017/10/21.
 */

public class PMHistoryInfoActivity extends BaseActivity {
    private ImageButton mImageView_back = null;
    private ImageButton mImageView_icon = null;
    private TextView toolbar_title = null;

    private PullToRefreshListView mListView = null;
    private TextView mDevicesName = null;
    private TextView mMap = null;
    private LinearLayout mGotoMap = null;
    public static  final  int HANDLER_GET_DATA_START = 1;
    public static  final  int HANDLER_GET_DATA_END = 2;
    private HttpPost mHttpPost = new HttpPost();
    private ArrayList<DataQueryVoBean> list = new ArrayList<DataQueryVoBean>();
    private int devicesId ;
    private String address;
    private String[] data = {"5分钟","1小时","24小时","1个月"};
    private Spinner mJiangeSpinner;
    private int position;
    private ArrayList<DataQueryVoBean> mData  = null;
    private PMHistoryinfoAdapter pmHistoryinfoAdapter = null;
    private DataQueryVoBeanPage mDataQueryVoBeanPage = null;
    private int mCurPageNum = 0;
    private int mFlag = -1;
    private String devicesCode;
    @Override
    protected int getLayoutRes() {
        return R.layout.activity_pmhistoryinfo;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        position = getIntent().getIntExtra("position",0);
        mData = (ArrayList<DataQueryVoBean>) getIntent().getSerializableExtra("devices");
        devicesId = getIntent().getIntExtra("id",0);
        address = getIntent().getStringExtra("address");
        devicesCode = getIntent().getStringExtra("devicesCode");
        init();
        setOnCliceked();
        mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
    }

    private void init(){
        mImageView_back = (ImageButton)findViewById(R.id.btn_back);
        mImageView_icon = (ImageButton)findViewById(R.id.btn_icon);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(R.string.history_data);
        mImageView_icon.setVisibility(View.INVISIBLE);
        mImageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mImageView_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mListView = (PullToRefreshListView)findViewById(R.id.listview);
        pmHistoryinfoAdapter = new PMHistoryinfoAdapter(getBaseContext());
        pmHistoryinfoAdapter.setData(list);
        mListView.setAdapter(pmHistoryinfoAdapter);
        mListView.setOnRefreshListener(listviewlistener);
        mDevicesName = (TextView)findViewById(R.id.textView1);
        mMap = (TextView)findViewById(R.id.textView4);
        mMap.setText(address);
        mGotoMap = (LinearLayout)findViewById(R.id.gotomap);
        mJiangeSpinner = (Spinner)findViewById(R.id.jiange_name);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.spinner_item,data);
        mJiangeSpinner.setAdapter(adapter);
        mJiangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFlag = -1;
                mCurPageNum = 0;
                list.clear();
                pmHistoryinfoAdapter.notifyDataSetChanged();
                mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posit, long id) {
                if(list != null){
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),PMDataInfoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("devicesbean",list.get(posit-1));
                    intent.putExtra("devices",mData);
                    intent.putExtra("position",position);
                    intent.putExtra("devicesCode",devicesCode);
                    getApplicationContext().startActivity(intent);
                }
            }
        });
        mDevicesName.setText(devicesCode);
    }

    PullToRefreshListView.OnRefreshListener listviewlistener = new PullToRefreshListView.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if(mDataQueryVoBeanPage.isFirst()){
                mCurPageNum = 0;
                mFlag = 1;
                mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
            }else {
                mListView.onRefreshComplete();
            }
        }

        @Override
        public void onLoadMore() {
            if(!mDataQueryVoBeanPage.isLast()){
                mCurPageNum = mCurPageNum + 1;
                mFlag = 2;
                mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
            }else{
                mListView.onLoadMoreComplete();
            }
        }
    };

    private void setOnCliceked(){

        mGotoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到地图
                Intent intent = new Intent();
                if(mData != null){
                    intent.putExtra("devices",mData);
                    intent.putExtra("position",position);
                    intent.putExtra("type",VideoMonitorMapActivity.TYPE_ENVIRONMENT);
                    intent.setClass(getBaseContext(),VideoMonitorMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(intent);
                }

            }
        });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_GET_DATA_START:{
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            getDevices();
                        }
                    };
                    thread.start();
                }
                break;
                case  HANDLER_GET_DATA_END:{
                    setmListViewData();
                }
                break;
            }
        }
    };

    private void getDevices(){
        int index = mJiangeSpinner.getSelectedItemPosition();
        PageableBean pageableBean = new PageableBean();
        pageableBean.setPage(mCurPageNum+"");
        mDataQueryVoBeanPage = mHttpPost.onePMDevicesDataListPage("["+devicesId+"]",(index+1)+"","","",pageableBean);
        mHandler.sendEmptyMessage(HANDLER_GET_DATA_END);
    }
    private void setmListViewData(){
        if(mFlag == -1){
            mListView.onRefreshComplete();
            mListView.onLoadMoreComplete();
        }
        if(mFlag == 1){
            list.clear();
            mListView.onRefreshComplete();
        }
        if(mFlag == 2){
            mListView.onLoadMoreComplete();
        }
        ArrayList<DataQueryVoBean> contest = mDataQueryVoBeanPage.getContent();
        if(contest != null){
            for (int i = 0 ; i < contest.size(); i ++){
                list.add(contest.get(i));
            }
        }
        pmHistoryinfoAdapter.notifyDataSetChanged();

    }

}
