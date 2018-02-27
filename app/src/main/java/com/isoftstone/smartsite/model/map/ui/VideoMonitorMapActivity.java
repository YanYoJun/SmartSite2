package com.isoftstone.smartsite.model.map.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.aqi.DataQueryVoBean;
import com.isoftstone.smartsite.http.video.DevicesBean;
import com.isoftstone.smartsite.model.main.ui.PMDataInfoActivity;
import com.isoftstone.smartsite.model.main.ui.PMHistoryInfoActivity;
import com.isoftstone.smartsite.model.system.ui.PermissionsActivity;
import com.isoftstone.smartsite.model.system.ui.PermissionsChecker;
import com.isoftstone.smartsite.model.video.SnapPicturesActivity;
import com.isoftstone.smartsite.model.video.VideoPlayActivity;
import com.isoftstone.smartsite.model.video.VideoRePlayActivity;
import com.isoftstone.smartsite.model.video.bean.AlbumInfo;
import com.isoftstone.smartsite.model.video.bean.PhotoInfo;
import com.isoftstone.smartsite.model.video.bean.PhotoList;
import com.isoftstone.smartsite.model.video.utils.ThumbnailsUtil;
import com.isoftstone.smartsite.utils.DensityUtils;
import com.isoftstone.smartsite.utils.FilesUtils;
import com.isoftstone.smartsite.utils.LogUtils;
import com.isoftstone.smartsite.utils.MapUtils;
import com.isoftstone.smartsite.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter.COLOR_0;
import static com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter.COLOR_150;
import static com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter.COLOR_250;
import static com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter.COLOR_350;
import static com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter.COLOR_420;
import static com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter.COLOR_50;
import static com.isoftstone.smartsite.model.main.adapter.PMDevicesListAdapter.COLOR_600;

public class VideoMonitorMapActivity extends BaseActivity implements View.OnClickListener, AMap.OnMarkerClickListener {

    public static final int TYPE_CAMERA = 0x0001;
    public static final int TYPE_ENVIRONMENT = 0x0002;

    private MapView mMapView;
    private AMap aMap;

    private LatLng aotiLatLon = new LatLng(30.482348,114.514417);
    private PopupWindow mPopWindow;
    private ArrayList<DataQueryVoBean> envir_devices;
    private List<DevicesBean> camera_devices;


    private TextView tv_deviceNumber;
    private TextView tv_isOnline;
    private TextView tv_deviceTime;
    private TextView tv_deviceAddress;
    private View videoView;
    private ImageView iv_video;
    private TextView tv_video;
    private View historyView;
    private ImageView iv_history;
    private TextView tv_history;
    private View galleryView;
    private ImageView iv_gallery;
    private TextView tv_gallery;
    private DataQueryVoBean currentEnvirDevice;
    private DevicesBean currentCameraDevice;
    private Marker roundMarker;

    private int type;
    private View eviorment_view;
    private TextView tv_pm10;
    private TextView tv_pm25;
    private TextView tv_pmco2;
    private View background_line;

    private boolean isHasData = false;

    private float zoom = 13f;
    private CameraPosition mCameraPosition;
    private List<Marker> markerList;
    private int position;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_video_monitor_map;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        type = getIntent().getIntExtra("type",0);
        position = getIntent().getIntExtra("position",-1);

        if(type == TYPE_CAMERA){
            camera_devices = (ArrayList<DevicesBean>) getIntent().getSerializableExtra("devices");
            if(camera_devices == null || camera_devices.size() == 0 ){
                ToastUtils.showLong(getString(R.string.map_not_get_device_address_info));
            }else {
                isHasData = true;

                if(position != -1){
                    currentCameraDevice = camera_devices.get(position);
                }
            }
        } else if(type == TYPE_ENVIRONMENT){
            envir_devices = (ArrayList<DataQueryVoBean>) getIntent().getSerializableExtra("devices");
            if(envir_devices == null || envir_devices.size() == 0 ){
                ToastUtils.showLong(getString(R.string.map_not_get_device_address_info));
            }else {
                isHasData = true;
                if(position != -1){
                    currentEnvirDevice = envir_devices.get(position);
                }
            }
        }

        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState){
        TextView tv_title = (TextView) findViewById(R.id.toolbar_title);

        if(type == TYPE_CAMERA){
            tv_title.setText(getString(R.string.video_monitor_map));
        } else if(type == TYPE_ENVIRONMENT) {
            tv_title.setText(getString(R.string.pmd_monitor_map));
        } else if(type == 0){
            tv_title.setText("");
        }


        findViewById(R.id.btn_back).setOnClickListener(this);

        ImageButton btn_search = (ImageButton) findViewById(R.id.btn_icon);
        btn_search.setImageResource(R.drawable.search);
        btn_search.setVisibility(View.GONE);

        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);

        initMapView();
        initPopWindow();
    }

    private void showDeviceInfo(){
        if(currentCameraDevice != null){
            LatLng latLng = new LatLng(Double.parseDouble(currentCameraDevice.getLatitude()),
                                    Double.parseDouble(currentCameraDevice.getLongitude()));
            initLocation(latLng);
        }else if(currentEnvirDevice != null){
            LatLng latLng = new LatLng(Double.parseDouble(currentEnvirDevice.getLatitude()),
                    Double.parseDouble(currentEnvirDevice.getLongitude()));
            initLocation(latLng);
        } else {
            initLocation(aotiLatLon);
        }
        if(currentCameraDevice!=null||currentEnvirDevice!=null){
            addAndRemoveRoundMarker();
        }


        if(type == TYPE_CAMERA && currentCameraDevice!=null){
            if(currentCameraDevice.getDeviceCoding().length() >= 10){
                tv_deviceNumber.setText(currentCameraDevice.getDeviceCoding().substring(0,10));
            } else {
                tv_deviceNumber.setText(currentCameraDevice.getDeviceCoding());
            }
            if("0".equals(currentCameraDevice.getDeviceStatus())){
                tv_isOnline.setText(getString(R.string.map_online));
                tv_isOnline.setBackgroundResource(R.drawable.shape_map_online);
            } else if("1".equals(currentCameraDevice.getDeviceStatus())){
                tv_isOnline.setText(getString(R.string.map_offline));
                tv_isOnline.setBackgroundResource(R.drawable.shape_offline);
            } else if("2".equals(currentCameraDevice.getDeviceStatus())){
                tv_isOnline.setText(getString(R.string.map_fault));
                tv_isOnline.setBackgroundResource(R.drawable.shape_map_bad);
            }
            tv_deviceTime.setText(getString(R.string.map_install_time)+ currentCameraDevice.getInstallTime().substring(0,10));
            tv_deviceAddress.setText(currentCameraDevice.getDeviceName());
            if("0".equals(currentCameraDevice.getDeviceStatus())){
                videoView.setClickable(true);
                videoView.setEnabled(true);
                historyView.setClickable(true);
                historyView.setEnabled(true);
                galleryView.setClickable(true);
                galleryView.setEnabled(true);
                iv_video.setImageResource(R.drawable.time);
                iv_history.setImageResource(R.drawable.history);
                iv_gallery.setImageResource(R.drawable.capture);
                tv_video.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_gallery.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                videoView.setClickable(false);
                videoView.setEnabled(false);
                historyView.setClickable(false);
                historyView.setEnabled(false);
                galleryView.setClickable(false);
                galleryView.setEnabled(false);
                iv_video.setImageResource(R.drawable.timedisable);
                iv_history.setImageResource(R.drawable.historydisable);
                iv_gallery.setImageResource(R.drawable.capturedisable);
                tv_video.setTextColor(getResources().getColor(R.color.gray_9999));
                tv_history.setTextColor(getResources().getColor(R.color.gray_9999));
                tv_gallery.setTextColor(getResources().getColor(R.color.gray_9999));
            }
            initLocation(new LatLng(Double.parseDouble(currentCameraDevice.getLatitude()),Double.parseDouble(currentCameraDevice.getLongitude())));
        } else if(type == TYPE_ENVIRONMENT && currentEnvirDevice!=null){
            tv_deviceNumber.setText(currentEnvirDevice.getDeviceCoding());
            if(0 == currentEnvirDevice.getDeviceStatus()){
                tv_isOnline.setText(getString(R.string.map_online));
                tv_isOnline.setBackgroundResource(R.drawable.shape_map_online);
            } else if(1 == currentEnvirDevice.getDeviceStatus()){
                tv_isOnline.setText(getString(R.string.map_offline));
                tv_isOnline.setBackgroundResource(R.drawable.shape_offline);
            } else if(2 == currentEnvirDevice.getDeviceStatus()){
                tv_isOnline.setText(getString(R.string.map_fault));
                tv_isOnline.setBackgroundResource(R.drawable.shape_map_bad);
            }
            tv_deviceTime.setText(getString(R.string.map_install_time) + currentEnvirDevice.getInstallTime().substring(0,10));
            tv_deviceAddress.setText(currentEnvirDevice.getDeviceName());
            if(0 == currentEnvirDevice.getDeviceStatus()){
                videoView.setClickable(true);
                videoView.setEnabled(true);
                historyView.setClickable(true);
                historyView.setEnabled(true);
                galleryView.setClickable(true);
                galleryView.setEnabled(true);
                iv_video.setImageResource(R.drawable.time);
                iv_history.setImageResource(R.drawable.history);
                iv_gallery.setImageResource(R.drawable.capture);
                tv_video.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_gallery.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                videoView.setClickable(false);
                videoView.setEnabled(false);
                historyView.setClickable(false);
                historyView.setEnabled(false);
                galleryView.setClickable(false);
                galleryView.setEnabled(false);
                iv_video.setImageResource(R.drawable.timedisable);
                iv_history.setImageResource(R.drawable.historydisable);
                iv_gallery.setImageResource(R.drawable.capturedisable);
                tv_video.setTextColor(getResources().getColor(R.color.gray_9999));
                tv_history.setTextColor(getResources().getColor(R.color.gray_9999));
                tv_gallery.setTextColor(getResources().getColor(R.color.gray_9999));
            }

            String pm10 = "";
            double d_pm10 = currentEnvirDevice.getPm10() == null ? 0 : currentEnvirDevice.getPm10();
            int pm_10 = (int) d_pm10;

            double d_pm25 = currentEnvirDevice.getPm2_5() == null ? 0 : currentEnvirDevice.getPm2_5();
            int pm_25 = (int) d_pm25;

            double d_co2 = currentEnvirDevice.getCo2() == null ? 0 : currentEnvirDevice.getCo2();
            int pm_co2 = (int) d_co2;

            if(pm_10 < 50){
                pm10 = "PM10：<font color='" + COLOR_50 + "'>" + pm_10 + "</font>";
            } else if(pm_10 < 150){
                pm10 = "PM10：<font color='" + COLOR_150 + "'>" + pm_10 + "</font>";
            } else if(pm_10 < 250){
                pm10 = "PM10：<font color='" + COLOR_250 + "'>" + pm_10 + "</font>";
            } else if(pm_10 < 350){
                pm10 = "PM10：<font color='" + COLOR_350 + "'>" + pm_10 + "</font>";
            } else if(pm_10 < 420){
                pm10 = "PM10：<font color='" + COLOR_420 + "'>" + pm_10 + "</font>";
            } else {
                pm10 = "PM10：<font color='" + COLOR_600 + "'>" + pm_10 + "</font>";
            }

            tv_pm10.setText(Html.fromHtml(pm10));
            String pm25 = "PM2.5：<font color='" + COLOR_0 + "'>" + pm_25 + "</font>";
            tv_pm25.setText(Html.fromHtml(pm25));
            String co2 = "CO2：<font color='" + COLOR_0 + "'>" + pm_co2 + "</font>";
            tv_pmco2.setText(Html.fromHtml(co2));
            initLocation(new LatLng(Double.parseDouble(currentEnvirDevice.getLatitude()),Double.parseDouble(currentEnvirDevice.getLongitude())));
        }

        mPopWindow.showAtLocation(mMapView, Gravity.BOTTOM,0,DensityUtils.dip2px(this,-8));
    }

    private void initMapView(){
        aMap = mMapView.getMap();
        if(aMap != null){
            aMap.setOnMarkerClickListener(this);
            aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {

                }

                @Override
                public void onCameraChangeFinish(CameraPosition cameraPosition) {
                    zoom = cameraPosition.zoom;
                }
            });
        }


        addRoundLine();

        if(isHasData){
            initMarker();
        }
    }

    private void initLocation(LatLng latLng){
        mCameraPosition = new CameraPosition(latLng,zoom,0,0);

        CameraUpdate update = CameraUpdateFactory.newCameraPosition(mCameraPosition);
        if(aMap != null){
            aMap.animateCamera(update);
        }

    }


    private void initMarker(){
        markerList = new ArrayList<>();
        if(type == TYPE_CAMERA){
            for (int i = 0; i < camera_devices.size(); i++){
                DevicesBean device = camera_devices.get(i);
                LatLng latLng = new LatLng(Double.parseDouble(device.getLatitude()),Double.parseDouble(device.getLongitude()));
                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng);
                markerOption.visible(true);

                markerOption.draggable(false);//设置Marker可拖动

                //0在线，1离线，2故障
                if("0".equals(device.getDeviceStatus())){
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(),R.drawable.camera_normal)));
                }else if("1".equals(device.getDeviceStatus())){
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(),R.drawable.camera_gray)));
                } else  if("2".equals(device.getDeviceStatus())){
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(),R.drawable.camera_red)));
                }

                // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                markerOption.setFlat(true);//设置marker平贴地图效果

                Marker marker = aMap.addMarker(markerOption);
                marker.setAnchor(0.5f,1.2f);
                marker.setObject(device);

                markerList.add(marker);
            }
        } else if(type == TYPE_ENVIRONMENT){
            for (int i = 0; i < envir_devices.size(); i++){
                DataQueryVoBean device = envir_devices.get(i);
                LogUtils.d(TAG,device);
                LatLng latLng = new LatLng(Double.parseDouble(device.getLatitude()),Double.parseDouble(device.getLongitude()));
                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng);
                markerOption.visible(true);

                markerOption.draggable(false);//设置Marker可拖动
                //0在线，1离线，2故障
                if(0 == device.getDeviceStatus()){
                    double pm10 = device.getPm10() == null ? 0 : device.getPm10();
                    if(pm10 <= 0){
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.environment_blue)));
                    }else if(pm10 <= 50){
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.environment_green)));
                    }else if(pm10 <= 150){
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.environment_yellow)));
                    }else if(pm10 <= 250){
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.environment_orange)));
                    }else if(pm10 <= 350){
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.environment_red)));
                    }else if(pm10 <= 420){
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.environment_pink)));
                    }else {
                        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                                .decodeResource(getResources(),R.drawable.environment_dark)));
                    }
                }else if(1 == device.getDeviceStatus()){
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(),R.drawable.environment_gray)));
                } else  if(2 == device.getDeviceStatus()){
                    markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(),R.drawable.environment_red)));
                }


                // 将Marker设置为贴地显示，可以双指下拉地图查看效果
                markerOption.setFlat(true);//设置marker平贴地图效果

                if(aMap != null){
                    Marker marker = aMap.addMarker(markerOption);
                    marker.setAnchor(0.5f,1.2f);
                    marker.setObject(device);

                    markerList.add(marker);
                }
            }
        }

    }

    private void addAndRemoveRoundMarker(){
        MarkerOptions markerOption1 = new MarkerOptions();
        if(type == TYPE_CAMERA){
            markerOption1.position(new LatLng(Double.parseDouble(currentCameraDevice.getLatitude()
            ), Double.parseDouble(currentCameraDevice.getLongitude())));
        }else if(type == TYPE_ENVIRONMENT){
            markerOption1.position(new LatLng(Double.parseDouble(currentEnvirDevice.getLatitude()
            ), Double.parseDouble(currentEnvirDevice.getLongitude())));
        }

        markerOption1.visible(true);

        markerOption1.draggable(false);//设置Marker可拖动
        View centerView = LayoutInflater.from(this).inflate(R.layout.layout_map_video_monitor_center,null);
        markerOption1.icon(BitmapDescriptorFactory.fromView(centerView));

        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption1.setFlat(true);//设置marker平贴地图效果

        roundMarker = aMap.addMarker(markerOption1);
        roundMarker.setAnchor(0.5f,0.5f);


    }

    private void initPopWindow(){
        View popWindowView = LayoutInflater.from(this).inflate(R.layout.layout_map_video_monitor_popwindow,null);
        popWindowView.findViewById(R.id.iv_dismiss).setOnClickListener(this);
        tv_deviceNumber = (TextView) popWindowView.findViewById(R.id.tv_device_number);
        tv_isOnline = (TextView) popWindowView.findViewById(R.id.tv_isonline);
        tv_deviceTime = (TextView) popWindowView.findViewById(R.id.tv_device_time);
        tv_deviceAddress = (TextView) popWindowView.findViewById(R.id.tv_address);

        videoView = popWindowView.findViewById(R.id.video);
        iv_video = (ImageView) popWindowView.findViewById(R.id.iv_video);
        tv_video = (TextView) popWindowView.findViewById(R.id.tv_video);
        historyView = popWindowView.findViewById(R.id.history);
        iv_history = (ImageView) popWindowView.findViewById(R.id.iv_history);
        tv_history = (TextView) popWindowView.findViewById(R.id.tv_history);
        galleryView = popWindowView.findViewById(R.id.gallery);
        iv_gallery = (ImageView) popWindowView.findViewById(R.id.iv_gallery);
        tv_gallery = (TextView) popWindowView.findViewById(R.id.tv_gallery);
        videoView.setOnClickListener(this);
        historyView.setOnClickListener(this);
        galleryView.setOnClickListener(this);

        eviorment_view = popWindowView.findViewById(R.id.eviorment_view);

        tv_pm10 = (TextView) popWindowView.findViewById(R.id.text_pm10);
        tv_pm25 = (TextView) popWindowView.findViewById(R.id.text_pm25);
        tv_pmco2 = (TextView) popWindowView.findViewById(R.id.text_co2);
        background_line = popWindowView.findViewById(R.id.background_line);
        if(type == TYPE_CAMERA){
            eviorment_view.setVisibility(View.GONE);
            tv_video.setText(getString(R.string.realtime_video));
            tv_history.setText(getString(R.string.historical_monitoring));
            tv_gallery.setText(getString(R.string.snapshot_record));

        } else if(type == TYPE_ENVIRONMENT){
            galleryView.setVisibility(View.GONE);
            background_line.setVisibility(View.GONE);
            tv_video.setText(getString(R.string.realtime_video));
            tv_history.setText(getString(R.string.historical_monitoring));
        }

        mPopWindow = new PopupWindow(this);
        mPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopWindow.setContentView(popWindowView);
        mPopWindow.setOutsideTouchable(false);
        mPopWindow.setFocusable(true);
        mPopWindow.setTouchable(true);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(roundMarker != null){
                    roundMarker.remove();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    private boolean isFirstIn = true;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isFirstIn){
            showDeviceInfo();
            isFirstIn = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                this.finish();
                break;
            case R.id.iv_dismiss:
                mPopWindow.dismiss();
                break;
            case R.id.video:
                if(type == TYPE_CAMERA){
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("resCode", currentCameraDevice.getDeviceCoding());
                    bundle.putInt("resSubType", currentCameraDevice.getCameraType());
                    intent.putExtras(bundle);
                    intent.setClass(this, VideoPlayActivity.class);
                    startActivity(intent);
                } else if(type == TYPE_ENVIRONMENT){
                        //实时数据
                        Intent intent = new Intent();
                        intent.putExtra("id",currentEnvirDevice.getDeviceId());
                        intent.putExtra("address",currentEnvirDevice.getDeviceName());
                        intent.putExtra("devices",envir_devices);
                        intent.putExtra("position",position);
                        intent.setClass(this, PMDataInfoActivity.class);
                        this.startActivity(intent);
                }

                break;
            case R.id.history:
                if(type == TYPE_CAMERA){
                    Date now = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String beginTime = formatter.format(now) + " 00:00:00";
                    String endTime = formatter.format(now) + " 23:59:59";

                    Intent intent1 = new Intent();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("resCode", currentCameraDevice.getDeviceCoding());
                    bundle1.putString("resSubType", currentCameraDevice.getDeviceType() + "");
                    bundle1.putString("resName", currentCameraDevice.getDeviceName());
                    bundle1.putBoolean("isOnline", "0".equals(currentCameraDevice.getDeviceStatus()));
                    bundle1.putString("beginTime", beginTime);
                    bundle1.putString("endTime", endTime);
                    bundle1.putInt("position", 0);

                    //Toast.makeText(mContext, "ViewHolder: " +  ((ViewHolder)rootView.getTag()).name.getText().toString(), Toast.LENGTH_SHORT).show();
                    intent1.putExtras(bundle1);
                    intent1.setClass(this, VideoRePlayActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent1);
                } else if(type == TYPE_ENVIRONMENT){
                        //历史数据
                        Intent intent = new Intent();
                        intent.setClass(this, PMHistoryInfoActivity.class);
                        intent.putExtra("devices",envir_devices);
                        intent.putExtra("position",position);
                        intent.putExtra("id",currentEnvirDevice.getDeviceId());
                        intent.putExtra("address",currentEnvirDevice.getDeviceName());
                        this.startActivity(intent);
                    }

                break;
            case R.id.gallery:
                //打开系统相册浏览照片  
                PermissionsChecker mPermissionsChecker = new PermissionsChecker(getBaseContext());
                if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
                    PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
                }else {
                    mListImageInfo.clear();
                    mlistPhotoInfo.clear();
                    new ImageAsyncTask().execute();
                }
                break;
        }
    }

    private static final int REQUEST_CODE = 100; // 权限检查请求码
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<PhotoInfo> mlistPhotoInfo = new ArrayList<PhotoInfo>();
    private List<AlbumInfo> mListImageInfo = new ArrayList<AlbumInfo>();

    private class ImageAsyncTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            //获取缩略图
            ThumbnailsUtil.clear();
            ContentResolver sContentResolver = getBaseContext().getContentResolver();
            String[] projection = { MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID, MediaStore.Images.Thumbnails.DATA };
            Cursor cur = sContentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);

            if (cur!=null&&cur.moveToFirst()) {
                int image_id;
                String image_path;
                int image_idColumn = cur.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID);
                int dataColumn = cur.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                do {
                    image_id = cur.getInt(image_idColumn);
                    image_path = cur.getString(dataColumn);
                    ThumbnailsUtil.put(image_id, "file://"+image_path);
                } while (cur.moveToNext());
            }
            //获取原图
            Cursor cursor = sContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, "date_modified DESC");
            String _path="_data";
            String _album="bucket_display_name";
            HashMap<String,AlbumInfo> myhash = new HashMap<String, AlbumInfo>();
            AlbumInfo albumInfo = null;
            PhotoInfo photoInfo = null;
            if (cursor!=null&&cursor.moveToFirst())
            {
                do{
                    int index = 0;
                    int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String path = cursor.getString(cursor.getColumnIndex(_path));
                    String album = cursor.getString(cursor.getColumnIndex(_album));
                    List<PhotoInfo> stringList = new ArrayList<PhotoInfo>();
                    photoInfo = new PhotoInfo();
                    if(myhash.containsKey(album)){
                        albumInfo = myhash.remove(album);
                        if(mListImageInfo.contains(albumInfo)) {
                            index = mListImageInfo.indexOf(albumInfo);
                        }
                        photoInfo.setImage_id(_id);
                        photoInfo.setPath_file("file://"+path);
                        photoInfo.setPath_absolute(path);
                        albumInfo.getList().add(photoInfo);
                        mListImageInfo.set(index, albumInfo);
                        myhash.put(album, albumInfo);
                        //Log.i("zyf", albumInfo.toString() + "\n" + album);
                    }else{
                        albumInfo = new AlbumInfo();
                        stringList.clear();
                        photoInfo.setImage_id(_id);
                        photoInfo.setPath_file("file://"+path);
                        photoInfo.setPath_absolute(path);
                        stringList.add(photoInfo);
                        albumInfo.setImage_id(_id);
                        albumInfo.setPath_file("file://"+path);
                        albumInfo.setPath_absolute(path);
                        albumInfo.setName_album(album);
                        albumInfo.setList(stringList);
                        mListImageInfo.add(albumInfo);
                        myhash.put(album, albumInfo);
                        //Log.i("zyf", albumInfo.toString() + "\n" + album);
                    }
                }while (cursor.moveToNext());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            //Log.i("zzz", FilesUtils.SNATCH_PATH + mDevicesBean.getDeviceCoding() + "/");
            for (int i=0; i<mListImageInfo.size(); i++) {
                if (mListImageInfo.get(i).getPath_absolute().contains(FilesUtils.SNATCH_PATH + currentCameraDevice.getDeviceCoding() + "/")) {
                    mlistPhotoInfo.addAll(mListImageInfo.get(i).getList());
                }
            }

            if (mlistPhotoInfo.size() <= 0 ) {
                ToastUtils.showShort(getText(R.string.snatch_photo_size_0_toast).toString());
                return;
            } else {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                PhotoList photo = new PhotoList();
                photo.setList(mlistPhotoInfo);
                bundle.putSerializable("list", photo);
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(getBaseContext(), SnapPicturesActivity.class);
                getBaseContext().startActivity(intent);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getObject() != null){
            if(type == TYPE_CAMERA){
                DevicesBean device = (DevicesBean) marker.getObject();
                currentCameraDevice = device;
                if(device.getDeviceCoding().length() >= 10){
                    tv_deviceNumber.setText(device.getDeviceCoding().substring(0,10));
                }else {
                    tv_deviceNumber.setText(device.getDeviceCoding());
                }
                if("0".equals(device.getDeviceStatus())){
                    tv_isOnline.setText(getString(R.string.map_online));
                    tv_isOnline.setBackgroundResource(R.drawable.shape_map_online);
                } else if("1".equals(device.getDeviceStatus())){
                    tv_isOnline.setText(getString(R.string.map_offline));
                    tv_isOnline.setBackgroundResource(R.drawable.shape_offline);
                } else if("2".equals(device.getDeviceStatus())){
                    tv_isOnline.setText(getString(R.string.map_fault));
                    tv_isOnline.setBackgroundResource(R.drawable.shape_map_bad);
                }
                tv_deviceTime.setText(getString(R.string.map_install_time) + device.getInstallTime().substring(0,10));
                tv_deviceAddress.setText(device.getDeviceName());
                if("0".equals(device.getDeviceStatus())){
                    videoView.setClickable(true);
                    videoView.setEnabled(true);
                    historyView.setClickable(true);
                    historyView.setEnabled(true);
                    galleryView.setClickable(true);
                    galleryView.setEnabled(true);
                    iv_video.setImageResource(R.drawable.time);
                    iv_history.setImageResource(R.drawable.history);
                    iv_gallery.setImageResource(R.drawable.capture);
                    tv_video.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_gallery.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    videoView.setClickable(false);
                    videoView.setEnabled(false);
                    historyView.setClickable(true);
                    historyView.setEnabled(true);
                    galleryView.setClickable(false);
                    galleryView.setEnabled(false);
                    iv_video.setImageResource(R.drawable.timedisable);
                    iv_history.setImageResource(R.drawable.history);
                    iv_gallery.setImageResource(R.drawable.capturedisable);
                    tv_video.setTextColor(getResources().getColor(R.color.gray_9999));
                    tv_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_gallery.setTextColor(getResources().getColor(R.color.gray_9999));
                }
                initLocation(new LatLng(Double.parseDouble(device.getLatitude()),Double.parseDouble(device.getLongitude())));
            } else if(type == TYPE_ENVIRONMENT){
                DataQueryVoBean device = (DataQueryVoBean) marker.getObject();
                currentEnvirDevice = device;
                tv_deviceNumber.setText(device.getDeviceCoding());
                if(0 == device.getDeviceStatus()){
                    tv_isOnline.setText(getString(R.string.map_online));
                    tv_isOnline.setBackgroundResource(R.drawable.shape_map_online);
                } else if(1 == device.getDeviceStatus()){
                    tv_isOnline.setText(getString(R.string.map_offline));
                    tv_isOnline.setBackgroundResource(R.drawable.shape_offline);
                } else if(2 == device.getDeviceStatus()){
                    tv_isOnline.setText(getString(R.string.map_fault));
                    tv_isOnline.setBackgroundResource(R.drawable.shape_map_bad);
                }
                tv_deviceTime.setText( getString(R.string.map_install_time) + device.getInstallTime().substring(0,10));
                tv_deviceAddress.setText(device.getDeviceName());
                if(0 == device.getDeviceStatus()){
                    videoView.setClickable(true);
                    videoView.setEnabled(true);
                    historyView.setClickable(true);
                    historyView.setEnabled(true);
                    galleryView.setClickable(true);
                    galleryView.setEnabled(true);
                    iv_video.setImageResource(R.drawable.time);
                    iv_history.setImageResource(R.drawable.history);
                    iv_gallery.setImageResource(R.drawable.capture);
                    tv_video.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_gallery.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    videoView.setClickable(false);
                    videoView.setEnabled(false);
                    historyView.setClickable(true);
                    historyView.setEnabled(true);
                    galleryView.setClickable(false);
                    galleryView.setEnabled(false);
                    iv_video.setImageResource(R.drawable.timedisable);
                    iv_history.setImageResource(R.drawable.history);
                    iv_gallery.setImageResource(R.drawable.capturedisable);
                    tv_video.setTextColor(getResources().getColor(R.color.gray_9999));
                    tv_history.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_gallery.setTextColor(getResources().getColor(R.color.gray_9999));
                }

                String pm10 = "";
                double d_pm10 = device.getPm10() == null ? 0 : device.getPm10();
                int pm_10 = (int) d_pm10;

                double d_pm25 = device.getPm2_5() == null ? 0 : device.getPm2_5();
                int pm_25 = (int) d_pm25;

                double d_co2 = device.getCo2() == null ? 0 : device.getCo2();
                int pm_co2 = (int) d_co2;

                if(pm_10 < 50){
                    pm10 = "PM10：<font color='" + COLOR_50 + "'>" + pm_10 + "</font>";
                } else if(pm_10 < 150){
                    pm10 = "PM10：<font color='" + COLOR_150 + "'>" + pm_10 + "</font>";
                } else if(pm_10 < 250){
                    pm10 = "PM10：<font color='" + COLOR_250 + "'>" + pm_10 + "</font>";
                } else if(pm_10 < 350){
                    pm10 = "PM10：<font color='" + COLOR_350 + "'>" + pm_10 + "</font>";
                } else if(pm_10 < 420){
                    pm10 = "PM10：<font color='" + COLOR_420 + "'>" + pm_10 + "</font>";
                } else {
                    pm10 = "PM10：<font color='" + COLOR_600 + "'>" + pm_10 + "</font>";
                }

                tv_pm10.setText(Html.fromHtml(pm10));
                String pm25 = "PM2.5：<font color='" + COLOR_0 + "'>" + pm_25 + "</font>";
                tv_pm25.setText(Html.fromHtml(pm25));
                String co2 = "CO2：<font color='" + COLOR_0 + "'>" + pm_co2 + "</font>";
                tv_pmco2.setText(Html.fromHtml(co2));
                initLocation(new LatLng(Double.parseDouble(device.getLatitude()),Double.parseDouble(device.getLongitude())));
            }
            addAndRemoveRoundMarker();

            mPopWindow.showAtLocation(mMapView, Gravity.BOTTOM,0,DensityUtils.dip2px(this,-8));
        }
        return true;
    }

    public void addRoundLine(){
        List<LatLng> latLngs = MapUtils.getAroundLatlons();
        if(aMap != null){
            Polyline polyline = aMap.addPolyline(new PolylineOptions().
                    addAll(latLngs).width(10).color(Color.parseColor("#3464dd")));
        }
    }


}
