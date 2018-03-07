package com.isoftstone.smartsite.model.main.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.isoftstone.smartsite.MainActivity;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.model.main.adapter.InstantMessageAdapter;
import com.isoftstone.smartsite.model.main.utils.WindSpeed;
import com.isoftstone.smartsite.model.patroltask.ui.PatroPlanDetailsActivity;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.model.muckcar.ui.SlagcarInfoActivity;
import com.isoftstone.smartsite.base.BaseFragment;
import com.isoftstone.smartsite.http.aqi.DataQueryBean;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.message.MobileHomeBean;
import com.isoftstone.smartsite.model.inspectplan.activity.ApprovalPendingInspectPlansActivity;
import com.isoftstone.smartsite.model.inspectplan.activity.PatrolPlanActivity;
import com.isoftstone.smartsite.model.map.ui.ConstructionMonitorMapActivity;
import com.isoftstone.smartsite.model.map.ui.ConstructionSummaryActivity;
import com.isoftstone.smartsite.model.tripartite.activity.TripartiteActivity;
import com.isoftstone.smartsite.utils.NetworkUtils;
import com.isoftstone.smartsite.utils.ToastUtils;
import com.isoftstone.smartsite.utils.Utils;

import java.util.ArrayList;


/**
 * Created by zw on 2017/10/11.
 * <p>
 * <p>
 * 开发版SHA1 ： 17:02:19:67:57:D4:F4:AF:3E:AE:22:1F:95:65:9A:27:FD:F7:8D:D0
 * <p>
 * 17:02:19:67:57:D4:F4:AF:3E:AE:22:1F:95:65:9A:27:FD:F7:8D:D0;com.isoftstone.smartsite
 * <p>
 * <p>
 * 发布版SHA1 ： C3:83:83:56:68:FD:2B:BC:EE:BB:16:AF:BA:52:EC:6A:C9:24:19:D5
 * <p>
 * C3:83:83:56:68:FD:2B:BC:EE:BB:16:AF:BA:52:EC:6A:C9:24:19:D5;com.isoftstone.smartsite
 */

public class MainFragment extends BaseFragment {

    private TextView mCityTestView = null;
    private TextView mWeatherTextView = null;
    private TextView mTemperatureTextView = null;
    private TextView lab_main_unread_num = null;  //未查看消息数目
    private TextView lab_main_unHandlerTask_num = null;  //待处理任务数目
    private TextView lab_report_unread_num = null;  //未查看报告数目
    private TextView lab_vcr_unread_num = null;//视频监控设备数
    private TextView lab_air_unread_num = null;//环境监控数目
    private HttpPost mHttpPost = new HttpPost();
    private View mImageButton_1 = null;
    private View mImageButton_2 = null;
    private View mImageButton_3 = null;
    private View mImageButton_4 = null;
    private View mImageButton_5 = null;
    private View mImageButton_6 = null;
    private View mImageButton_7 = null;
    private View mImageButton_8 = null;

    private LinearLayout mVideoMonitoringMsg = null;    //视频监控设备
    private LinearLayout mAirMonitoringMsg = null;      //环境监控设备
    //private LinearLayout mUnCheckMsg = null;            //未查看消息点击区域
    private LinearLayout mUntreatedReport = null;       //待处理报告点击区域
    private LinearLayout mUnHandlerTask = null;         //待处理任务点击
    private ListView mListView = null;
    private ImageView wuran_image = null;
    private ImageView wuran_icon = null;
    private TextView wuran_text = null;
    private TextView wuran_number = null;
    private TextView shidu_textview = null;
    private TextView fengxiang_textview = null;

    public static final int HANDLER_GET_HOME_DATA_START = 1;
    public static final int HANDLER_GET_HOME_DATA_END = 2;
    private MobileHomeBean mMobileHomeBean = null;

    ArrayList<View> mViewsList = new ArrayList<>();

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_main;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        initView();
        permissionCheck();
    }

    private void initView() {
        mCityTestView = (TextView) rootView.findViewById(R.id.text_city);
        lab_main_unHandlerTask_num = (TextView) rootView.findViewById(R.id.lab_main_unhandlertask_num);  //未查看消息数目
        lab_main_unHandlerTask_num.setVisibility(View.INVISIBLE);
        //lab_main_unread_num = (TextView) rootView.findViewById(R.id.lab_main_unread_num);  //未查看消息数目
        //lab_main_unread_num.setVisibility(View.INVISIBLE);
        lab_report_unread_num = (TextView) rootView.findViewById(R.id.lab_report_unread_num);  //未查看报告数目
        lab_report_unread_num.setVisibility(View.INVISIBLE);
        lab_vcr_unread_num = (TextView) rootView.findViewById(R.id.lab_vcr_unread_num);//视频监控设备数
        lab_vcr_unread_num.setVisibility(View.INVISIBLE);
        lab_air_unread_num = (TextView) rootView.findViewById(R.id.lab_air_unread_num);//环境监控数目
        lab_air_unread_num.setVisibility(View.INVISIBLE);
        mTemperatureTextView = (TextView) rootView.findViewById(R.id.text_temperature);

        /*mUnCheckMsg = (LinearLayout) rootView.findViewById(R.id.textView10);
        mUnCheckMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterUnChekMsg();
            }
        });*/
        mUnHandlerTask = (LinearLayout) rootView.findViewById(R.id.textView9);
        mUnHandlerTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterPatrolMission(getActivity());
            }
        });
        mUntreatedReport = (LinearLayout) rootView.findViewById(R.id.textView11);
        mUntreatedReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterThirdPartReport(getActivity());
            }
        });
        mVideoMonitoringMsg = (LinearLayout) rootView.findViewById(R.id.textView12);
        mVideoMonitoringMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterVideoMonitoring(getActivity());
            }
        });
        mAirMonitoringMsg = (LinearLayout) rootView.findViewById(R.id.textView13);
        mAirMonitoringMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterAirMonitoringMsg(getActivity());
            }
        });

        mImageButton_1 = rootView.findViewById(R.id.button_1);
        mImageButton_1.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_1);
        mImageButton_2 = rootView.findViewById(R.id.button_2);
        mImageButton_2.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_2);
        mImageButton_3 = rootView.findViewById(R.id.button_3);
        mImageButton_3.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_3);
        mImageButton_4 = rootView.findViewById(R.id.button_4);
        mImageButton_4.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_4);
        mImageButton_5 = rootView.findViewById(R.id.button_5);
        mImageButton_5.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_5);
        mImageButton_6 = rootView.findViewById(R.id.button_6);
        mImageButton_6.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_6);
        mImageButton_7 = rootView.findViewById(R.id.button_7);
        mImageButton_7.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_7);
        mImageButton_8 = rootView.findViewById(R.id.button_8);
        mImageButton_8.setOnClickListener(viewOnClick);
        mViewsList.add(mImageButton_8);

        mListView = (ListView) rootView.findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked();
            }
        });
        wuran_image = (ImageView) rootView.findViewById(R.id.wuran_image);
        wuran_icon = (ImageView) rootView.findViewById(R.id.wuran_icon);
        wuran_text = (TextView) rootView.findViewById(R.id.wuran_text);
        wuran_number = (TextView) rootView.findViewById(R.id.wuran_number);
        shidu_textview = (TextView) rootView.findViewById(R.id.shidu_textview);
        fengxiang_textview = (TextView) rootView.findViewById(R.id.fengxiang_textview);
    }

    private void permissionCheck(){
        //配置权限
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isVS()){
            mVideoMonitoringMsg.setVisibility(View.VISIBLE);    //视频监控设备
        }else {
            mVideoMonitoringMsg.setVisibility(View.GONE);    //视频监控设备
        }
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isENVIRMENT_VIEW()){
            mAirMonitoringMsg.setVisibility(View.VISIBLE);      //环境监控设备
        }else{
            mAirMonitoringMsg.setVisibility(View.GONE);      //环境监控设备
        }
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isM_PATROL_ACCEPT()
                || HttpPost.mLoginBean.getmUserBean().getmPermission().isM_PATROL_REPORT()){
            mUntreatedReport.setVisibility(View.VISIBLE);       //待处理报告点击区域
        }else {
            mUntreatedReport.setVisibility(View.GONE);       //待处理报告点击区域
        }
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPT()){
            mUnHandlerTask.setVisibility(View.VISIBLE);      //待处理任务点击
        }else{
            mUnHandlerTask.setVisibility(View.GONE);      //待处理任务点击
        }
        //配置权限
        ArrayList<String> descriptionList = new ArrayList<String>();
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isVS()) {
            descriptionList.add("enterVideoMonitoring");
        }
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isENVIRMENT_VIEW()){
            descriptionList.add("enterAirMonitoring");
        }
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isM_PATROL_ACCEPT()
                || HttpPost.mLoginBean.getmUserBean().getmPermission().isM_PATROL_REPORT()) {
            descriptionList.add("enterThirdPartReport");
        }
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isMUCKCAR_MONITOR()) {
            descriptionList.add("enterDircar");
        }
        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPINFO()){
            descriptionList.add("enterPatrolSurvey");
        }

        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPP()
                ||HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPPA()){
            descriptionList.add("enterInspectPlan");
        }

        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPT()) {
            descriptionList.add("enterPatrolMission");
        }

        if(HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPM()){
            descriptionList.add("enterConstructionMonitor");
        }

        for (int i = 0 ; i < mViewsList.size() ; i ++){
            mViewsList.get(i).setVisibility(View.GONE);
        }
        for (int i = 0 ; i < descriptionList.size() ; i ++){
            String str = descriptionList.get(i);
            if(str.equals("enterVideoMonitoring")){
                mViewsList.get(i).setBackgroundResource(R.drawable.shipinjiankong);
                mViewsList.get(i).setContentDescription("enterVideoMonitoring");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
            if(str.equals("enterAirMonitoring")){
                mViewsList.get(i).setBackgroundResource(R.drawable.huanjingjiance);
                mViewsList.get(i).setContentDescription("enterAirMonitoring");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
            if(str.equals("enterThirdPartReport")){
                mViewsList.get(i).setBackgroundResource(R.drawable.sanfangxietong);
                mViewsList.get(i).setContentDescription("enterThirdPartReport");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
            if(str.equals("enterDircar")){
                mViewsList.get(i).setBackgroundResource(R.drawable.zhatuchejiankong);
                mViewsList.get(i).setContentDescription("enterDircar");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
            if(str.equals("enterPatrolSurvey")){
                mViewsList.get(i).setBackgroundResource(R.drawable.xunchagaikuang);
                mViewsList.get(i).setContentDescription("enterPatrolSurvey");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
            if(str.equals("enterInspectPlan")){
                mViewsList.get(i).setBackgroundResource(R.drawable.xunchajihua);
                mViewsList.get(i).setContentDescription("enterInspectPlan");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
            if(str.equals("enterPatrolMission")){
                mViewsList.get(i).setBackgroundResource(R.drawable.xuncharenwu);
                mViewsList.get(i).setContentDescription("enterPatrolMission");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
            if(str.equals("enterConstructionMonitor")){
                mViewsList.get(i).setBackgroundResource(R.drawable.xunchajiankong);
                mViewsList.get(i).setContentDescription("enterConstructionMonitor");
                mViewsList.get(i).setVisibility(View.VISIBLE);
            }
        }
    }

    View.OnClickListener viewOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getContentDescription().equals("enterVideoMonitoring")){
                enterVideoMonitoring(getActivity());
            }else  if(v.getContentDescription().equals("enterAirMonitoring")){
                enterAirMonitoring(getActivity());
            }else  if(v.getContentDescription().equals("enterThirdPartReport")){
                enterThirdPartReport(getActivity());
            }else  if(v.getContentDescription().equals("enterDircar")){
                enterDircar(getActivity());
            }else  if(v.getContentDescription().equals("enterPatrolSurvey")){
                enterPatrolSurvey(getActivity());
            }else  if(v.getContentDescription().equals("enterInspectPlan")){
                enterInspectPlan(getActivity());
            }else  if(v.getContentDescription().equals("enterPatrolMission")){
                enterPatrolMission(getActivity());
            }else  if(v.getContentDescription().equals("enterConstructionMonitor")){
                enterConstructionMonitor(getActivity());
            }

        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_GET_HOME_DATA_START: {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            mMobileHomeBean = mHttpPost.getMobileHomeData();
                            mHandler.sendEmptyMessage(HANDLER_GET_HOME_DATA_END);
                        }
                    };
                    thread.start();
                }
                break;
                case HANDLER_GET_HOME_DATA_END: {
                    setMobileHomeDataToView();
                }
                break;
            }
        }
    };

    private void setWeatherData() {
        //设置天气情况
        DataQueryBean dataQueryBean = mMobileHomeBean.getAvgEqis();
        if (dataQueryBean != null) {
            int AQI = mMobileHomeBean.getAQI();
            if (AQI < 50) {
                wuran_image.setBackgroundResource(R.drawable.wuran_you_jingdu);
                wuran_icon.setBackgroundResource(R.drawable.main_aqi);
                wuran_text.setText(R.string.great);
            } else if (AQI < 100 && AQI >= 50) {
                wuran_image.setBackgroundResource(R.drawable.wuran_liang_jingdu);
                wuran_icon.setBackgroundResource(R.drawable.main_aqi);
                wuran_text.setText(R.string.populattion_middle);
            } else if (AQI < 200 && AQI >= 100) {
                wuran_image.setBackgroundResource(R.drawable.wuran_qingdu_jingdu);
                wuran_icon.setBackgroundResource(R.drawable.main_aqi);
                wuran_text.setText(R.string.populattion_one);
            } else if (AQI < 300 && AQI >= 200) {
                wuran_image.setBackgroundResource(R.drawable.wuran_zhong1du_jingdu);
                wuran_icon.setBackgroundResource(R.drawable.main_aqi);
                wuran_text.setText(R.string.populattion_two);
            } else if (AQI >= 300) {
                wuran_image.setBackgroundResource(R.drawable.wuran_zhongdu_jingdu);
                wuran_icon.setBackgroundResource(R.drawable.main_aqi);
                wuran_text.setText(R.string.populattion_third);
            }
            wuran_number.setText("" + AQI);
            mTemperatureTextView.setText(dataQueryBean.getAirTemperature() + "");
            shidu_textview.setText(dataQueryBean.getAirHumidity() + "%");
            String wind = WindSpeed.getWindSpeed(dataQueryBean.getWindDirection(),dataQueryBean.getWindSpeed());
            fengxiang_textview.setText(wind);
        }
    }

    private void setMobileHomeDataToView() {
        mCityTestView.setText(R.string.city_name);
        if (mMobileHomeBean == null) {
            return;
        }
        /*
        int unreadMessage = mMobileHomeBean.getUnreadMessages();
        if (unreadMessage > 0) {
            lab_main_unread_num.setVisibility(View.VISIBLE);
            lab_main_unread_num.setText(unreadMessage + "");
        }
        */
        int unHandlerTask = mMobileHomeBean.getUnHandleTask();
        if (unHandlerTask > 0) {
            lab_main_unHandlerTask_num.setVisibility(View.VISIBLE);
            lab_main_unHandlerTask_num.setText(unHandlerTask + "");
        }
        int unreadPatrols = mMobileHomeBean.getUntreatedPatrols();
        if (unreadPatrols > 0) {
            lab_report_unread_num.setVisibility(View.VISIBLE);
            lab_report_unread_num.setText(unreadPatrols + "");
        }
        lab_vcr_unread_num.setText(mMobileHomeBean.getAllVses() + "");//视频监控设备数
        lab_vcr_unread_num.setVisibility(View.VISIBLE);
        lab_air_unread_num.setText(mMobileHomeBean.getAllEmes() + "");//环境监控数目
        lab_air_unread_num.setVisibility(View.VISIBLE);
        //设置天气参数
        setWeatherData();
        //设置即使消息
        InstantMessageAdapter adapter = new InstantMessageAdapter(getContext());
        adapter.setData(mMobileHomeBean.getMessages());
        mListView.setAdapter(adapter);
    }


    private void onItemClicked() {

    }

    private void enterUnChekMsg() {
        //进入未查看消息
        ((MainActivity) getActivity()).setCurrentTab(2);
    }

    private void enterUntreatedReport() {
        //进入未处理报告
        ((MainActivity) getActivity()).setCurrentTab(2);
    }

    private static void enterAirMonitoringMsg(Context context) {
        Intent intent = new Intent(context, PMDevicesListActivity.class);
        context.startActivity(intent);
    }

    public static void enterThirdPartReport(Context context) {
        //进入三方协同
        //yanyongjun 这个地方应该是进到三方协同界面，而不是到消息fragment页
        Intent intent = new Intent(context, TripartiteActivity.class);
        context.startActivity(intent);
    }

    public static void enterVideoMonitoring(Context context) {
        //进入视频监控

        if (!NetworkUtils.isConnected()) {
            ToastUtils.showShort(context.getText(R.string.network_can_not_be_used_toast).toString());
            return;
        }

        if (!HttpPost.mVideoIsLogin) {
            //Utils.LogginVideoTask logginVideoTask = new Utils.LogginVideoTask((BaseActivity)getActivity(), Utils.ENTER_VIDEO_DEVICE_LIST, null);
            //logginVideoTask.execute();
            Utils.showInitVideoServerDialog((BaseActivity) context, Utils.ENTER_VIDEO_DEVICE_LIST, null);
        } else {
            Intent intent = new Intent(context, VideoMonitoringActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    /**
     * 进入渣土车监控
     */
    public static void enterDircar(Context context) {
        Intent intent = new Intent(context, SlagcarInfoActivity.class);
        context.startActivity(intent);
    }

    /*
     进入巡查概况
     */
    private static void enterPatrolSurvey(Context context) {
        Intent intent = new Intent(context, ConstructionSummaryActivity.class);
        context.startActivity(intent);
    }

    private static void enterAirMonitoring(Context context) {
        //进入环境监控
        Intent intent = new Intent(context, AirMonitoringActivity.class);
        context.startActivity(intent);
    }

    private static void enterInspectPlan(Context context) {
        //进入巡查计划
        boolean isHasPermission = false;

        try {

            isHasPermission = HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPPA();

        } catch (Exception e) {

            isHasPermission = false;
            //Log.i(TAG, "access permission exception : " + e.getMessage());

        } finally {

            if (isHasPermission) {
                //有审批计划权限
                Intent intent = new Intent(context, ApprovalPendingInspectPlansActivity.class);
                context.startActivity(intent);
            } else {
                //没有审批计划权限
                Intent intent = new Intent(context, PatrolPlanActivity.class);
                context.startActivity(intent);
            }

        }
    }

    public  static void enterPatrolMission(Context context) {
        //进入巡查任务
        Intent intent = new Intent(context, PatroPlanDetailsActivity.class);
        context.startActivity(intent);
    }

    public  static void enterConstructionMonitor(Context context) {
        //进入施工监控
        Intent intent = new Intent(context, ConstructionMonitorMapActivity.class);
        context.startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(HANDLER_GET_HOME_DATA_START);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
