package com.isoftstone.smartsite.model.inspectplan.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.pageable.PageableBean;
import com.isoftstone.smartsite.http.patrolplan.PatrolPlanBean;
import com.isoftstone.smartsite.http.patrolplan.PatrolPlanCommitBean;
import com.isoftstone.smartsite.http.patroltask.PatrolTaskBean;
import com.isoftstone.smartsite.http.patroltask.PatrolTaskBeanPage;
import com.isoftstone.smartsite.http.patroluser.UserTrackBean;
import com.isoftstone.smartsite.http.user.SimpleUserBean;
import com.isoftstone.smartsite.model.inspectplan.adapter.PatrolPlanAdapter;
import com.isoftstone.smartsite.model.main.ui.AirMonitoringActivity;
import com.isoftstone.smartsite.model.map.ui.MapTaskDetailActivity;
import com.isoftstone.smartsite.utils.LogUtils;
import com.isoftstone.smartsite.widgets.CustomDatePicker;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gone on 2017/11/19.
 */

public class PatrolPlanActivity extends BaseActivity implements View.OnClickListener {

    private HttpPost mHttpPost = new HttpPost();
    private ImageButton mAddPatrolTask = null;   //新增巡查任务
    private LinearLayout mTitleLayout = null;
    private TextView mTitleTextView = null;
    private LinearLayout weeks = null;
    private LinearLayout days = null;
    private TextView day_0 = null;
    private TextView day_1 = null;
    private TextView day_2 = null;
    private TextView day_3 = null;
    private TextView day_4 = null;
    private TextView day_5 = null;
    private TextView day_6 = null;
    private LocalDate today = null;
    private final int HANDLER_GET_WEEK_START = 1;
    private final int HANDLER_GET_WEEK_END = 2;
    private final int HANDLER_GET_DAY_START = 3;
    private final int HANDLER_GET_DAY_END = 4;
    private final int HANDLER_BOHUI_START = 5;
    private final int HANDLER_BOHUI_END = 6;
    private final int HANDLER_TONGUO_START = 7;
    private final int HANDLER_TONGUO_END = 8;
    private final int HANDLER_TIJIAO_START = 9;
    private final int HANDLER_TIJIAO_END = 10;
    private PatrolTaskBeanPage patrolTaskBeanPage = null;
    private int selectindex = -1;
    private ArrayList<TextView> dayTextViewList = new ArrayList<TextView>();
    private ListView mListView = null;
    private ArrayList<PatrolTaskBean> mListData = null;
    private ArrayList<PatrolTaskBean> mListData_week = new ArrayList<PatrolTaskBean>();
    private int planState = -1;
    private PatrolPlanBean patrolPlanBean = null;
    private ImageView imageview_tuihui;   //退回按钮
    private ImageView imageview_tongguo;  //通过按钮
    private LinearLayout tuihuitonguo_layout;  //退回通过layout
    private LinearLayout tijiaoshenpi_layout;  //提交审批layout
    private ImageView imageview_tijiaoshenpi; //提交审批按钮
    private float touchX; //触摸点
    private String taskTimeStart; //开始时间
    private String taskTimeEnd;   //结束时间
    private long userId;   //用户id
    private CustomDatePicker customDatePicker;
    private PatrolPlanAdapter adapter = null;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_GET_WEEK_START: {
                    showDlg(getString(R.string.toast_information));
                    new Thread() {
                        @Override
                        public void run() {
                            String user_id = "";
                            if (userId >= 0) {
                                user_id = userId + "";
                            }
                            taskTimeStart = taskTimeStart + " 00:00";
                            taskTimeEnd = taskTimeEnd + " 23:59";
                            PageableBean pageableBean = new PageableBean();
                            mListData = mHttpPost.getPatrolTaskListAll(user_id, "", "", "0", "", taskTimeStart, taskTimeEnd, pageableBean);
                            mListData_week.clear();
                            if(mListData != null){
                                for (int i = 0 ;i < mListData.size(); i ++){
                                    PatrolTaskBean patrolTaskBean = mListData.get(i);
                                    mListData_week.add(patrolTaskBean);
                                }
                            }
                            mHandler.sendEmptyMessage(HANDLER_GET_WEEK_END);
                        }
                    }.start();
                    ;
                }
                break;
                case HANDLER_GET_WEEK_END: {
                    loadingData();
                    closeDlg();
                }
                break;
                case HANDLER_GET_DAY_START: {
                    if(mListData!=null){
                        mListData.clear();
                        for (int i = 0 ; i < mListData_week.size(); i ++){
                            PatrolTaskBean patrolTaskBean = mListData_week.get(i);
                            if(selectindex == -1){
                                mListData.add(patrolTaskBean);
                            }else{
                                String start = patrolTaskBean.getTaskTimeStart().substring(0,10);
                                String end = patrolTaskBean.getTaskTimeEnd().substring(0,10);
                                if(start.compareTo(taskTimeStart) <= 0 && end.compareTo(taskTimeEnd) >= 0){
                                    mListData.add(patrolTaskBean);
                                }
                            }
                        }
                        loadingData();
                    }
                }
                break;
                case HANDLER_GET_DAY_END: {
                    //loadingData();
                    //closeDlg();
                }
                break;
                case HANDLER_BOHUI_START: {
                    showDlg(getString(R.string.submiting));
                    new Thread() {
                        @Override
                        public void run() {
                            mHttpPost.planRefuse(patrolPlanBean);
                            //begin：审批成功需调用此方法 通知审批列表界面刷新数据状态
                            Intent i = new Intent();
                            setResult(Activity.RESULT_OK, i);
                            //end：审批成功需调用此方法 通知审批列表界面刷新数据状态
                            mHandler.sendEmptyMessage(HANDLER_BOHUI_END);
                        }
                    }.start();
                }
                break;
                case HANDLER_BOHUI_END: {
                    selectindex = -1;
                    updateWidget();
                    mHandler.sendEmptyMessage(HANDLER_GET_WEEK_START);
                    closeDlg();
                }
                break;

                case HANDLER_TONGUO_START: {
                    showDlg(getString(R.string.submiting));
                    new Thread() {
                        @Override
                        public void run() {
                            mHttpPost.planThrough(patrolPlanBean);
                            //begin：审批成功需调用此方法 通知审批列表界面刷新数据状态
                            Intent i = new Intent();
                            setResult(Activity.RESULT_OK, i);
                            //end：审批成功需调用此方法 通知审批列表界面刷新数据状态
                            mHandler.sendEmptyMessage(HANDLER_TONGUO_END);
                        }
                    }.start();
                }
                break;
                case HANDLER_TONGUO_END: {
                    selectindex = -1;
                    updateWidget();
                    mHandler.sendEmptyMessage(HANDLER_GET_WEEK_START);
                    closeDlg();
                }
                break;

                case HANDLER_TIJIAO_START: {
                    showDlg(getString(R.string.submiting));
                    new Thread() {
                        @Override
                        public void run() {
                            PatrolPlanCommitBean patrolPlanCommitBean = new PatrolPlanCommitBean();
                            patrolPlanCommitBean.setTaskTimeStart(today.toString() + " 00:00");
                            patrolPlanCommitBean.setTaskTimeEnd(today.plusDays(6).toString() + " 23:59");
                            SimpleUserBean simpleUserBean = new SimpleUserBean();
                            simpleUserBean.setId(userId);
                            patrolPlanCommitBean.setCreator(simpleUserBean);
                            mHttpPost.patrolPlanCommit(patrolPlanCommitBean);
                            mHandler.sendEmptyMessage(HANDLER_TIJIAO_END);
                        }
                    }.start();
                }
                break;
                case HANDLER_TIJIAO_END: {
                    selectindex = -1;
                    Toast.makeText(PatrolPlanActivity.this,R.string.submit_success,Toast.LENGTH_SHORT).show();
                    updateWidget();
                    mHandler.sendEmptyMessage(HANDLER_GET_WEEK_START);
                    closeDlg();
                }
                break;
            }
        }
    };

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_patrolplan;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        //获取巡查计划ID
        patrolPlanBean = (PatrolPlanBean) getIntent().getSerializableExtra("patrolplan");
        if (patrolPlanBean != null) {
            userId = patrolPlanBean.getCreator().getId();   //用户id
        } else {
            userId = HttpPost.mLoginBean.getmUserBean().getLoginUser().getId();   //用户id
        }
        initToolbar();
        //显示星期
        weeks = (LinearLayout) findViewById(R.id.weeks);
        String[] strweeks = getResources().getStringArray(R.array.weeks);
        for (int i = 0; i < 7; i++) {
            TextView textView = (TextView) weeks.getChildAt(i);
            textView.setText(strweeks[i]);
            textView.setTextColor(getResources().getColor(R.color.single_text_color));
        }
        //显示日期
        days = (LinearLayout) findViewById(R.id.days);
        day_0 = (TextView) findViewById(R.id.day_0);
        day_0.setOnClickListener(this);
        day_0.setOnTouchListener(onTouchListener);
        day_1 = (TextView) findViewById(R.id.day_1);
        day_1.setOnClickListener(this);
        day_1.setOnTouchListener(onTouchListener);
        day_2 = (TextView) findViewById(R.id.day_2);
        day_2.setOnClickListener(this);
        day_2.setOnTouchListener(onTouchListener);
        day_3 = (TextView) findViewById(R.id.day_3);
        day_3.setOnClickListener(this);
        day_3.setOnTouchListener(onTouchListener);
        day_4 = (TextView) findViewById(R.id.day_4);
        day_4.setOnClickListener(this);
        day_4.setOnTouchListener(onTouchListener);
        day_5 = (TextView) findViewById(R.id.day_5);
        day_5.setOnClickListener(this);
        day_5.setOnTouchListener(onTouchListener);
        day_6 = (TextView) findViewById(R.id.day_6);
        day_6.setOnClickListener(this);
        day_6.setOnTouchListener(onTouchListener);
        dayTextViewList.add(day_0);
        dayTextViewList.add(day_1);
        dayTextViewList.add(day_2);
        dayTextViewList.add(day_3);
        dayTextViewList.add(day_4);
        dayTextViewList.add(day_5);
        dayTextViewList.add(day_6);

        days.setOnTouchListener(onTouchListener);

        mListView = (ListView) findViewById(R.id.listview);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PatrolTaskBean patrolTaskBean = mListData.get(position);
                if (patrolTaskBean==null){
                    return;
                }
                if (patrolTaskBean.getPlanStatus()==1 || patrolTaskBean.getPlanStatus()== 4){
                    Long creatUser=patrolTaskBean.getCreator().getId();
                    userId = HttpPost.mLoginBean.getmUserBean().getLoginUser().getId();
                    if (creatUser.equals(userId)){
//                        修改巡查任务
                        Intent intent = new Intent(PatrolPlanActivity.this, AddInspectPlan.class);
                        intent.putExtra("taskTimeStart",patrolTaskBean.getTaskTimeStart());
                        intent.putExtra("taskTimeEnd",patrolTaskBean.getTaskTimeEnd());
                        intent.putExtra("patrolTaskBean",patrolTaskBean);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }else {
                        Toast.makeText(PatrolPlanActivity.this,R.string.information,Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    enterPatrolTask(patrolTaskBean);
                }
            }
        });
        imageview_tuihui = (ImageView) findViewById(R.id.imageview_tuihui);   //退回按钮
        imageview_tuihui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //审批拒绝
                mHandler.sendEmptyMessage(HANDLER_BOHUI_START);

            }
        });
        imageview_tongguo = (ImageView) findViewById(R.id.imageview_tongguo);  //通过按钮
        imageview_tongguo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //审批通过
                mHandler.sendEmptyMessage(HANDLER_TONGUO_START);
            }
        });
        tuihuitonguo_layout = (LinearLayout) findViewById(R.id.tuihuitonguo_layout);  //退回通过layout
        tijiaoshenpi_layout = (LinearLayout) findViewById(R.id.tijiaoshenpi_layout);  //提交审批
        imageview_tijiaoshenpi = (ImageView) findViewById(R.id.imageview_tijiaoshenpi);
        imageview_tijiaoshenpi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //提交审批
                mHandler.sendEmptyMessage(HANDLER_TIJIAO_START);
            }
        });

        if (patrolPlanBean != null) {
            taskTimeStart = patrolPlanBean.getStart(); //开始时间
            taskTimeEnd = patrolPlanBean.getEndDate();   //结束时间
            today = LocalDate.parse(taskTimeStart);
        } else {
            LocalDate localDate = LocalDate.now();
            int minus = localDate.getDayOfWeek();
            today = localDate.minusDays(minus - 1);
            taskTimeStart = today.toString(); //开始时间
            taskTimeEnd = today.plusDays(6).toString();   //结束时间
        }


        updateWidget();
        customDatePicker = new CustomDatePicker(PatrolPlanActivity.this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
                LocalDate localDate = new LocalDate(time.substring(0, 7) + "-01");
                int minus = localDate.getDayOfWeek();
                today = localDate.minusDays(minus - 1);
                selectindex = -1;
                updateWidget();
                taskTimeStart = today.toString(); //开始时间
                taskTimeEnd = today.plusDays(6).toString();   //结束时间
                mHandler.sendEmptyMessage(HANDLER_GET_WEEK_START);
            }
        }, "2010-01-01 00:00", "2037-01-01 00:00"); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        customDatePicker.showYearMonth();
        customDatePicker.setIsLoop(false); // 不允许循环滚动

    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    touchX = event.getRawX();
                    Log.e("test","setOnTouchListener   down "+touchX);
                    if(v.getId() == R.id.day_0 ||v.getId() == R.id.day_1 ||v.getId() == R.id.day_2 ||
                            v.getId() == R.id.day_3 ||v.getId() == R.id.day_4 ||v.getId() == R.id.day_5 ||v.getId() == R.id.day_6){
                        return  false;
                    }else {
                        return  true;
                    }
                }
                case MotionEvent.ACTION_UP: {
                    float X = event.getRawX();
                    Log.e("test","setOnTouchListener   up  "+X);
                    if (touchX - X > 40) {
                        //下一周
                        today = today.plusDays(7);
                        selectindex = -1;
                        updateWidget();
                        taskTimeStart = today.toString(); //开始时间
                        taskTimeEnd = today.plusDays(6).toString();   //结束时间
                        mHandler.sendEmptyMessage(HANDLER_GET_WEEK_START);
                    }

                    if (X - touchX > 40) {
                        //上一周
                        today = today.minusDays(7);
                        selectindex = -1;
                        updateWidget();
                        taskTimeStart = today.toString(); //开始时间
                        taskTimeEnd = today.plusDays(6).toString();   //结束时间
                        mHandler.sendEmptyMessage(HANDLER_GET_WEEK_START);
                    }
                }
                break;
            }
            return v.onTouchEvent(event);
        }
    };
    private void initToolbar() {
        mTitleTextView = (TextView) findViewById(R.id.toolbar_title);
        mTitleLayout = (LinearLayout) findViewById(R.id.toolbar_title_layout);
        mTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());
                customDatePicker.show(now);
            }
        });
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mAddPatrolTask = (ImageButton) findViewById(R.id.btn_icon);
        mAddPatrolTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新增巡查任务
                Intent intent = new Intent(PatrolPlanActivity.this, AddInspectPlan.class);
                intent.putExtra("taskTimeStart",taskTimeStart);
                intent.putExtra("taskTimeEnd",taskTimeEnd);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        if (HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPPA()) {
            mAddPatrolTask.setVisibility(View.INVISIBLE);
        } else {
            mAddPatrolTask.setVisibility(View.VISIBLE);
        }
    }

    /*
    加载数据
     */
    private void loadingData() {
        //根据计划状态来加载背景  是否显示审批按钮
        //设置计划状态
        int state = 0;
        if(mListData_week.size() > 0){
            state = mListData_week.get(0).getPlanStatus();
        }
        if (mListData != null && mListData.size() > 0) {
            adapter = new PatrolPlanAdapter(this);
            adapter.setList(mListData);
            mListView.setAdapter(adapter);
        } else {
            adapter = new PatrolPlanAdapter(this);
            mListView.setAdapter(adapter);
        }

        switch (state) {
            case 1://已创建，待提交
                days.setBackgroundResource(R.drawable.jihua_weitijiao_bg);
                break;
            case 2://已提交，待审批
                days.setBackgroundResource(R.drawable.jihua_daishenpishenpi_bg);
                break;
            case 3://已通过
                days.setBackgroundResource(R.drawable.jihua_shenpitongguo_bg);
                break;
            case 4://已打回
                days.setBackgroundResource(R.drawable.jihua_shenpituihui_bg);
                break;
            default:
                days.setBackgroundResource(R.drawable.jihua_weitijiao_bg);
        }

        //审批权限
        if (HttpPost.mLoginBean.getmUserBean().getmPermission().isM_CPPA()) {
            if (state == 2) {
                tuihuitonguo_layout.setVisibility(View.VISIBLE);
                tijiaoshenpi_layout.setVisibility(View.GONE);
            } else {
                tuihuitonguo_layout.setVisibility(View.GONE);
                tijiaoshenpi_layout.setVisibility(View.GONE);
            }
        } else {
            if (state < 2) {
                tuihuitonguo_layout.setVisibility(View.GONE);
                tijiaoshenpi_layout.setVisibility(View.VISIBLE);
            } else {
                tuihuitonguo_layout.setVisibility(View.GONE);
                tijiaoshenpi_layout.setVisibility(View.GONE);
            }
        }

        for (TextView textView : dayTextViewList) {
            if (state >= 2 && state <= 4) {
                textView.setTextColor(getResources().getColor(R.color.white));
            } else {
                textView.setTextColor(getResources().getColor(R.color.black));
            }
        }
        if (selectindex != -1) {
            dayTextViewList.get(selectindex).setTextColor(getResources().getColor(R.color.mainColor));
        }

        updateWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectindex = -1;
        taskTimeStart = today.plusDays(0).toString(); //开始时间
        taskTimeEnd = today.plusDays(6).toString();   //结束时间
        mHandler.sendEmptyMessage(HANDLER_GET_WEEK_START);
    }

    @Override
    public void onClick(View v) {
        int select = -1;
        switch (v.getId()) {
            case R.id.day_0: {
                select = 0;
            }
            break;
            case R.id.day_1: {
                select = 1;
            }
            break;
            case R.id.day_2: {
                select = 2;
            }
            break;
            case R.id.day_3: {
                select = 3;
            }
            break;
            case R.id.day_4: {
                select = 4;
            }
            break;
            case R.id.day_5: {
                select = 5;
            }
            break;
            case R.id.day_6: {
                select = 6;
            }
            break;
        }

        if (select == selectindex) {
            selectindex = -1;
            taskTimeStart = today.plusDays(0).toString(); //开始时间
            taskTimeEnd = today.plusDays(6).toString();   //结束时间
        } else {
            selectindex = select;
            taskTimeStart = today.plusDays(selectindex).toString(); //开始时间
            taskTimeEnd = today.plusDays(selectindex).toString();   //结束时间
        }
        mHandler.sendEmptyMessage(HANDLER_GET_DAY_START);
        updateWidget();
    }

    private void updateWidget() {

        //修改按钮  和文字颜色
        for (TextView textView : dayTextViewList) {
            textView.setBackgroundResource(R.drawable.rili_tian_zhengchang);
        }
        if (selectindex != -1) {
            dayTextViewList.get(selectindex).setBackgroundResource(R.drawable.rili_tian_xuanzhong);
        }
        //获取时间
        day_0.setText(today.getDayOfMonth() + "");
        day_1.setText(today.plusDays(1).getDayOfMonth() + "");
        day_2.setText(today.plusDays(2).getDayOfMonth() + "");
        day_3.setText(today.plusDays(3).getDayOfMonth() + "");
        day_4.setText(today.plusDays(4).getDayOfMonth() + "");
        day_5.setText(today.plusDays(5).getDayOfMonth() + "");
        day_6.setText(today.plusDays(6).getDayOfMonth() + "");
        //设置标题时间
        mTitleTextView.setText(today.plusDays(6).toString().substring(0, 7));
    }

    private void enterPatrolTask(PatrolTaskBean patrolTaskBean) {
        Intent intent = new Intent(this, MapTaskDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        UserTrackBean userTrackBean = new UserTrackBean();
        userTrackBean.setTaskId(patrolTaskBean.getTaskId());
        userTrackBean.setPatrolTask(patrolTaskBean);
        userTrackBean.setUser(patrolTaskBean.getCreator());
        //userTrackBean.setUserId(patrolTaskBean.getCreator().getId());
        intent.putExtra("data", userTrackBean);
        this.startActivity(intent);
    }
}
