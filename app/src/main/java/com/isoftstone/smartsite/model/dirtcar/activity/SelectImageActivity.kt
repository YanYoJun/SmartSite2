package com.isoftstone.smartsite.model.dirtcar.activity

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.TextView
import com.isoftstone.smartsite.R
import com.isoftstone.smartsite.base.BaseActivity
import com.isoftstone.smartsite.model.dirtcar.Data.SelectImage
import com.isoftstone.smartsite.model.dirtcar.adapter.SelectImageAdapter
import java.io.File
import java.util.*
import kotlin.collections.HashSet


/**
 * Created by yanyongjun on 2017/11/19.
 */
open class SelectImageActivity : BaseActivity() {
    var mGridView: GridView? = null
    var mAdapter: SelectImageAdapter? = null
    var mDataList = ArrayList<SelectImage>()
    var mRootDir = HashSet<String>()
    var mLabSelectedNum: TextView? = null
    var mSelectPaths = HashSet<String>() //选中图片的保存路径，这个应该是绝对路径

    var mSelectedNum: Int = 0
    var mListener = object : SelectImage.OnStatusChangeListener {
        override fun onChange(status: Boolean, path: String) {
            if (status) {
                if (mSelectPaths.add(path)) {
                    mSelectedNum++
                }
            } else {
                if (mSelectPaths.remove(path)) {
                    mSelectedNum--
                }

            }
            if (mSelectedNum <= 0) {
                mSelectedNum = 0
                mLabSelectedNum?.setText(R.string.submit)
            }
            if (mSelectedNum > 0) {
                mLabSelectedNum?.setText("提交（$mSelectedNum）")
            }
        }
    }

    init {
        mRootDir.add(Environment.getExternalStorageDirectory().toString() + "/isoftstone/Camera")
        var sd1 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Camera")
        var sdfather = File(Environment.getExternalStorageDirectory().toString() + "/../..")
        var pathList = sdfather.list()
        if (pathList != null) {
            for (temp in pathList) {
                var file = File(sdfather.canonicalPath + "/" + temp)
                if (!(file.name.equals("..") || file.name.equals("."))) {
                    var filetemp = File(file.canonicalPath + "/DCIM/Camera/")
                    if (filetemp.exists()) {
                        mRootDir.add(filetemp.canonicalPath)
                    }
                }
            }
        }

        if (sd1.exists()) {
            mRootDir.add(sd1.canonicalPath)
        }

        for (temp in mRootDir) {
            Log.e(TAG, "sync image dir:" + temp);
        }
    }


    override fun getLayoutRes(): Int {
        return R.layout.activity_select_image
    }

    override fun afterCreated(savedInstanceState: Bundle?) {
        mLabSelectedNum = findViewById(R.id.btn_submit) as TextView
        initGridView()
    }

    fun getSelectNum(): Int {
        return mSelectedNum
    }

    fun initGridView() {
        var list = intent.getSerializableExtra("selected_data") as ArrayList<String>
        if (list != null) {
            for (temp in list) {
                mSelectPaths.add(temp)
                mSelectedNum++
            }
        }
        if (mSelectedNum <= 0) {
            mSelectedNum = 0
            mLabSelectedNum?.setText(R.string.submit)
        }
        if (mSelectedNum > 0) {
            mLabSelectedNum?.setText("提交（$mSelectedNum）")
        }

        mGridView = findViewById(R.id.grid_view) as GridView
        mAdapter = SelectImageAdapter(this, mDataList)
        mGridView?.adapter = mAdapter
        query()
    }

    fun onClick_submit(v: View) {
        //TODO
        var i = Intent()
        if (mSelectPaths.size > 0) {
            i.putExtra("data", mSelectPaths)
            setResult(Activity.RESULT_OK, i)
        } else {
            setResult(Activity.RESULT_CANCELED, i)
        }
        finish()
    }
    fun query() {
        var mInitResTask = object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                mDataList.clear()
                for (path in mRootDir) {
                    Log.e(TAG, "yanlog rootPath:" + path)
                    var stack = Stack<File>()
                    stack.push(File(path))
                    while (!stack.isEmpty()) {
                        var curFile = stack.pop()
                        if (!curFile.exists()) {
                            continue
                        }
                        if (curFile.isFile && (curFile.path.endsWith(".jpg") || curFile.path.endsWith(".jpeg") || curFile.path.endsWith(".png") ||
                                curFile.path.endsWith(".JPG") || curFile.path.endsWith(".JPEG") || curFile.path.endsWith(".PNG"))) {
                            if (mSelectPaths.contains(curFile.canonicalPath)) {
                                mDataList.add(SelectImage(curFile.canonicalPath, true, mListener))
                            } else {
                                mDataList.add(SelectImage(curFile.canonicalPath, false, mListener))
                            }
                        } else if (curFile.isDirectory) {
                            var files = curFile.listFiles()
                            for (file in files) {
                                if (!(file.name.equals(".") || file.name.equals(".."))) {
                                    stack.push(file)
                                }
                            }
                        }
                    }
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                mAdapter?.notifyDataSetChanged()
                super.onPostExecute(result)
            }
        }
        mInitResTask.execute()
    }


}
