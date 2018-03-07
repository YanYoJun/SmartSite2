package com.isoftstone.smartsite.model.dirtcar.activity

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.isoftstone.smartsite.R
import com.isoftstone.smartsite.common.AppManager
import com.isoftstone.smartsite.http.HttpPost
import com.isoftstone.smartsite.http.muckcar.BayonetGrabInfoBean
import com.isoftstone.smartsite.model.dirtcar.Service.RecognizeDirtCarService
import com.isoftstone.smartsite.utils.ImageUtils

/**
 * Created by yanyongjun on 2017/11/21.
 */
open class RecognizeDirtCarActivity : AppCompatActivity() {
    var mHttpPost: HttpPost = HttpPost()
    val TAG = "RecognizeDirtCar"
    var mLicense = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppManager.getAppManager().addToActivities(this)

        setContentView(R.layout.activity_recognize_dirt_car)
        if(HttpPost.mLoginBean == null || !HttpPost.mLoginBean.isLoginSuccess){
            finish()
        }
        try {
            var gsonData = intent.getStringExtra("data")
            var gson = Gson()

            var bean = gson.fromJson<BayonetGrabInfoBean>(gsonData, BayonetGrabInfoBean::class.java)
            Log.e(TAG, "yanlog onCreate:" + bean)
            var lab_license = findViewById(R.id.lab_license) as TextView
            if (TextUtils.isEmpty(bean.licence)) {
                lab_license.setText(R.string.unknow)
            } else {
                mLicense = bean.licence
                lab_license.setText(bean.licence)
            }

            var lab_address = findViewById(R.id.lab_address) as TextView
            if (TextUtils.isEmpty(bean.addr)) {
                lab_address.setText(R.string.unknow)
            } else {
                lab_address.setText(bean.addr)
            }

            var lab_time = findViewById(R.id.lab_time) as TextView
            if (TextUtils.isEmpty(bean.dateTime)) {
                lab_time.setText(R.string.unknow)
            } else {
                lab_time.setText(bean.dateTime)
            }

            var lab_speed = findViewById(R.id.lab_speed) as TextView
            lab_speed.setText("" + bean.speed+" km/h")

            var imgUri = bean.imgList;
            var imageView = findViewById(R.id.img_view) as ImageView
            ImageUtils.loadImageWithPlaceHolderLoading(this, imageView, imgUri, R.drawable.timg)
//            var imagePath = intent.getStringExtra("path")
//            Log.e(TAG, "yanlog imagePath:" + imagePath)
//            if (!TextUtils.isEmpty(imagePath)) {
//                var imageView = findViewById(R.id.img_view) as ImageView
//                ImageUtils.loadImageViewDiskCache(this, imagePath, imageView)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onClick_yes(v: View) {
        markResult(1)
    }

    fun onClick_no(v: View) {
        markResult(0)
    }

    fun onClick_cannotReg(v:View){
        markResult(2)
    }

    override fun onDestroy() {
        var i = Intent(this, RecognizeDirtCarService::class.java)
        i.putExtra("sync", false)
        startService(i)
        super.onDestroy()
    }

    fun markResult(result: Int) {
        val temp = result
        var query = object : AsyncTask<Void, Void, Boolean>() {
            override fun doInBackground(vararg params: Void?): Boolean {
                if (!TextUtils.isEmpty(mLicense)) {
                    mHttpPost.recForMobile(mLicense, temp)
                }
                return true
            }

            override fun onPostExecute(result: Boolean?) {
                super.onPostExecute(result)
                finish()
            }
        }
        query.execute()
    }
}
