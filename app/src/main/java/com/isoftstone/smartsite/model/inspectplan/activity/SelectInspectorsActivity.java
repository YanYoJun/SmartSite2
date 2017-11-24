package com.isoftstone.smartsite.model.inspectplan.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.gjiazhe.wavesidebar.WaveSideBar;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.PatrolBean;
import com.isoftstone.smartsite.model.inspectplan.adapter.InspectorsAdapter;
import com.isoftstone.smartsite.model.inspectplan.adapter.InspectorsIconAdapter;
import com.isoftstone.smartsite.model.inspectplan.data.InspectorData;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017-11-24.
 */

public class SelectInspectorsActivity extends BaseActivity {
    public ArrayList<InspectorData> list = null;
    public InspectorData contactDate;
    private ListView listView_Contact;
    private ListView listView_Icon;
    public InspectorsAdapter contactAdapter;
    public InspectorsIconAdapter iconAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDate();
        initSideBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayoutRes() {
        return 0;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {

    }

    @Override
    public void openActivity(Class<?> activity, Bundle bundle) {
        super.openActivity(activity, bundle);
    }

    @Override
    public void onBackBtnClick(View v) {
        super.onBackBtnClick(v);
    }

    @Override
    public PatrolBean getReportData() {
        return super.getReportData();
    }

    @Override
    public void showDlg(String text) {
        super.showDlg(text);
    }

    @Override
    public void closeDlg() {
        super.closeDlg();
    }

    public void initDate() {
        list = new ArrayList<InspectorData>();
        for (int i = 0; i < 50; i++) {
            contactDate = new InspectorData();
            if (i < 10) {
                contactDate.setSort("A");
            } else if (i < 20) {
                contactDate.setSort("B");
            } else if (i < 30) {
                contactDate.setSort("C");
            } else if (i < 40) {
                contactDate.setSort("D");
            } else if (i < 50) {
                contactDate.setSort("F");
            }
            contactDate.setSelected(true);
            contactDate.setContactName("姓名" + String.valueOf(i));
            if (i % 10 == 0) {
                contactDate.setVisible(View.VISIBLE);
            } else {
                contactDate.setVisible(View.GONE);
            }
            list.add(contactDate);
        }
        contactAdapter = new InspectorsAdapter(this, list);
        listView_Contact.setAdapter(contactAdapter);
        iconAdapter = new InspectorsIconAdapter(this, list);
        listView_Icon.setAdapter(iconAdapter);
    }

    public void initSideBar() {
        WaveSideBar sideBar = (WaveSideBar) findViewById(R.id.side_bar);
        sideBar.setIndexItems("↑","☆", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#");
        sideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {
            @Override
            public void onSelectIndexItem(String index) {
                Log.d("WaveSideBar", index);
                // Do something here ....
                for (int i = 0; i < list.size(); i++){
                    if (list.get(i).getSort().equals(index)){
                        listView_Contact.smoothScrollToPosition(i);
                        contactAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
        });
    }
}
