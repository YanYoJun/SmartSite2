package com.isoftstone.smartsite.model.main.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.aqi.DataQueryVoBean;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.model.map.ui.VideoMonitorMapActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gone on 2017/10/21.
 */

public class PMDataInfoActivity extends BaseActivity {
    private HttpPost mHttpPsot = new HttpPost();
    private LineChart mLineChart = null;
    private TextView mDevicesName = null;
    private TextView mMap = null;
    private ImageButton mImageView_back = null;
    private ImageButton mImageView_icon = null;
    private TextView toolbar_title = null;
    private LinearLayout mGotoMap = null;
    public static  final  int HANDLER_GET_DATA_START = 1;
    public static  final  int HANDLER_GET_DATA_END = 2;
    public static  final  int HANDLER_GET_24DATA_START = 3;
    public static  final  int HANDLER_GET_24DATA_END = 4;
    private int devicesId ;
    private String address ;
    private ArrayList<DataQueryVoBean> list = null;
    private ArrayList<DataQueryVoBean> list_24 = null;
    private TextView text_pm10 ;
    private TextView text_pm25 ;
    private TextView text_so2 ;
    private TextView text_no2 ;
    private TextView text_o3 ;
    private TextView text_co ;

    private TextView text_indoortemp ;
    private TextView text_windspeed ;
    private TextView text_winddirection ;
    private TextView text_airpressure ;
    private TextView text_temp ;
    private TextView text_humidity ;
    private TextView text_precipitation ;

    private Spinner shujuSpinner = null;
    private String[] name = {"PM2.5","PM10","CO2"};
    private String begintime = "";
    private int position;
    private ArrayList<DataQueryVoBean> mData  = null;
    private DataQueryVoBean mDataQueryVoBean;
    private String devicesCode;
    @Override
    protected int getLayoutRes() {
        return R.layout.activity_pmdatainfo;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        init();

        mDataQueryVoBean = (DataQueryVoBean)getIntent().getSerializableExtra("devicesbean");
        position = getIntent().getIntExtra("position",0);
        devicesCode = getIntent().getStringExtra("devicesCode");
        mData = (ArrayList<DataQueryVoBean>) getIntent().getSerializableExtra("devices");
        if(mDataQueryVoBean != null){
            devicesId = mDataQueryVoBean.getDeviceId();
            address = mDataQueryVoBean.getDeviceName();
            begintime = mDataQueryVoBean.getPushTime();
        }else{
            devicesId = getIntent().getIntExtra("id",0);
            address = getIntent().getStringExtra("address");
            begintime = "";
        }

        setOnCliceked();

        if(mDataQueryVoBean == null){
            toolbar_title.setText(getString(R.string.device_list_left));
        }else {
            toolbar_title.setText(getString(R.string.device_list_right));
        }
        if(devicesCode != null){
            mDevicesName.setText(devicesCode);
        }

        if(mDataQueryVoBean == null){
            mHandler.sendEmptyMessage(HANDLER_GET_DATA_START);
        }else {
            setData();
            mHandler.sendEmptyMessage(HANDLER_GET_24DATA_START);
        }
    }

    private void init(){
        mImageView_back = (ImageButton)findViewById(R.id.btn_back);
        mImageView_icon = (ImageButton)findViewById(R.id.btn_icon);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
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
        mLineChart = (LineChart)findViewById(R.id.chart3);
        mDevicesName = (TextView)findViewById(R.id.textView1);
        mMap = (TextView)findViewById(R.id.textView4);

        text_pm10 = (TextView)findViewById(R.id.text_pm10);
        text_pm25 = (TextView)findViewById(R.id.text_pm25);
        text_so2 = (TextView)findViewById(R.id.text_so2);
        text_no2 = (TextView)findViewById(R.id.text_no2);
        text_o3 = (TextView)findViewById(R.id.text_o3);
        text_co = (TextView)findViewById(R.id.text_co);

        text_indoortemp = (TextView)findViewById(R.id.text_indoortemp);
        text_windspeed = (TextView)findViewById(R.id.text_windspeed);
        text_winddirection = (TextView)findViewById(R.id.text_winddirection);
        text_airpressure = (TextView)findViewById(R.id.text_airpressure);
        text_temp = (TextView)findViewById(R.id.text_temp);
        text_humidity = (TextView)findViewById(R.id.text_humidity);
        text_precipitation = (TextView)findViewById(R.id.text_precipitation);



        mGotoMap = (LinearLayout)findViewById(R.id.gotomap);


        shujuSpinner = (Spinner) findViewById(R.id.shuju_name);
        shujuSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setLineChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter adapter_tongqi = new ArrayAdapter<String>(this,R.layout.spinner_item,name);
        shujuSpinner.setAdapter(adapter_tongqi);
        shujuSpinner.setSelection(0);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_GET_DATA_START:{
                    showDlg(getString(R.string.toast_information));
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            getDataInfo();
                        }
                    };
                    thread.start();
                }
                break;
                case  HANDLER_GET_DATA_END:{
                    setData();
                    closeDlg();
                    mHandler.sendEmptyMessage(HANDLER_GET_24DATA_START);
                }
                break;
                case HANDLER_GET_24DATA_START:{
                    showDlg(getString(R.string.toast_information));
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            get24DataInfo();
                        }
                    };
                    thread.start();
                }
                break;
                case  HANDLER_GET_24DATA_END:{
                    setLineChart();
                    closeDlg();
                }
                break;
            }
        }
    };

    private void getDataInfo(){
        if(begintime.equals("")){
            list =  mHttpPsot.onePMDevicesDataList("["+devicesId+"]","0","","");
        }else{
            list =  mHttpPsot.onePMDevicesDataList("["+devicesId+"]","0",begintime,"");
        }

        mHandler.sendEmptyMessage(HANDLER_GET_DATA_END);
    }

    private void get24DataInfo(){
        if(begintime.equals("")){
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            Calendar calendar = Calendar.getInstance();
            String startTime = df.format(calendar.getTime());
            list_24 = mHttpPsot.onePMDevices24Data(""+devicesId,startTime);
        }else{
            list_24 = mHttpPsot.onePMDevices24Data(""+devicesId,begintime);
        }

        mHandler.sendEmptyMessage(HANDLER_GET_24DATA_END);
    }

    private void setOnCliceked(){
        mGotoMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到地图
                Intent intent = new Intent();
                if(mData != null){
                    intent.putExtra("devices",mData);
                    intent.putExtra("type",VideoMonitorMapActivity.TYPE_ENVIRONMENT);
                    intent.putExtra("position",position);
                    intent.setClass(getBaseContext(),VideoMonitorMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(intent);
                }
            }
        });
    }

    private void setData(){
        if(address != null){
            mMap.setText(address);
        }

        DataQueryVoBean dataQueryVoBean =null;
        if(mDataQueryVoBean != null){
            dataQueryVoBean = mDataQueryVoBean;
        }else{
            if(list == null || list.size() <= 0){
                return;
            }
            dataQueryVoBean = list.get(0);
        }
        if(dataQueryVoBean == null){
            return;
        }
        text_pm10.setText("PM10:"+doubleToString(dataQueryVoBean.getPm10()));
        text_pm25.setText("PM2.5:"+doubleToString(dataQueryVoBean.getPm2_5()));
        text_so2.setText("O2:");
        text_so2.setVisibility(View.GONE);
        text_no2.setText("no2:");
        text_no2.setVisibility(View.GONE);
        text_o3.setText("O3:");
        text_o3.setVisibility(View.GONE);
        text_co.setText("CO2:"+ doubleToString(dataQueryVoBean.getCo2()));

        DecimalFormat df = new DecimalFormat("#.0");
        text_indoortemp.setText(R.string.wind_speed+doubleToString(dataQueryVoBean.getWindSpeed()));
        text_windspeed.setText(R.string.wind_direction+dataQueryVoBean.getWindDirection());
        text_winddirection.setText(R.string.pressure+doubleToString(dataQueryVoBean.getAtmosphericPressure()));
        text_airpressure.setVisibility(View.INVISIBLE);

        text_temp.setText(R.string.tempressure+doubleToString(dataQueryVoBean.getAirTemperature()));
        text_humidity.setText(R.string.humidity+doubleToString(dataQueryVoBean.getAirHumidity()));
        text_precipitation.setText(R.string.heytal+doubleToString(dataQueryVoBean.getRainfall()));

    }

    private String doubleToString( Double dl){
        if(dl == null) return "0";

        String value = dl+"";
        if(value.indexOf(".") == -1){
            return  value;
        }else{
            if(value.indexOf(".") +3 < value.length()){
                return  value.substring(0,value.indexOf(".")+3);
            }else{
                return  value;
            }
        }
    }

    private void setLineChart(){
        if(list_24 == null || list_24.size() <= 0){
            return;
        }
        Collections.sort(list_24, new Comparator<DataQueryVoBean>() {
            @Override
            public int compare(DataQueryVoBean t1, DataQueryVoBean t2) {

                return t1.getPushTime().compareTo(t2.getPushTime());
            }
        });
        int index = shujuSpinner.getSelectedItemPosition();
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
        xAxis.setAxisMaximum(24);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.enableGridDashedLine(10f, 0f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(false);

        mLineChart.getAxisRight().setEnabled(false);



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
        int beforeMonthSize = list_24.size();
        if(beforeMonthSize > 0){
            ArrayList<Entry> values = new ArrayList<Entry>();
            for (int i = 0; i < beforeMonthSize; i++) {
                String value = "";
                if(index == 0){
                    value =list_24.get(i).getPm2_5().toString();
                }else if(index == 1){
                    value = list_24.get(i).getPm10().toString();
                }else if(index == 2){
                    value =list_24.get(i).getCo2().toString();
                }
                String time=list_24.get(i).getPushTime().substring(11, 13);
                Entry entry = new Entry(Integer.parseInt(time),Float.parseFloat(value));
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



        // create a data object with the datasets
        LineData data = new LineData(dataSets);
        // set data
        mLineChart.setData(data);

    }
}
