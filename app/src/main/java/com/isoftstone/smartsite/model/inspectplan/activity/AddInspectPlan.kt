package com.isoftstone.smartsite.model.inspectplan.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.model.LatLng
import com.google.gson.Gson
import com.isoftstone.smartsite.R
import com.isoftstone.smartsite.base.BaseActivity
import com.isoftstone.smartsite.http.HttpPost
import com.isoftstone.smartsite.http.patroltask.PatrolPositionBean
import com.isoftstone.smartsite.http.patroltask.PatrolTaskBean
import com.isoftstone.smartsite.http.user.BaseUserBean
import com.isoftstone.smartsite.model.dirtcar.View.MyFlowLayout
import com.isoftstone.smartsite.model.inspectplan.adapter.PeopleAdapter
import com.isoftstone.smartsite.model.map.ui.MapSearchTaskPositionActivity
import com.isoftstone.smartsite.utils.DateUtils
import com.isoftstone.smartsite.utils.ToastUtils
import com.isoftstone.smartsite.widgets.CustomDatePicker
import kotlinx.android.synthetic.main.activity_add_inspect_plan.*
import kotlinx.android.synthetic.main.view_input_inspect_time.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by yanyongjun on 2017/11/15.
 */
open class AddInspectPlan : BaseActivity() {
    var mAddressList = ArrayList<PatrolPositionBean>()

    var mGridViewPeople: GridView? = null
    var mPeopleList = ArrayList<BaseUserBean>()
    var mAdapterPeople: PeopleAdapter? = null
    var mEditName: EditText? = null
    var title: TextView? = null;
    private var mWaittingAdd: Drawable? = null
    private var mWattingChanged: Drawable? = null

    var labPeople: TextView? = null

    var labTimeLeft: TextView? = null
    var labBeginTimeRight: TextView? = null
    var labEndTimeRight: TextView? = null

    var edit_report_msg: EditText? = null
    var lab_address_choose_left: TextView? = null
    val FLAG_TARGET_ADDRESS = 0
    val FLAG_TARGET_PEOPLE = 1
    var mTaskType = 0
    var  patrolTaskBean:PatrolTaskBean?=null
    var flow_layout_address: MyFlowLayout? = null
    var mHttpPost = HttpPost()

    var taskTimeStart = ""
    var taskTimeEnd = ""

    override fun getLayoutRes(): Int {
        return R.layout.activity_add_inspect_plan;
    }

    override fun afterCreated(savedInstanceState: Bundle?) {

        mWaittingAdd = resources.getDrawable(R.drawable.addcolumn)
        mWaittingAdd?.setBounds(0, 0, mWaittingAdd?.getIntrinsicWidth()!!, mWaittingAdd?.getIntrinsicHeight()!!)
        mWattingChanged = resources.getDrawable(R.drawable.editcolumn)
        mWattingChanged?.setBounds(0, 0, mWattingChanged?.getIntrinsicWidth()!!, mWattingChanged?.getIntrinsicHeight()!!)

        mAdapterPeople = PeopleAdapter(this, mPeopleList)
        try {
            mTaskType = intent.getIntExtra("taskType", 0)
            patrolTaskBean= intent.getSerializableExtra("patrolTaskBean") as PatrolTaskBean?
            taskTimeStart = intent.getStringExtra("taskTimeStart")
            taskTimeEnd = intent.getStringExtra("taskTimeEnd")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initEditName()
        initBeginTime()
        initEndTime()
        initMsg()
        initAddressGridView()
        initPeopleGridView()
        if (patrolTaskBean!=null){
            initData(patrolTaskBean);
        }
        queryTask(intent.getLongExtra("taskId", -1))
    }

     fun initData(patrolTaskBean: PatrolTaskBean?) {
       title=findViewById(R.id.lab_title) as TextView
         title!!.setText(R.string.task_title)
         mEditName!!.setText(patrolTaskBean!!.taskName)
         edit_report_msg!!.setText(patrolTaskBean!!.taskContent)
         var beginTime=DateUtils.format_yyyy_MM_dd_HH_mm_ss.format(DateUtils.format_yyyy_MM_dd_HH_mm.parse(patrolTaskBean!!.taskTimeStart))
         var endTime=DateUtils.format_yyyy_MM_dd_HH_mm_ss.format(DateUtils.format_yyyy_MM_dd_HH_mm.parse(patrolTaskBean!!.taskTimeEnd))
         labBeginTimeRight!!.setText(beginTime)
         labEndTimeRight!!.setText(endTime)
         labBeginTimeRight?.setTextColor(resources.getColor(R.color.main_text_color))
         labEndTimeRight?.setTextColor(resources.getColor(R.color.main_text_color))
         mAddressList.clear();
         mAddressList=patrolTaskBean!!.patrolPositions
         addAddressView();
         var peopleList = patrolTaskBean!!.users as? ArrayList<BaseUserBean>
         if (peopleList != null) {
             mPeopleList.clear()
             mPeopleList.addAll(peopleList)
             Log.e(TAG, "yanlog result:" + mPeopleList.size)
             mAdapterPeople?.notifyDataSetChanged()
         }
     }

    fun initEditName() {
        mEditName = findViewById(R.id.edit_name) as EditText
        var labName = findViewById(R.id.lab_name) as TextView
        mEditName?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length != 0) {
                    labName.setCompoundDrawables(mWattingChanged, null, null, null)
                } else {
                    labName.setCompoundDrawables(mWaittingAdd, null, null, null)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    fun initBeginTime() {
        labTimeLeft = findViewById(R.id.lab_inspect_report_time) as TextView
        labBeginTimeRight = findViewById(R.id.lab_begin_time) as TextView
        labBeginTimeRight?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                showDatePickerDialog(labBeginTimeRight, labTimeLeft, taskTimeStart)
            }
        })
    }

    fun initEndTime() {
        labEndTimeRight = findViewById(R.id.lab_end_time) as TextView
        labEndTimeRight?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                showDatePickerDialog(labEndTimeRight, labTimeLeft, taskTimeEnd)
            }
        })
    }

    fun initMsg() {
        val lab_report_msg = findViewById(R.id.lab_report_msg) as TextView
        edit_report_msg = findViewById(R.id.edit_report_msg) as EditText
        edit_report_msg?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length != 0) {
                    lab_report_msg.setCompoundDrawables(mWattingChanged, null, null, null)
                } else {
                    lab_report_msg.setCompoundDrawables(mWaittingAdd, null, null, null)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

    }

    fun initPeopleGridView() {
        var lab_people_right = findViewById(R.id.lab_people_right) as TextView
        lab_people_right.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var i = Intent(this@AddInspectPlan, SelectInspectorsActivity::class.java)
                i.action = "action"
                i.putExtra("list", mPeopleList)
                startActivityForResult(i, FLAG_TARGET_PEOPLE)
            }
        })

        labPeople = findViewById(R.id.lab_people_left) as TextView
        mGridViewPeople = findViewById(R.id.grid_view_people) as GridView
        mGridViewPeople?.adapter = mAdapterPeople
    }

    fun initAddressGridView() {
        flow_layout_address = findViewById(R.id.flow_layout_address) as MyFlowLayout
        val lab_address_choose_right = findViewById(R.id.lab_address_choose_right) as TextView
        lab_address_choose_left = findViewById(R.id.lab_address_choose_left) as TextView

        lab_address_choose_right.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var i = Intent(this@AddInspectPlan, MapSearchTaskPositionActivity::class.java)
                startActivityForResult(i, FLAG_TARGET_ADDRESS)
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FLAG_TARGET_ADDRESS) {
            var address = data?.getStringExtra("latLngsNameJson")
            var latlan = data?.getStringExtra("latLngsJson")
            Log.e(TAG, "yanlog latlan:" + latlan)
            if (address != null) {
                var gson = Gson()
                var tempAddress: ArrayList<String> = gson.fromJson<ArrayList<String>>(address, ArrayList::class.java)
                //var tempLatlng : ArrayList<LatLng> = gson.fromJson<ArrayList<LatLng>>(latlan,ArrayList<LatLng::class.java>::class.java)
                var tempLatlng = data?.getParcelableArrayListExtra<LatLng>("latLngs")
                for (temp in tempAddress) {
                    var loc = tempAddress.indexOf(temp)
                    var result = PatrolPositionBean()
                    Log.e(TAG, "yanlog lati:" + tempLatlng!!.get(loc).latitude + " long:" + tempLatlng.get(loc).longitude)
                    result.latitude = tempLatlng.get(loc).latitude
                    result.longitude = tempLatlng.get(loc).longitude
                    result.position = temp
                    mAddressList.add(result)
                }
                //mAddressList.addAll(result)
                if (mAddressList.size > 0) {
                    lab_address_choose_left?.setCompoundDrawables(mWattingChanged, null, null, null)
                }
                addAddressView()
            }
        } else if (requestCode == FLAG_TARGET_PEOPLE) {
            var peopleList = data?.getSerializableExtra("list") as? ArrayList<BaseUserBean>
            if (peopleList != null) {
                mPeopleList.clear()
                mPeopleList.addAll(peopleList)
                Log.e(TAG, "yanlog result:" + mPeopleList.size)
                mAdapterPeople?.notifyDataSetChanged()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    /**
     * 新增选择之后的地点
     */
    fun addAddressView() {
        flow_layout_address?.removeAllViews()
        for (str in mAddressList) {
            var v = LayoutInflater.from(this@AddInspectPlan).inflate(R.layout.listview_add_inspect_plan_address_item, null)
            var textView = v.findViewById(R.id.lab_address) as TextView
            textView.setText(str.position)

            var imgDelete = v.findViewById(R.id.img_delete) as ImageView
            imgDelete.setOnClickListener(object : View.OnClickListener {
                override fun onClick(tempV: View?) {
                    Log.e(TAG, "yanlog imageDelte view")
                    flow_layout_address?.removeView(v)
                    mAddressList.remove(str)
                }
            })
            flow_layout_address?.addView(v)
        }
    }

    fun isOkTime(str: String?): Boolean {
        try {
            DateUtils.format_yyyy_MM_dd_HH_mm_ss.parse(str)
        } catch (e: Exception) {
            return false;
        }
        return true
    }

    fun queryTask(id: Long) {
        if (id == -1L) {
            return
        }
        val tempId = id
        var query = object : AsyncTask<Void, Void, PatrolTaskBean>() {
            override fun doInBackground(vararg params: Void?): PatrolTaskBean {
                var bean = mHttpPost.patrolTaskFindOne(tempId)
                return bean
            }

            override fun onPostExecute(bean: PatrolTaskBean?) {
                try {
                    if (bean != null) {
                        edit_name.setText(bean.taskName)
                        mAddressList.clear()
                        mAddressList.addAll(bean.patrolPositions)

                        mPeopleList.clear()
                        mPeopleList.addAll(bean.users)

                        lab_begin_time.setText(DateUtils.format_yyyy_MM_dd_HH_mm_ss.format(DateUtils.format_yyyy_MM_dd_HH_mm.parse(bean.taskTimeStart)))
                        lab_end_time.setText(DateUtils.format_yyyy_MM_dd_HH_mm_ss.format(DateUtils.format_yyyy_MM_dd_HH_mm.parse(bean.taskTimeEnd)))
                        labEndTimeRight?.setTextColor(resources.getColor(R.color.main_text_color))
                        labBeginTimeRight?.setTextColor(resources.getColor(R.color.main_text_color))
                        edit_report_msg?.setText(bean.taskContent)

                        mAdapterPeople?.notifyDataSetChanged()
                        addAddressView()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        query.execute()
    }

    fun onClick_submit(v: View) {
        var  submit = object :  AsyncTask<Void, Void, Boolean>() {
            override fun onPreExecute() {
                this@AddInspectPlan.showDlg(getString(R.string.submit))
                super.onPreExecute()
            }

            override fun doInBackground(vararg params: Void?): Boolean {
                try {
                    var name = mEditName?.text?.toString()
                    var addList = mAddressList
                    var beginTime = labBeginTimeRight?.text?.toString()
                    var endTime = labEndTimeRight?.text?.toString()
                    var content = edit_report_msg?.text?.toString()

//                    var peopleList = ArrayList<BaseUserBean>()
//                    var bean_1 = BaseUserBean()
//                    bean_1.id = HttpPost.mLoginBean.getmUserBean().loginUser.getId() //TODO
                    //peopleList.add(bean_1)

                    var peopleTempList = ArrayList<BaseUserBean>()
                    Log.e(TAG, "yanlog submit people size:" + mPeopleList.size)
                    for (temp in mPeopleList) {
                        var people = BaseUserBean()
                        people.id = temp.id
                        peopleTempList.add(people)
                    }
                    Log.e(TAG, "yanlog peopleTempList size:" + peopleTempList.size)

                    if (TextUtils.isEmpty(name) || !isOkTime(beginTime) || !isOkTime(endTime) ||
                            TextUtils.isEmpty(content) || addList.size == 0 || peopleTempList.size == 0) {
                        Log.e(TAG, "yanlog error 1:" + name + " " + beginTime + " " + endTime + " " + content)
                        return false
                    }

                    //ensure the begin time is earlier than the end time
                    try {
                        val beginDate = DateUtils.format_yyyy_MM_dd_HH_mm_ss.parse(beginTime)
                        val endDate = DateUtils.format_yyyy_MM_dd_HH_mm_ss.parse(endTime)
                        if (beginDate.after(endDate)) {
                            Log.e(TAG,"yanlog the begintime mash be earlier than the end time")
                            return false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                        for(bean in addList){
                            bean.bitmap = null
                            bean.executionTime = null
                            bean.user = null
                            bean.id = 0
                            bean.status = 0
                        }

                    var planBean = PatrolTaskBean()
                    if(patrolTaskBean!=null){
                        planBean.taskId=patrolTaskBean!!.taskId
                    }
                    planBean.taskName = name
                    planBean.patrolPositions = addList
                    planBean.taskTimeStart = beginTime
                    planBean.taskTimeEnd = endTime
                    planBean.taskContent = content
                    planBean.users = peopleTempList
                    planBean.taskType = mTaskType


                    var result = mHttpPost.patrolTaskSave(planBean)
                    if (result == null) {
                        Log.e(TAG, "yanlog error 2")
                        return false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "yanlog error 3")
                    return false
                }
                return true
            }

            override fun onPostExecute(result: Boolean) {
                this@AddInspectPlan.closeDlg()
                if (result) {
                    ToastUtils.showShort(getString(R.string.submit_success))
                    finish()
                } else {
                    ToastUtils.showShort(getString(R.string.re_submit))
                }
            }
        }
//        submit.execute()
        submit.executeOnExecutor(Executors.newCachedThreadPool())
    }

    fun showDatePickerDialog(editRight: TextView?, labLeft: TextView?, defautTime: String) {

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        val now = sdf.format(Date())

        val customDatePicker = CustomDatePicker(this, CustomDatePicker.ResultHandler { time ->
            // 回调接口，获得选中的时间
            try {
                editRight?.text = DateUtils.format_yyyy_MM_dd_HH_mm_ss.format(DateUtils.format_yyyy_MM_dd_HH_mm.parse(time))
                labLeft?.setCompoundDrawables(mWattingChanged, null, null, null)
                editRight?.setTextColor(resources.getColor(R.color.main_text_color))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, "1970-01-01 00:00", "2099-12-12 00:00") // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        customDatePicker.showSpecificTime(true) // 不显示时和分
        //customDatePicker.showYearMonth();
        customDatePicker.setIsLoop(false) // 不允许循环滚动
        //customDatePicker.show(dateText.getText().toString() + " " + timeText.getText().toString());
        var defTime = DateUtils.format_yyyy_MM_dd_HH_mm.format(Date())
        try {
            defTime = DateUtils.format_yyyy_MM_dd_HH_mm.format(DateUtils.format1.parse(defautTime))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        customDatePicker.show(defTime)
    }
}
