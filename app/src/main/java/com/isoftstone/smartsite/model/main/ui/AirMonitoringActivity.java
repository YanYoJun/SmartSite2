package com.isoftstone.smartsite.model.main.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.aqi.EQIRankingBean;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.aqi.MonthlyComparisonBean;
import com.isoftstone.smartsite.http.aqi.WeatherConditionBean;
import com.isoftstone.smartsite.model.main.adapter.AirMonitoringRankAdapter;
import com.isoftstone.smartsite.model.tripartite.view.MyListView;
import com.isoftstone.smartsite.widgets.CustomDatePicker;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gone on 2017/10/17.
 * modifed by zhangyinfu on 2017/10/19
 */

public class AirMonitoringActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private HttpPost mHttpPost = new HttpPost();
    private String getEqiDataRankingTime;    //
    private String geteqiDataRankingarchid = "1";
    private String getWeatherDaysArchId;  //查询优良天气传参数
    private String getWeatherDaysTime;    //查询优良时间传参数
    private String getComparisonarchid;
    private String getComparisontime ;
    private EQIRankingBean mEQIRankingBean = null;
    private ArrayList<WeatherConditionBean> mWeatherList = null;
    private PieChart mPieChart = null;
    private LineChart mLineChart = null;
    private LineChart mLineChart_quyu = null;
    private ImageView mImageView_back = null;
    private ImageView mImageView_devices = null;
    private TextView mRankTime = null;
    private TextView mYouliangTime = null;
    private TextView mTongqiTime = null;
    private Spinner mRankSpinner = null;
    private Spinner mTongqiSpinner = null;
    private Spinner mYouliangSpinner = null;

    public static  final  int HANDLER_GET_RANKING_START = 1;
    public static  final  int HANDLER_GET_RANKING_END = 2;
    public static  final  int HANDLER_GET_DAYS_PROPORTION_START = 3;//
    public static  final  int HANDLER_GET_DAYS_PROPORTION_END = 4;//
    public static  final  int HANDLER_GET_COMPARISON_START = 5;
    public static  final  int HANDLER_GET_COMPARISON_END = 6;
    public static  final  int HANDLER_GET_QUYUDUIBI_START = 7;
    public static  final  int HANDLER_GET_QUYUDUIBI_END = 8;
    private static final String[] m={"AQI","PM2.5","CO2","PM10"};
    private String address[];
    private MonthlyComparisonBean mMonthlyComparisonBean = null;
    private MyListView myListView = null;
    private TextView toolbar_title = null;

    private  TextView text_pushtime = null;
    private  TextView text_before = null;
    private  TextView text_pushtime_0 = null;
    private  TextView text_before_0 = null;


    private TextView quyu_name = null;
    private TextView quyu_date = null;
    private MonthlyComparisonBean mMonthlyComparisonBean_1;
    private MonthlyComparisonBean mMonthlyComparisonBean_2;
    private boolean[] addressFlags = new boolean[100];
    private View oneIconLayout = null;
    private View searchLayout = null;
    private String quyu_id_1;
    private String quyu_id_2;
    private String quyutime;
    private TextView text_quyu_1;
    private TextView text_quyu_2;
    private ListView checkBoxListView;
    private MyCheckboxAdapter checkboxAdapter;
    private PopupWindow mCheckBoxPopWindow;
    private ImageButton imageButton;
    private TextView mSearch_cancel=null;
    private ImageButton mSearch_back=null;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_airmonitoring;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        init();
        setOnCliceked();
        initDatePicker();
        mHandler.sendEmptyMessage(HANDLER_GET_RANKING_START);
    }

    private void init(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        imageButton = (ImageButton) findViewById(R.id.btn_search);
        imageButton.setVisibility(View.VISIBLE);
        imageButton.setOnClickListener(this);
        oneIconLayout = (View)findViewById(R.id.one_icon);
        searchLayout = (View)findViewById(R.id.serch);
        mSearch_back = (ImageButton)findViewById(R.id.search_btn_back);
        mSearch_cancel = (TextView)findViewById(R.id.search_btn_icon_right);
        mSearch_back.setOnClickListener(this);
        mSearch_cancel.setOnClickListener(this);
        mRankTime = (TextView)findViewById(R.id.date);
        mRankTime.setOnClickListener(this);
        mRankTime.setText(df.format(new Date()));
        mRankSpinner = (Spinner)findViewById(R.id.name);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.spinner_item,m);
        mRankSpinner.setAdapter(adapter);
        mRankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setHorizontalBarChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mYouliangTime = (TextView)findViewById(R.id.youliang_datedate);
        mYouliangTime.setText(df.format(new Date()));
        mYouliangTime.setOnClickListener(this);
        mYouliangSpinner = (Spinner)findViewById(R.id.youliang_name);
        mYouliangSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getWeatherDaysArchId = mEQIRankingBean.getAQI().get(position).getArchId();
                mHandler.sendEmptyMessage(HANDLER_GET_DAYS_PROPORTION_START);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mTongqiTime = (TextView)findViewById(R.id.tongqi_date);
        mTongqiTime.setText(df.format(new Date()));
        mTongqiTime.setOnClickListener(this);
        mTongqiSpinner = (Spinner)findViewById(R.id.tongqi_name);
        mTongqiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getComparisonarchid = mEQIRankingBean.getAQI().get(position).getArchId();
                mHandler.sendEmptyMessage(HANDLER_GET_COMPARISON_START);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mImageView_back = (ImageView)findViewById(R.id.btn_back);
        mImageView_devices = (ImageView)findViewById(R.id.btn_icon);
        mPieChart = (PieChart)findViewById(R.id.chart2);
        mLineChart = (LineChart)findViewById(R.id.chart3);
        myListView = (MyListView) findViewById(R.id.lv);
        mImageView_devices.setImageResource(R.drawable.environmentlist);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText("环境监控");

        text_before = (TextView) findViewById(R.id.text_before);
        text_pushtime = (TextView) findViewById(R.id.text_pushtime);
        text_before_0 = (TextView) findViewById(R.id.text_before_0);
        text_pushtime_0 = (TextView) findViewById(R.id.text_pushtime_0);


        mLineChart_quyu = (LineChart) findViewById(R.id.chart4);
        quyu_name = (TextView)findViewById(R.id.quyu_name);
        quyu_name.setOnClickListener(this);
        quyu_date = (TextView)findViewById(R.id.quyu_date);
        quyu_date.setText(df.format(new Date()));
        quyu_date.setOnClickListener(this);
        text_quyu_1 = (TextView) findViewById(R.id.text_quyu_1);
        text_quyu_2 = (TextView) findViewById(R.id.text_quyu_2);

    }
    private void setOnCliceked(){
        mImageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mImageView_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AirMonitoringActivity.this,PMDevicesListActivity.class);
                AirMonitoringActivity.this.startActivity(intent);
            }
        });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_GET_RANKING_START:{
                    showDlg(getString(R.string.toast_information));
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            getEqiDataRankingTime = mRankTime.getText().toString();
                            Log.i("test",getEqiDataRankingTime+" "+geteqiDataRankingarchid);
                            mEQIRankingBean = mHttpPost.eqiDataRanking(geteqiDataRankingarchid,getEqiDataRankingTime);
                            if(mEQIRankingBean != null){
                                ArrayList<EQIRankingBean.AQI>  list = mEQIRankingBean.getAQI();
                                Collections.sort(list, new AirMonitoringRankAdapter.SortByValue());
                                mEQIRankingBean.setAQI(list);
                            }
                            mHandler.sendEmptyMessage(HANDLER_GET_RANKING_END);
                        }
                    };
                    thread.start();
                }
                break;
                case HANDLER_GET_RANKING_END:{
                    setHorizontalBarChart();
                    setSpinnerData();
                    mHandler.sendEmptyMessage(HANDLER_GET_QUYUDUIBI_START);
                    closeDlg();
                }
                break;
                case HANDLER_GET_DAYS_PROPORTION_START:{
                    showDlg(getString(R.string.toast_information));
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            getWeatherDaysTime = mYouliangTime.getText().toString();
                            mWeatherList = mHttpPost.getWeatherConditionDay(getWeatherDaysArchId,getWeatherDaysTime);
                            mHandler.sendEmptyMessage(HANDLER_GET_DAYS_PROPORTION_END);
                        }
                    };
                    thread.start();
                }
                break;
                case HANDLER_GET_DAYS_PROPORTION_END:{
                    setPieChart();
                    closeDlg();
                }
                break;
                case HANDLER_GET_COMPARISON_START:{
                    showDlg(getString(R.string.toast_information));
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            getComparisontime = mTongqiTime.getText().toString();
                            mMonthlyComparisonBean = mHttpPost.carchMonthlyComparison(getComparisonarchid,getComparisontime,1+"");
                            mHandler.sendEmptyMessage(HANDLER_GET_COMPARISON_END);
                        }
                    };
                    thread.start();
                }
                break;
                case HANDLER_GET_COMPARISON_END:{
                    setLineChart();
                    setLineChart_quyu();
                    closeDlg();
                }
                break;
                case HANDLER_GET_QUYUDUIBI_START:{
                    showDlg(getString(R.string.toast_information));
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            quyutime = quyu_date.getText().toString();
                            mMonthlyComparisonBean_1 = mHttpPost.carchMonthlyComparison(quyu_id_1+"",quyutime,0+"");
                            mMonthlyComparisonBean_2 = mHttpPost.carchMonthlyComparison(quyu_id_2+"",quyutime,0+"");
                            mHandler.sendEmptyMessage(HANDLER_GET_QUYUDUIBI_END);
                        }
                    };
                    thread.start();
                }
                break;
                case HANDLER_GET_QUYUDUIBI_END:{
                    setLineChart_quyu();
                    closeDlg();
                }
                break;
            }
        }
    };

    private void setSpinnerData(){
        if(mEQIRankingBean == null){
            return;
        }
        address = new String[mEQIRankingBean.getAQI().size()];
        for (int i = 0; i < mEQIRankingBean.getAQI().size() ;i ++){
            address[i] = mEQIRankingBean.getAQI().get(i).getArchName();
            if(i == 0){
                quyu_id_1 = mEQIRankingBean.getAQI().get(i).getArchId();
                addressFlags[0] = true;
            }

            if(i == 1){
                quyu_id_2 = mEQIRankingBean.getAQI().get(i).getArchId();
                addressFlags[1] = true;
            }
        }
        ArrayAdapter adapter_youliang = new ArrayAdapter<String>(this,R.layout.spinner_item,address);
        mYouliangSpinner.setAdapter(adapter_youliang);
        mYouliangSpinner.setSelection(0);
        ArrayAdapter adapter_tongqi = new ArrayAdapter<String>(this,R.layout.spinner_item,address);
        mTongqiSpinner.setAdapter(adapter_tongqi);
        mTongqiSpinner.setSelection(0);

    }

    private void setHorizontalBarChart(){
        if(mEQIRankingBean == null){
            return;
        }
        int index = mRankSpinner.getSelectedItemPosition();
        AirMonitoringRankAdapter adapter = new AirMonitoringRankAdapter(this);
        switch (index){
            case  1:
                adapter.setList(mEQIRankingBean.getPM2_5());
                break;
            case  2:
                adapter.setList(mEQIRankingBean.getCO2());
                break;
            case  3:
                adapter.setList(mEQIRankingBean.getPM10());
                break;
            case  0:
                adapter.setList(mEQIRankingBean.getAQI());
                break;
        }
        myListView.setAdapter(adapter);
    }

    private void setPieChart(){
        if(mWeatherList == null){
            return;
        }
        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(40, 10, 30, 10);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);

        //mPieChart.setCenterTextTypeface(mTfLight);
        mPieChart.setCenterText("");

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(65f);
        mPieChart.setTransparentCircleRadius(65f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mPieChart.setRotationEnabled(false);  //转动
        mPieChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        //mPieChart.setOnChartValueSelectedListener(this);



        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // mChart.spin(2000, 0, 360);


        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTextSize(12);//文字颜色
        l.setFormSize(12);
        l.setFormToTextSpace(6); //图标和文字距离
        l.setXEntrySpace(20f);  //左右间隔
        l.setYEntrySpace(0f); //
        l.setXOffset(10f);  //距离左边距离
        l.setYOffset(5f); //距离底部距离

        // entry label styling
        mPieChart.setEntryLabelColor(Color.WHITE);//分区内部文字颜色
        mPieChart.setEntryLabelTextSize(12f);//分区内部文字大小
        mPieChart.setDrawEntryLabels(false); //分区不显示文字

        mPieChart.setClickable(false);
        mPieChart.setEnabled(false);


        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (int i = 0; i < mWeatherList.size(); i ++) {
            int value = mWeatherList.get(i).getValue();
            if(value > 0){
                PieEntry entry = new PieEntry(value);
                entry.setLabel(mWeatherList.get(i).getName());
                entry.setData(value+"天");
                entries.add(entry);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(3f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int i = 0; i < mWeatherList.size() ;i ++){
            String name = mWeatherList.get(i).getName();
            if(mWeatherList.get(i).getValue() > 0){
                if(name.equals(R.string.great)){
                    colors.add((Integer)getResources().getColor(R.color.huanjin_you));
                }else if(name.equals(R.string.populattion_middle)){
                    colors.add((Integer)getResources().getColor(R.color.huanjin_liang));
                }else if(name.equals(R.string.populattion_one)){
                    colors.add((Integer)getResources().getColor(R.color.huanjin_qingdu));
                }
                else if(name.equals(R.string.populattion_two)){
                    colors.add((Integer)getResources().getColor(R.color.huanjin_zhong1du));
                }
                else if(name.equals(R.string.populattion_third)){
                    colors.add((Integer)getResources().getColor(R.color.huanjin_zhongdu));
                }
                else if(name.equals(R.string.populattion_four)){
                    colors.add((Integer)getResources().getColor(R.color.huanjin_yanzhong));
                }
            }
        }
        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.6f);
        dataSet.setValueTextColor(Color.RED);
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return entry.getData().toString();
            }
        });
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        //data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);
        //data.setValueTypeface(mTfLight);
        mPieChart.setData(data);

        // undo all highlights
        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }

    private void setLineChart(){
        float max = 0;
        String time = mTongqiTime.getText().toString();
        text_pushtime.setText(time);
        LocalDate date = new LocalDate();
        date.toString(time);
        String befortime = date.plusYears(-1).toString().substring(0,7);
        text_before.setText(befortime);

        if(mMonthlyComparisonBean == null){
            return;
        }

        mLineChart.setDrawGridBackground(false);

        // no description text
        mLineChart.getDescription().setEnabled(false);

        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        // enable scaling and dragging
        mLineChart.setDragEnabled(false);  //是否可以缩放
        mLineChart.setScaleEnabled(false);  //是否可以缩放
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);
        mLineChart.setExtraOffsets(10,0,10,20);



        XAxis xAxis = mLineChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setAxisMaximum(31);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.enableGridDashedLine(10f, 0f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(false);
        leftAxis.setAxisMinimum(0f);


        mLineChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mLineChart.animateX(2500);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mLineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setEnabled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        if(mMonthlyComparisonBean != null){
            int beforeMonthSize = mMonthlyComparisonBean.getBeforeMonth().size();
            if(beforeMonthSize > 0){
                ArrayList<Entry> values = new ArrayList<Entry>();
                for (int i = 0; i < beforeMonthSize; i++) {
                    String value = mMonthlyComparisonBean.getBeforeMonth().get(i).getAqi();
                    float v = Float.parseFloat(value);
                    if(v > max){
                        max = v;
                    }
                    Entry entry = new Entry(i+1,v);
                    values.add(entry);
                }

                LineDataSet set1 = new LineDataSet(values, "DataSet 1");
                set1.setDrawIcons(false);
                // set the line to be drawn like this "- - - - - -"
                //set1.enableDashedLine(10f, 10f, 0f);
                set1.setColor(Color.parseColor("#ff9e5d"));
                set1.setCircleColor(Color.parseColor("#ff9e5d"));
                set1.setLineWidth(1f);
                set1.setCircleRadius(4f);//设置焦点圆心的大小
                set1.setDrawCircleHole(true);
                set1.setCircleHoleRadius(2);
                set1.setCircleColorHole(Color.WHITE);
                set1.setValueTextSize(9f);
                set1.setDrawFilled(false);
                set1.setFormLineWidth(1f);
                set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 10f}, 0f));
                set1.setFormSize(15.f);
                set1.setDrawFilled(false);
                set1.setHighlightEnabled(false);
                set1.setDrawValues(false);

                if (Utils.getSDKInt() >= 18) {
                    // fill drawable only supported on api level 18 and above
                    //Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                    //set1.setFillDrawable(drawable);
                }
                else {
                    set1.setFillColor(Color.BLACK);
                }
                dataSets.add(set1); // add the datasets
            }
        }

        if(mMonthlyComparisonBean != null){
            int pushtimeMonthSize = mMonthlyComparisonBean.getCurrentMonth().size();
            if(pushtimeMonthSize > 0){
                ArrayList<Entry> values_2 = new ArrayList<Entry>();

                for (int i = 0; i < pushtimeMonthSize; i++) {
                    String value = mMonthlyComparisonBean.getCurrentMonth().get(i).getAqi();
                    float v = Float.parseFloat(value);
                    if(v > max){
                        max = v;
                    }
                    Entry entry = new Entry(i+1,v);
                    values_2.add(entry);
                }


                LineDataSet set2 = new LineDataSet(values_2, "DataSet 2");
                set2.setDrawIcons(false);
                // set the line to be drawn like this "- - - - - -"
                set2.enableDashedLine(10f, 0f, 0f);//设置连线样式
                set2.setColor(Color.parseColor("#599fff"));
                set2.setDrawCircleHole(false);
                set2.setFormLineWidth(1f);
                set2.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 10f}, 0f));
                set2.setFormSize(15.f);
                set2.setLineWidth(1f);//设置线宽
                set2.setCircleColor(Color.parseColor("#599fff"));
                set2.setCircleRadius(4f);//设置焦点圆心的大小
                set2.setDrawCircleHole(true);
                set2.setCircleHoleRadius(2);
                set2.setCircleColorHole(Color.WHITE);
                set2.enableDashedHighlightLine(10f, 5f, 0f);//点击后的高亮线的显示样式
                set2.setHighlightLineWidth(2f);//设置点击交点后显示高亮线宽
                set2.setHighlightEnabled(false);//是否禁用点击高亮线
                set2.setHighLightColor(Color.RED);//设置点击交点后显示交高亮线的颜色
                set2.setValueTextSize(9f);//设置显示值的文字大小
                set2.setDrawFilled(false);//设置禁用范围背景填充
                set2.setDrawValues(false);

                if (Utils.getSDKInt() >= 18) {
                    // fill drawable only supported on api level 18 and above
                    //Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                    //set1.setFillDrawable(drawable);
                }
                else {
                    set2.setFillColor(Color.BLACK);
                }

                dataSets.add(set2); // add the datasets
            }
        }

        // create a data object with the datasets
        LineData data = new LineData(dataSets);
        leftAxis.setAxisMaximum(max);
        // set data
        mLineChart.setData(data);
    }

    private void setLineChart_quyu(){
        float max = 0;
        String quyu_archs_1 = "";
        String quyu_archs_2 = "";
        for (int i = 0 ; i < addressFlags.length ; i ++){
            if(addressFlags[i] ){
                quyu_archs_1 = mEQIRankingBean.getAQI().get(i).getArchName();
            }
        }
        for (int i = addressFlags.length -1; i >= 0  ; i --){
            if(addressFlags[i] ){
                quyu_archs_2 = mEQIRankingBean.getAQI().get(i).getArchName();
            }
        }
        text_quyu_1.setText(quyu_archs_1);
        text_quyu_2.setText(quyu_archs_2);
        if(mMonthlyComparisonBean == null){
            return;
        }
        mLineChart_quyu.setDrawGridBackground(false);
        mLineChart_quyu.getDescription().setEnabled(false);
        mLineChart_quyu.setTouchEnabled(true);
        mLineChart_quyu.setDragEnabled(false);  //是否可以缩放
        mLineChart_quyu.setScaleEnabled(false);  //是否可以缩放
        mLineChart_quyu.setPinchZoom(false);
        mLineChart_quyu.setExtraOffsets(10,0,10,20);

        XAxis xAxis = mLineChart_quyu.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setAxisMaximum(31);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        //Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        /*
        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        //ll1.setTypeface(tf);
        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        //ll2.setTypeface(tf);
        */

        YAxis leftAxis = mLineChart_quyu.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.enableGridDashedLine(10f, 0f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(false);
        leftAxis.setAxisMinimum(0f);

        mLineChart_quyu.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mLineChart_quyu.animateX(2500);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mLineChart_quyu.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setEnabled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        if(mMonthlyComparisonBean_1 != null) {
            int beforeMonthSize = mMonthlyComparisonBean_1.getCurrentMonth().size();
            if (beforeMonthSize > 0) {
                ArrayList<Entry> values = new ArrayList<Entry>();
                for (int i = 0; i < beforeMonthSize; i++) {
                    String value = mMonthlyComparisonBean_1.getCurrentMonth().get(i).getAqi();
                    float v = Float.parseFloat(value);
                    if(v > max){
                       max = v;
                    }
                    Entry entry = new Entry(i+1, v);
                    values.add(entry);
                }

                LineDataSet set1 = new LineDataSet(values, "DataSet 1");
                set1.setDrawIcons(false);
                // set the line to be drawn like this "- - - - - -"
                //set1.enableDashedLine(10f, 10f, 0f);
                set1.setColor(Color.parseColor("#ff9e5d"));
                set1.setCircleColor(Color.parseColor("#ff9e5d"));
                set1.setLineWidth(1f);
                set1.setCircleRadius(4f);//设置焦点圆心的大小
                set1.setDrawCircleHole(true);
                set1.setCircleHoleRadius(2);
                set1.setCircleColorHole(Color.WHITE);
                set1.setValueTextSize(9f);
                set1.setDrawFilled(false);
                set1.setFormLineWidth(1f);
                set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 10f}, 0f));
                set1.setFormSize(15.f);
                set1.setDrawFilled(false);
                set1.setHighlightEnabled(false);
                set1.setDrawValues(false);

                if (Utils.getSDKInt() >= 18) {
                    // fill drawable only supported on api level 18 and above
                    //Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                    //set1.setFillDrawable(drawable);
                } else {
                    set1.setFillColor(Color.BLACK);
                }
                dataSets.add(set1); // add the datasets
            }
        }


        if(mMonthlyComparisonBean_2 != null) {

            int pushtimeMonthSize = mMonthlyComparisonBean_2.getCurrentMonth().size();
            if (pushtimeMonthSize > 0) {
                ArrayList<Entry> values_2 = new ArrayList<Entry>();

                for (int i = 0; i < pushtimeMonthSize; i++) {
                    String value = mMonthlyComparisonBean_2.getCurrentMonth().get(i).getAqi();
                    float v = Float.parseFloat(value);
                    if(v > max){
                        max = v;
                    }
                    Entry entry = new Entry(i+1,v);
                    values_2.add(entry);
                }


                LineDataSet set2 = new LineDataSet(values_2, "DataSet 2");
                set2.setDrawIcons(false);
                // set the line to be drawn like this "- - - - - -"
                set2.enableDashedLine(10f, 0f, 0f);//设置连线样式
                set2.setColor(Color.parseColor("#599fff"));
                set2.setDrawCircleHole(false);
                set2.setFormLineWidth(1f);
                set2.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 10f}, 0f));
                set2.setFormSize(15.f);
                set2.setLineWidth(1f);//设置线宽
                set2.setCircleColor(Color.parseColor("#599fff"));
                set2.setCircleRadius(4f);//设置焦点圆心的大小
                set2.setDrawCircleHole(true);
                set2.setCircleHoleRadius(2);
                set2.setCircleColorHole(Color.WHITE);
                set2.enableDashedHighlightLine(10f, 5f, 0f);//点击后的高亮线的显示样式
                set2.setHighlightLineWidth(2f);//设置点击交点后显示高亮线宽
                set2.setHighlightEnabled(false);//是否禁用点击高亮线
                set2.setHighLightColor(Color.RED);//设置点击交点后显示交高亮线的颜色
                set2.setValueTextSize(9f);//设置显示值的文字大小
                set2.setDrawFilled(false);//设置禁用范围背景填充
                set2.setDrawValues(false);

                if (Utils.getSDKInt() >= 18) {
                    // fill drawable only supported on api level 18 and above
                    //Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                    //set1.setFillDrawable(drawable);
                } else {
                    set2.setFillColor(Color.BLACK);
                }

                dataSets.add(set2); // add the datasets
            }
        }
        // create a data object with the datasets
        LineData data = new LineData(dataSets);
        leftAxis.setAxisMaximum(max);
        // set data
        mLineChart_quyu.setData(data);
    }

    private int mCheckCount = 2;
    private int oldClickedId1 = 0;
    private int newClickedId2 = 1;
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case 1:
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.dialog_title));

                builder.setMultiChoiceItems(address, addressFlags, new DialogInterface.OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        addressFlags[which]=isChecked;
                        if(isChecked){
                            mCheckCount ++;
                        }else{
                            mCheckCount --;
                        }

                    }
                });

                builder.setPositiveButton(getString(R.string.select_inspecter_btn), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCheckCount != 2){

                        }else{
                            for (int i = 0 ; i < addressFlags.length ; i ++){
                                if(addressFlags[i] ){
                                    quyu_id_1 = mEQIRankingBean.getAQI().get(i).getArchId();
                                }
                            }
                            for (int i = addressFlags.length -1; i >= 0  ; i --){
                                if(addressFlags[i] ){
                                    quyu_id_2 = mEQIRankingBean.getAQI().get(i).getArchId();
                                }
                            }
                            mHandler.sendEmptyMessage(HANDLER_GET_QUYUDUIBI_START);
                            dialog.dismiss();
                        }
                    }
                });
                dialog = builder.create();
                break;

            default:
                break;
        }
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_search:
                oneIconLayout.setVisibility(View.GONE);
                searchLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.search_btn_icon_right:{
                oneIconLayout.setVisibility(View.VISIBLE);
                searchLayout.setVisibility(View.GONE);
            }
            break;
            case R.id.search_btn_back:{
                finish();
            }
            break;
            case R.id.quyu_name:{
//                onCreateDialog(1).show();
                if(mCheckBoxPopWindow == null){
                    initCheckPopWindow();
                }
                mCheckBoxPopWindow.showAtLocation(findViewById(R.id.scrollview), Gravity.CENTER,0,0);
            }
            break;
            case R.id.date:{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());
                //initDatePicker();
                customDatePicker1.show(now);
            }
            break;
            case R.id.tongqi_date:{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());
                //initDatePicker();
                customDatePicker3.show(now);
            }
            break;
            case R.id.quyu_date:{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());
                //initDatePicker();
                customDatePicker4.show(now);
            }
            break;
            case R.id.youliang_datedate:{
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String now = sdf.format(new Date());
                //initDatePicker();
                customDatePicker2.show(now);
            }
            break;
            case R.id.sure:
                for (int i = 0 ; i < addressFlags.length ; i ++){
                    if(addressFlags[i] ){
                        quyu_id_1 = mEQIRankingBean.getArchs().get(i).getArchId();
                    }
                }
                for (int i = addressFlags.length -1; i >= 0  ; i --){
                    if(addressFlags[i] ){
                        quyu_id_2 = mEQIRankingBean.getArchs().get(i).getArchId();
                    }
                }
                mHandler.sendEmptyMessage(HANDLER_GET_QUYUDUIBI_START);
                mCheckBoxPopWindow.dismiss();
            break;
        }
    }

    private void initCheckPopWindow(){
        LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.layout_main_checkbox,null);

        TextView sure = (TextView) view.findViewById(R.id.sure);
        sure.setOnClickListener(this);
        checkBoxListView = (ListView) view.findViewById(R.id.lv);
        checkboxAdapter = new MyCheckboxAdapter();
        checkBoxListView.setAdapter(checkboxAdapter);

        mCheckBoxPopWindow = new PopupWindow(this);
//        mCheckBoxPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//        mCheckBoxPopWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mCheckBoxPopWindow.setWidth(this.getWindowManager().getDefaultDisplay().getWidth()/2);
        mCheckBoxPopWindow.setHeight(this.getWindowManager().getDefaultDisplay().getHeight()/ 2);
        mCheckBoxPopWindow.setContentView(view);
        mCheckBoxPopWindow.setOutsideTouchable(false);
        mCheckBoxPopWindow.setFocusable(true);
        mCheckBoxPopWindow.setTouchable(true);
        mCheckBoxPopWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mCheckBoxPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                for (int i = 0 ; i < addressFlags.length ; i ++){
                    if(addressFlags[i] ){
                        quyu_id_1 = mEQIRankingBean.getArchs().get(i).getArchId();
                    }
                }
                for (int i = addressFlags.length -1; i >= 0  ; i --){
                    if(addressFlags[i] ){
                        quyu_id_2 = mEQIRankingBean.getArchs().get(i).getArchId();
                    }
                }
                mHandler.sendEmptyMessage(HANDLER_GET_QUYUDUIBI_START);
            }
        });

    }

    private CustomDatePicker customDatePicker1,customDatePicker2,customDatePicker3,customDatePicker4;
    private void initDatePicker() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String now = sdf.format(new Date());
        customDatePicker1 = new CustomDatePicker(AirMonitoringActivity.this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
                mRankTime.setText(time.substring(0,7));
                mHandler.sendEmptyMessage(HANDLER_GET_RANKING_START);
            }
        }, "2010-01-01 00:00", now); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        //customDatePicker1.showSpecificTime(true); // 不显示时和分
        customDatePicker1.showYearMonth();
        customDatePicker1.setIsLoop(false); // 不允许循环滚动

        customDatePicker2 = new CustomDatePicker(AirMonitoringActivity.this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
                mYouliangTime.setText(time.substring(0,7));
                mHandler.sendEmptyMessage(HANDLER_GET_DAYS_PROPORTION_START);
            }
        }, "2010-01-01 00:00", now); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        //customDatePicker1.showSpecificTime(true); // 不显示时和分
        customDatePicker2.showYearMonth();
        customDatePicker2.setIsLoop(false); // 不允许循环滚动

        customDatePicker3 = new CustomDatePicker(AirMonitoringActivity.this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
                mTongqiTime.setText(time.substring(0,7));
                mHandler.sendEmptyMessage(HANDLER_GET_COMPARISON_START);
            }
        }, "2010-01-01 00:00", now); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        //customDatePicker1.showSpecificTime(true); // 不显示时和分
        customDatePicker3.showYearMonth();
        customDatePicker3.setIsLoop(false); // 不允许循环滚动

        customDatePicker4 = new CustomDatePicker(AirMonitoringActivity.this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
                quyu_date.setText(time.substring(0,7));
                mHandler.sendEmptyMessage(HANDLER_GET_QUYUDUIBI_START);
            }
        }, "2010-01-01 00:00", now); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        //customDatePicker1.showSpecificTime(true); // 不显示时和分
        customDatePicker4.showYearMonth();
        customDatePicker4.setIsLoop(false); // 不允许循环滚动

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String checkStr = (String) buttonView.getTag();
        int checkId = Integer.parseInt(checkStr);
        addressFlags[checkId] = isChecked;
        /**
         * by zw
         *
         * 1.预制两个点击的ID，0、1
         * 2.如果选择新的，就把1传给0，然后把新的传给1  保证被点击的按钮永远都是最新的
         * 3.取消选择时，保证剩余的要么都是-1，要么剩下的一个是旧的
         */
        if(isChecked){
            if(oldClickedId1 != -1 && newClickedId2 != -1){
                oldClickedId1 = newClickedId2;
                newClickedId2 = checkId;
            } else if(oldClickedId1 != -1){
                newClickedId2 = checkId;
            } else {
                oldClickedId1 = checkId;
            }

        }else {
            if(checkId == oldClickedId1){
                oldClickedId1 = newClickedId2;
                newClickedId2 = -1;
            } else if(checkId == newClickedId2){
                newClickedId2 = -1;
            }

        }
        checkboxAdapter.notifyDataSetChanged();
    }


    private class MyCheckboxAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return address.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(AirMonitoringActivity.this).inflate(R.layout.layout_main_checkbox_item,parent,false);
            }

            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
            checkBox.setTag("" + position);
            if(oldClickedId1 == position || newClickedId2 == position){
                checkBox.setChecked(true);
            }else {
                checkBox.setChecked(false);
            }

            checkBox.setOnCheckedChangeListener(AirMonitoringActivity.this);
            TextView textView = (TextView) convertView.findViewById(R.id.tv);
            textView.setText(address[position]);

            return convertView;
        }
    }
}