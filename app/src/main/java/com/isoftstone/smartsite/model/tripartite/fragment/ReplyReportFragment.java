package com.isoftstone.smartsite.model.tripartite.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.base.BaseFragment;
import com.isoftstone.smartsite.http.PatrolBean;
import com.isoftstone.smartsite.http.ReportBean;
import com.isoftstone.smartsite.http.UserBean;
import com.isoftstone.smartsite.model.tripartite.activity.TripartiteActivity;
import com.isoftstone.smartsite.model.tripartite.adapter.AttachGridViewAdatper;
import com.isoftstone.smartsite.utils.DateUtils;
import com.isoftstone.smartsite.utils.FilesUtils;
import com.isoftstone.smartsite.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by yanyongjun on 2017/10/30.
 */

public class ReplyReportFragment extends BaseFragment {
    private GridView mAttachView = null;
    private AttachGridViewAdatper mAttachAdapter = null;
    private ArrayList<Object> mData = null;

    private EditText mEditContent = null;
    private Button mSubButton = null;
    private BaseActivity mActivity = null;
    private TextView mLabName = null;
    public final static int REQUEST_ACTIVITY_ATTACH = 0;//请求图片的request code
    private ArrayList<String> mFilesPath = new ArrayList<>();
    Dialog mLoginingDlg;


    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_reply_report;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        init();
    }

    private void init() {
        initView();
        initGridView();
    }

    private void initView() {
        mActivity = (BaseActivity) getActivity();
        mEditContent = (EditText) rootView.findViewById(R.id.edit_report_msg);
        mSubButton = (Button) rootView.findViewById(R.id.btn_add_report_submit);
        mSubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAllMsgSetted()) {
                    new DealTask(getReportBean()).execute();
                } else {
                    Toast.makeText(getActivity(), "您还有未填写的信息", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mLabName = (TextView) rootView.findViewById(R.id.lab_report_people_name);
        mLabName.setText(mHttpPost.mLoginBean.getmName());
    }

    public void initGridView() {
        mAttachView = (GridView) getView().findViewById(R.id.grid_view);

        mData = new ArrayList<Object>();
        mData.add(R.drawable.attachment);
        //mAttachAdapter = new SimpleAdapter(getActivity(), mData, R.layout.add_attach_grid_item, new String[]{"image"}, new int[]{R.id.image});
        mAttachAdapter = new AttachGridViewAdatper(getActivity(), mData);
        mAttachView.setAdapter(mAttachAdapter);

        mAttachView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mData.size() - 1) {
                    //点击添加附件
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("*/*");
                    startActivityForResult(i, REQUEST_ACTIVITY_ATTACH);
                } else {
                    mFilesPath.remove(position);
                    mData.remove(position);
                    mAttachAdapter.notifyDataSetChanged();
                    ToastUtils.showShort("附件删除成功");
                }
            }
        });
    }

    private boolean isAllMsgSetted() {
        if (TextUtils.isEmpty(mEditContent.getText())) {
            return false;
        }
        return true;
    }

    private ReportBean getReportBean() {
        PatrolBean tempBean = new PatrolBean();
        tempBean.setId(mActivity.getReportData().getId());
        ReportBean reportBean = new ReportBean();
        reportBean.setPatrol(tempBean);
        //reportBean.setCreator(mHttpPost.mLoginBean.getmName());
        reportBean.setContent(mEditContent.getText().toString()); //TODO
        reportBean.setDate(DateUtils.format_yyyy_MM_dd_HH_mm_ss.format(new Date()));
        reportBean.setCategory(2);
        reportBean.setStatus(mActivity.getReportData().getStatus());
        UserBean userBean = new UserBean();
        userBean.setId(mHttpPost.mLoginBean.getmUserBean().getId());
        tempBean.setCreator(userBean);
        return reportBean;
    }

    /* 初始化正在登录对话框 */
    private void initDlg() {

        mLoginingDlg = new Dialog(getActivity(), R.style.loginingDlg);
        mLoginingDlg.setContentView(R.layout.dialog_submit);

        mLoginingDlg.setCanceledOnTouchOutside(true); // 设置点击Dialog外部任意区域关闭Dialog
    }

    /* 显示正在登录对话框 */
    private void showDlg() {
        if (mLoginingDlg != null)
            mLoginingDlg.show();
    }

    /* 关闭正在登录对话框 */
    private void closeDlg() {
        if (mLoginingDlg != null && mLoginingDlg.isShowing())
            mLoginingDlg.dismiss();
    }


    private class DealTask extends AsyncTask<Void, Void, Boolean> {
        private ReportBean mBean = null;

        @Override
        protected void onPreExecute() {
            initDlg();
            showDlg();
            super.onPreExecute();
        }

        public DealTask(ReportBean reportBean) {
            mBean = reportBean;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.e(TAG, "deal task begin");
            try {
                mHttpPost.addPatrolReply(mBean); //添加回复
                if (mFilesPath != null && mFilesPath.size() >= 1) {
                    PatrolBean report = mHttpPost.getPatrolReport(mBean.getPatrol().getId() + "");
                    ArrayList<ReportBean> reports = report.getReports();
                    int id = report.getReports().get(reports.size() - 1).getId(); //查到刚才回复消息的id
                    for (String path : mFilesPath) {
                        Log.e(TAG,"yanlog update file:"+path);
                        mHttpPost.reportFileUpload(path, id); //上传附件
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean temp) {
            super.onPostExecute(temp);
            closeDlg();
            if (temp == true) {
                Toast.makeText(getActivity(), "提交成功", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), "提交失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACTIVITY_ATTACH: {
                Log.e(TAG, "onActivityResult:" + data);
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    Log.e(TAG, "yanlog uri:" + uri);
                    if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                        //Toast.makeText(getActivity(), uri.getPath() + "11111", Toast.LENGTH_SHORT).show();
                        addAttach(uri.getPath(), uri.toString());
                        return;
                    }
                    String path = FilesUtils.getPath(getActivity(), uri);
                    //Toast.makeText(getActivity(), path, Toast.LENGTH_SHORT).show();
                    addAttach(path, uri.toString());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //add files
    public void addAttach(String path, String uri) {
        Log.e(TAG, "yanlog remove begin size:" + mData.size());
        String formatPath = FilesUtils.getFormatString(path);
        Log.e(TAG, "yanlog remove begin size at0:" + mData.get(0));
        mData.remove(mData.size() - 1);
        mFilesPath.add(path);
        if (TripartiteActivity.mImageList.contains(formatPath)) {
            mData.add(uri);
        } else if (TripartiteActivity.mXlsList.contains(formatPath)) {
            mData.add(TripartiteActivity.mAttach.get(".xls"));
        } else if (TripartiteActivity.mDocList.contains(formatPath)) {
            mData.add(TripartiteActivity.mAttach.get(".doc"));
        } else if (TripartiteActivity.mPdfList.contains(formatPath)) {
            mData.add(TripartiteActivity.mAttach.get(".pdf"));
        } else if (TripartiteActivity.mPptList.contains(formatPath)) {
            mData.add(TripartiteActivity.mAttach.get(".ppt"));
        } else if (TripartiteActivity.mVideoList.contains(formatPath)) {
            mData.add(TripartiteActivity.mAttach.get(".video"));
        } else {
            mData.add(TripartiteActivity.mAttach.get(".doc"));
        }

        mData.add(R.drawable.attachment);
        Log.e(TAG, "yanlog remove end size:" + mData.size());
        Log.e(TAG, "yanlog mData at 0:" + mData.get(0));
        mAttachAdapter.notifyDataSetChanged();
        mAttachView.requestLayout();
        mAttachView.setMinimumHeight(600);
    }
}
