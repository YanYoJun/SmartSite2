package com.isoftstone.smartsite.model.dirtcar.activity

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import com.google.gson.Gson
import com.isoftstone.smartsite.R
import com.isoftstone.smartsite.base.BaseActivity
import com.isoftstone.smartsite.http.HttpPost
import com.isoftstone.smartsite.http.muckcar.UpdatePhotoInfoBean
import com.isoftstone.smartsite.http.user.SimpleUserBean
import com.isoftstone.smartsite.model.dirtcar.adapter.UpdatePhotoAdapter
import com.isoftstone.smartsite.model.map.ui.MapSearchTaskPositionActivity
import com.isoftstone.smartsite.model.system.ui.ActionSheetDialog
import com.isoftstone.smartsite.utils.DateUtils
import com.isoftstone.smartsite.utils.FilesUtils
import com.isoftstone.smartsite.utils.SPUtils
import com.isoftstone.smartsite.utils.ToastUtils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


/**
 * 拍照上传界面
 * Created by yanyongjun on 2017/11/19.
 */
open class UpdatePhotoActivity : BaseActivity() {
    var mGridView: GridView? = null //照片列表
    var mGridViewAdapter: UpdatePhotoAdapter? = null
    //var mUriList: ArrayList<Uri>? = null
    var mPathList: ArrayList<String> = ArrayList<String>()

    var mStoragePath = Environment.getExternalStorageDirectory().toString() + "/isoftstone/Camera";
    var mUriImage: Uri? = null
    var mCameraImage: File? = null
    var mLabAddress: TextView? = null
    var mLocationService: LocationManager? = null

    val SP_KEY_HAS_DRAFT = "has_camera_draft"
    val SP_KEY_DRAFT_IMAGE_NUM = "draft_camera_num"
    val SP_KEY_DRAFT_IMAGE = "draft_camera_image_"
    val SP_KEY_LOCATION = "camera_location"
    var mAddress: String = ""
    public val TARGET = "target_flag"
    public val FLAG_TARGET_CAMERA = 1
    public val FLAG_TARGET_GALLARY = 2
    val FLAG_TARGET_ADDRESS = 3
    var mHttpPost = HttpPost()
    var mLicence = "" //TODO

    override fun getLayoutRes(): Int {
        return R.layout.activity_update_photo
    }

    override fun afterCreated(savedInstanceState: Bundle?) {
        initGridView()
        initLocationLab()
        mLicence = intent.getStringExtra("licence")
        //read_sp()
        //if (!SPUtils.getBoolean(SP_KEY_HAS_DRAFT, false)) {
        var flag = intent.getIntExtra("target_flag", 1)
        if (flag == 1) {
            startCamera()
        } else {
            startGallary()
        }

    }

    fun read_sp() {
        var num = SPUtils.getInt(SP_KEY_DRAFT_IMAGE_NUM, 0)
        for (i in 0..num - 1) {
            var uri = SPUtils.getString(SP_KEY_DRAFT_IMAGE + i, "")
            //mUriList.add(Uri.parse(uri))
        }
        mAddress = SPUtils.getString(SP_KEY_LOCATION, "")
        if (!TextUtils.isEmpty(mAddress)) {
            mLabAddress?.setText(mAddress)
        }
        mGridViewAdapter?.notifyDataSetChanged()
    }

    fun onClick_submit(v: View) {
        submit()
    }

    fun save() {
        //if (mUriList.size == 0) {
        //   ToastUtils.showShort("没有图片可以保存")
        //    return
        //  }
        // var num = mUriList.size
//        SPUtils.saveInt(SP_KEY_DRAFT_IMAGE_NUM, num)
//        for (i in 0..num - 1) {
//            SPUtils.saveString(SP_KEY_DRAFT_IMAGE + i, mUriList.get(i).toString())
//        }
//        SPUtils.saveString(SP_KEY_LOCATION, mAddress)
//        SPUtils.saveBoolean(SP_KEY_HAS_DRAFT, true)
//        if(!TextUtils.isEmpty(address)){
//            mAddress = address
//        }
    }

    override fun onStop() {
        //save()
        super.onStop()
    }

    fun initGridView() {
        mGridView = findViewById(R.id.grid_view) as GridView
        mGridViewAdapter = UpdatePhotoAdapter(this, mPathList)
        mGridView?.adapter = mGridViewAdapter

        mGridView?.setOnItemClickListener(AdapterView.OnItemClickListener(fun(parent, v, pos, id) {
            if (pos == mPathList.size) {
//                startCamera()
                ActionSheetDialog(this@UpdatePhotoActivity)
                        .builder(false)
                        .setCancelable(true)
                        .setCanceledOnTouchOutside(true)
                        .addSheetItem(this@UpdatePhotoActivity.getText(R.string.camera).toString(),
                                ActionSheetDialog.SheetItemColor.Blue
                        ) {
                            //choseHeadImageFromGallery();
                            startCamera()
                        }
                        .addSheetItem(this@UpdatePhotoActivity.getText(R.string.album).toString(),
                                ActionSheetDialog.SheetItemColor.Blue,
                                object : ActionSheetDialog.OnSheetItemClickListener {

                                    override fun onClick(which: Int) {
                                        //choseHeadImageFromCameraCapture();
                                        startGallary()
                                    }
                                }).show()
            } else {
//                val intent = FilesUtils.getImageFileIntent(this@UpdatePhotoActivity, mPathList.get(pos))
//                startActivity(intent)
            }
        }))
    }

    fun startCamera() {
        var i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var fileName = DateUtils.format_file_name.format(Date()) + ".jpg"
        var path = File(mStoragePath)
        if (!path.exists()) {
            path.mkdirs()
        }
        mCameraImage = File(path, fileName)
        mUriImage = FilesUtils.getUriForFile(this@UpdatePhotoActivity, mCameraImage?.path)
        i.putExtra(MediaStore.EXTRA_OUTPUT, mUriImage)
        i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        Log.e(TAG,"yanlog i.flags" + i.flags)
        val uri = mUriImage;
        if (uri != null) {
            mUriImage = uri
        }
        startActivityForResult(i, FLAG_TARGET_CAMERA)
    }

    fun startGallary() {
        var i = Intent(this, SelectImageActivity::class.java)
        i.putExtra("selected_data", mPathList)
        startActivityForResult(i, FLAG_TARGET_GALLARY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e(TAG, "onActivityResult:" + requestCode)
        var i = Intent()
        if (requestCode == FLAG_TARGET_CAMERA) {
            val uri = mUriImage
            if (uri != null && isTakePicOk(mCameraImage?.canonicalPath)) {
                //var path = FilesUtils.getPath(this, mCameraImage?.canonicalPath)
                var path = mCameraImage?.canonicalPath
                if (path != null) {
                    mPathList.add(path)
                }
                mGridViewAdapter?.notifyDataSetChanged()
            } else if (mPathList.size == 0) {
                setResult(Activity.RESULT_CANCELED, i)
                finish()
                Log.e(TAG, "file size == null")
            }
            mUriImage = null
            mCameraImage = null
        } else if (requestCode == FLAG_TARGET_GALLARY) {
            if (resultCode == Activity.RESULT_OK) {
                var pathlist = data?.getSerializableExtra("data") as HashSet<String>
                mPathList.clear()
                for (path in pathlist) {
                    mPathList.add(path)
                    mGridViewAdapter?.notifyDataSetChanged()
                }
            } else if (mPathList.size == 0) {
                setResult(Activity.RESULT_CANCELED, i)
                finish()
            }
        } else if (requestCode == FLAG_TARGET_ADDRESS) {
            var address = data?.getStringExtra("latLngsNameJson")
            Log.e(TAG, "yanlog return address:" + address)
            if (address != null) {
                var gson = Gson()
                var temp: ArrayList<String> = gson.fromJson<ArrayList<String>>(address, ArrayList::class.java)
                if (temp != null && temp.size > 0) {
                    mAddress = temp.get(0)
                }
            }
            if (!TextUtils.isEmpty(mAddress)) {
                mLabAddress?.setText(mAddress)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun isTakePicOk(uri: Uri): Boolean {
        var cursor: Cursor ?= null
        try {
            cursor = contentResolver.query(uri, null, null, null, null);
            val sizeCol = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            val size = cursor.getInt(sizeCol)
            Log.e(TAG, "file size:" + size)
            if (size <= 0) {
                return false;
            }
            return true;
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            cursor?.close()
        }
        return true
    }

    fun isTakePicOk(str: String?): Boolean {
        if (str == null) {
            return false
        }
        var file = File(str)
        return file.exists()
    }

    fun initLocationLab() {
        mLabAddress = findViewById(R.id.lab_address) as TextView
        mLocationService = this.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    }

    fun onClick_address(v: View) {
        var i = Intent(this, MapSearchTaskPositionActivity::class.java)
        i.putExtra("zhuapai",100)
        startActivityForResult(i, FLAG_TARGET_ADDRESS)
        //mLocationService?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100L, 0.1F, mLocationListener, null)
    }

    var mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            var geocoder: Geocoder = Geocoder(this@UpdatePhotoActivity)
            if (location != null) {
                var list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (list != null) {
                    mAddress = list.get(0).getAddressLine(0)
                    mLabAddress?.setText(mAddress)
                }
            }
            mLocationService?.removeUpdates(this)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }
    }

    fun submit() {
        var query = object : AsyncTask<Void, Void, Boolean>() {
            override fun onPreExecute() {
                this@UpdatePhotoActivity.showDlg(getString(R.string.submit_now))
                super.onPreExecute()
            }

            override fun doInBackground(vararg params: Void?): Boolean {
                var result = mHttpPost.uploadPhotos(mPathList)
                Log.e(TAG, "yanlog update result:" + result)
                if (result != null && result.startsWith("\"") && result.endsWith("\"")) {
                    result = result.substring(1, result.length - 1)
                }
                var bean = UpdatePhotoInfoBean()
                bean.licence = mLicence
                bean.addr = mAddress
                bean.photoSrc = result
                bean.takePhotoTime = DateUtils.format_yyyy_MM_dd_HH_mm_ss.format(Date())
                var user = SimpleUserBean()
                //userBean.setId(mHttpPost.mLoginBean.getmUserBean().getLoginUser().getId());
                //user.id = mHttpPost.loginUser.loginUser.id
                user.id = HttpPost.mLoginBean.getmUserBean().loginUser.getId()
                bean.takePhoroUser = user
                Log.e(TAG, "yanlog updateBean:" + bean);
                var updateResult = mHttpPost.addPhoto(bean)
                Log.e(TAG, "yanlog updateResult:" + updateResult)


//                mHttpPost.loginUser
//                val evidencePhotoBean = UpdatePhotoInfoBean()
//                evidencePhotoBean.addr = "湖北武汉"
//                evidencePhotoBean.licence = "鄂AV785B"
//                evidencePhotoBean.photoSrc = "upload/track/images/20171117/20171117143934776.jpg"
//                evidencePhotoBean.takePhotoTime = "2017-11-18 11:11:11"
//                val simpleUserBean = SimpleUserBean()
//                simpleUserBean.id = HttpPost.mLoginBean.getmUserBean().loginUser.getId()!!
//                evidencePhotoBean.takePhoroUser = simpleUserBean
//                mHttpPost.addPhoto(evidencePhotoBean)
                return updateResult != null
            }

            override fun onPostExecute(result: Boolean) {
                this@UpdatePhotoActivity.closeDlg()
                if (result) {
                    ToastUtils.showShort(getString(R.string.submit_success))
                    var i = Intent()
                    setResult(Activity.RESULT_OK, i)
                    finish()
                } else {
                    ToastUtils.showShort(getString(R.string.submit_failed))
                }

            }
        }
        query.execute()
    }
}
