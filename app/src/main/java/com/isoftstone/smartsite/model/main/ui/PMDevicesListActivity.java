package com.isoftstone.smartsite.model.main.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.common.widget.PullToRefreshListView;
import com.isoftstone.smartsite.http.aqi.DataQueryVoBean;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.aqi.DataQueryVoBeanPage;
import com.isoftstone.smartsite.http.pageable.PageableBean;
import com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter;

import java.util.ArrayList;

/**
 * Created by gone on 2017/10/21.
 */

public class PMDevicesListActivity extends BaseActivity {

    private PullToRefreshListView mListView = null;
    private PMDevicesListAdapter adapter;
    public static  final  int HANDLER_GET_DATA_START = 1;
    public static  final  int HANDLER_GET_DATA_END = 2;
    public static  final  int HANDLER_LOAD_MORE = 3;
    private HttpPost mHttpPost = new HttpPost();
    private ArrayList<DataQueryVoBean> mList = new ArrayList<DataQueryVoBean>();

    private ImageButton mImageView_back = null;
    private ImageButton mImageView_serch = null;
    private View oneIconLayout = null;
    private View searchLayout = null;
    private ImageButton mSearch_back = null;
    private TextView mSearch_cancel = null;
    private TextView mtitleTextView = null;
    private ImageButton search_btn_search = null;
    private EditText search_edit_text = null;
    private DataQueryVoBeanPage mDataQueryVoBeanPage;
    private int mCurrentPage = 0;
    private int mFlag = -1;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_pmdeviceslist;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        init();
        showDlg(getString(R.string.pmd_data_loading));
        mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
    }

    private void init(){
        mImageView_back = (ImageButton)findViewById(R.id.btn_back);
        mImageView_serch = (ImageButton)findViewById(R.id.btn_icon);
//        mBtn_serch = (ImageButton)findViewById(R.id.btn_search);
//        mBtn_serch.setVisibility(View.VISIBLE);
        oneIconLayout = (View)findViewById(R.id.one_icon);
        searchLayout = (View)findViewById(R.id.serch);
        mSearch_back = (ImageButton)findViewById(R.id.search_btn_back);
        mSearch_cancel = (TextView)findViewById(R.id.search_btn_icon_right);
        mtitleTextView = (TextView) findViewById(R.id.toolbar_title);
        search_btn_search = (ImageButton)findViewById(R.id.search_btn_search);
        search_edit_text = (EditText)findViewById(R.id.search_edit_text);
        mtitleTextView.setText(getString(R.string.pmd_device_list));
        mImageView_serch.setImageResource(R.drawable.search);
        mImageView_back.setOnClickListener(listener);
//        mBtn_serch.setOnClickListener(listener);
        mImageView_serch.setOnClickListener(listener);
        mSearch_back.setOnClickListener(listener);
        mSearch_cancel.setOnClickListener(listener);
        search_btn_search.setOnClickListener(listener);


        mListView = (PullToRefreshListView)findViewById(R.id.listview);
        adapter = new PMDevicesListAdapter(getBaseContext());
        adapter.setData(mList);
        mListView.setAdapter(adapter);
        mListView.setOnRefreshListener(mOnRefreshListener);

        mImageView_serch.setVisibility(View.VISIBLE);
    }

    PullToRefreshListView.OnRefreshListener mOnRefreshListener = new PullToRefreshListView.OnRefreshListener() {
        @Override
        public void onRefresh() {
            //下拉刷新
            if(mDataQueryVoBeanPage!=null){
                if( mDataQueryVoBeanPage.isFirst()){
                    mCurrentPage = 0;
                    mFlag = 1;
                    mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
                }else{
                    mListView.onRefreshComplete();
                }
            }else{
                mCurrentPage = 0;
                mFlag = 1;
                mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
            }

        }

        @Override
        public void onLoadMore() {
            //上拉刷新
            if(!mDataQueryVoBeanPage.isLast()){
                mCurrentPage = mCurrentPage + 1;
                mFlag = 2;
                mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
            }else {
                mHandler.sendEmptyMessage(HANDLER_LOAD_MORE);
            }
        }
    };
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
                case HANDLER_LOAD_MORE:{
                    mListView.onLoadMoreComplete();
                }
                break;
            }
        }
    };

    private  void getDevices(){
        PageableBean pageableBean = new PageableBean();
        pageableBean.setPage(mCurrentPage+"");
        mDataQueryVoBeanPage = mHttpPost.onePMDevicesDataListPage("","0","","",pageableBean);
        mHandler.sendEmptyMessage(HANDLER_GET_DATA_END);
    }

    private void setmListViewData(){
        if(mFlag == 1){
            mList.clear();
            mListView.onRefreshComplete();
        }
        if(mFlag == 2){
            mListView.onLoadMoreComplete();
        }
        if(mDataQueryVoBeanPage != null){
            ArrayList<DataQueryVoBean> list =mDataQueryVoBeanPage.getContent();
            if(list != null){
               for (int i = 0 ; i < list.size() ;i ++){
                   mList.add(list.get(i));
               }
            }
        }
        closeDlg();
        adapter.notifyDataSetChanged();
    }
    View.OnClickListener listener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btn_back:{
                    finish();
                }
                break;
                case R.id.btn_icon:{
                    oneIconLayout.setVisibility(View.GONE);
                    searchLayout.setVisibility(View.VISIBLE);
                }
                break;
                case R.id.search_btn_back:{
                    finish();
                }
                break;
                case R.id.search_btn_icon_right:{
                    oneIconLayout.setVisibility(View.VISIBLE);
                    searchLayout.setVisibility(View.GONE);
                }
                break;
                case R.id.search_btn_search:{
                    String serch = search_edit_text.getText().toString();
                    Toast.makeText(getBaseContext(),getString(R.string.pmd_seacher_content)+serch,Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    };
}
