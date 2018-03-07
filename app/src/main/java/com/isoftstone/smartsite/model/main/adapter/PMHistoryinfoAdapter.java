package com.isoftstone.smartsite.model.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.http.aqi.DataQueryVoBean;

import java.util.ArrayList;

/**
 * Created by gone on 2017/10/16.
 * modifed by zhangyinfu on 2017/10/19
 * modifed by zhangyinfu on 2017/10/20
 * modifed by zhangyinfu on 2017/10/21
 */

public class PMHistoryinfoAdapter extends BaseAdapter {


    private LayoutInflater mInflater;
    private ArrayList<DataQueryVoBean> mData = new ArrayList<DataQueryVoBean>();
    private Context mContext = null;

    public PMHistoryinfoAdapter(Context context){
        this.mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void setData(ArrayList<DataQueryVoBean> list){
        mData = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.pmhistoryinfo_adapter, null);
            holder.time = (TextView)convertView.findViewById(R.id.text_1);
            holder.PM10 = (TextView)convertView.findViewById(R.id.text_2);
            holder.PM25 = (TextView)convertView.findViewById(R.id.text_3);
            holder.SO2 = (TextView)convertView.findViewById(R.id.text_4);
            holder.NO2 = (TextView)convertView.findViewById(R.id.text_5);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.time.setText(R.string.update_time+mData.get(position).getPushTime());
        holder.PM10.setText("PM10 "+String.format("%.1f",(mData.get(position).getPm10())));
        holder.PM25.setText("PM2.5 "+String.format("%.1f",(mData.get(position).getPm2_5())));
        holder.SO2.setText("CO2 "+String.format("%.1f",(mData.get(position).getCo2())));
        holder.NO2.setText("NO2 ");
        return convertView;
    }


    public final class ViewHolder{
        public TextView time;//资源名称
        public TextView  PM10;//PM10
        public TextView  PM25;//PM2.5
        public TextView  SO2;//SO2
        public TextView  NO2;//NO2
    }


}
