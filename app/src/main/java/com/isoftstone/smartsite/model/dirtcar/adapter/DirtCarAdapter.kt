package com.isoftstone.smartsite.model.dirtcar.adapter

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.isoftstone.smartsite.R
import com.isoftstone.smartsite.http.muckcar.BayonetGrabInfoBean
import com.isoftstone.smartsite.model.dirtcar.activity.ManualPhotographyActivity
import com.isoftstone.smartsite.model.map.ui.MapTrackHistoryActivity
import com.isoftstone.smartsite.utils.DateUtils

/**
 * Created by yanyongjun on 2017/11/14.
 */
open class DirtCarAdapter(context: Context, datas: ArrayList<BayonetGrabInfoBean>) : BaseAdapter() {
    var mDatas: ArrayList<BayonetGrabInfoBean> = datas
    var mContext: Context = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var v = if (convertView != null) convertView else {
            LayoutInflater.from(mContext).inflate(R.layout.listview_dirtcar_item, null)
        }
        var bean = mDatas.get(position)

        var vCamera = v.findViewById(R.id.linear_camera)
        vCamera.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var i = Intent(mContext, ManualPhotographyActivity::class.java)
                i.putExtra("licence",bean.licence)
                mContext.startActivity(i)
            }
        })

        var vHistory = v.findViewById(R.id.linear_history)
        vHistory.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //ToastUtils.showShort("历史轨迹")
                var i = Intent(mContext,MapTrackHistoryActivity::class.java)
                var time = DateUtils.format1.format(DateUtils.format_yyyy_MM_dd_HH_mm_ss.parse(bean.dateTime) )
                i.putExtra("licence",bean.licence)
                i.putExtra("time",time);
                mContext.startActivity(i)
            }
        })

        var relativeAddress = v.findViewById(R.id.relative_address)
        relativeAddress.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //ToastUtils.showShort("历史轨迹1")
                var i = Intent(mContext,MapTrackHistoryActivity::class.java)
                i.putExtra("licence",bean.licence)
                var time = DateUtils.format1.format(DateUtils.format_yyyy_MM_dd_HH_mm_ss.parse(bean.dateTime) )
                i.putExtra("time",time);
                mContext.startActivity(i)
                //跳转到MapTrackHistoryActivity，参数传licence就行了,intent.putExtra("licence",licence);
            }
        })


        var license = v.findViewById(R.id.lab_license) as TextView
        license.setText(bean.licence)

        var time = v.findViewById(R.id.lab_time) as TextView
        var format_time = DateUtils.format1.format(DateUtils.format_yyyy_MM_dd_HH_mm_ss.parse(bean.dateTime))
        time.setText(format_time)

        var address = v.findViewById(R.id.lab_address) as TextView
        if (TextUtils.isEmpty(bean.addr)) {
            address.setText(R.string.unknow_address)
        } else {
            address.setText(bean.addr)
        }


        return v
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0;
    }

    override fun getCount(): Int {
        return mDatas.size
        //TODO
    }
}