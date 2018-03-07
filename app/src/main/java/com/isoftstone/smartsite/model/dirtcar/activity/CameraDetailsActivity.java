package com.isoftstone.smartsite.model.dirtcar.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.base.BaseActivity;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.muckcar.EvidencePhotoBean;
import com.isoftstone.smartsite.http.user.BaseUserBean;
import com.isoftstone.smartsite.model.dirtcar.adapter.CameraDetailsAdapter;
import com.isoftstone.smartsite.model.dirtcar.adapter.ManualPhotographyAdapter;
import com.isoftstone.smartsite.model.dirtcar.adapter.PhotoGridAdapter;
import com.isoftstone.smartsite.model.dirtcar.bean.ManualPhotographyBean;
import com.isoftstone.smartsite.model.dirtcar.imagecache.ImageLoader;
import com.isoftstone.smartsite.model.system.ui.ActionSheetDialog;
import com.isoftstone.smartsite.model.system.ui.PhoneInfoUtils;
import com.isoftstone.smartsite.model.system.ui.SystemFragment;
import com.isoftstone.smartsite.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zhangyinfu on 2017/11/16.
 */

public class CameraDetailsActivity extends BaseActivity  implements View.OnClickListener {

	protected static final String TAG = "CameraDetailsActivity";
	/** Called when the activity is first created. */
	private CameraDetailsAdapter mAdapter;
	private ArrayList<String> mListDate = new ArrayList<String>();
	private Context mContext;
	private ListView mPhotoGridView;
	private HttpPost mHttpPost;

	private TextView mLicenceView;
	private TextView mDeviceCodingView;
	private TextView mTakePhotoTimeView;
	private TextView mDeviceAddressView;

	private String mLicence; //车牌号
	private String mDeviceCoding; //设备编号
	private String mTakePhotoTime; //拍摄日期
	private String mDeviceAddress; //设备地址

	private static final int  HANDLER_CAMERA_DETAILS_START = 1;
	private static  final int  HANDLER_CAMERA_DETAILS_END = 2;

	/* 查询请求识别码 查询成功*/
	private static final int QUERY_RESULTS_SUCCESSFUL_CODE = 1;
	/* 查询请求识别码 查询失败*/
	private static final int QUERY_RESULTS_FAILED_CODE = 2;
	/* 查询请求识别码 查询异常*/
	private static final int QUERY_RESULTS_EXCEPTION_CODE = 3;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case HANDLER_CAMERA_DETAILS_START: {
					Thread thread = new Thread(){
						@Override
						public void run() {
							//mListDate =  mHttpPost.getDevices("1","","",""); getPhontoList
							ArrayList<EvidencePhotoBean> arrayList = mHttpPost.getPhontoList("鄂A46F52","video","2017-11-17","");
							Log.i("zzz","CCCCCCCCC   arrayList = " + arrayList);
							if (arrayList != null) {
								StringBuffer stringBuffer = new StringBuffer();
								for (int i=0; i< arrayList.size(); i++) {
									Log.i("zzz","arrayList.size() = " + arrayList.size() + "  & " + i + "  && " + arrayList.get(i).toString());
									String urlStr = arrayList.get(i).getSmallPhotoSrc();
									if (urlStr == null) {
										urlStr = arrayList.get(i).getPhotoSrc();
									}

									/**if (urlStr != null) {
										String[] urlsStr = urlStr.split(",");
										for (int j=0; j<urlsStr.length; j++) {
											if (j == urlsStr.length -1) {
												stringBuffer.append(mHttpPost.getFileUrl(urlsStr[j]));
											} else {
												stringBuffer.append(mHttpPost.getFileUrl(urlsStr[j]) + ",");
											}
										}
									}*/

									if (urlStr != null) {
										if (i == arrayList.size() -1) {
											stringBuffer.append(mHttpPost.getFileUrl(urlStr));
										} else {
											stringBuffer.append(mHttpPost.getFileUrl(urlStr) + ",");
										}
									}


									BaseUserBean baseUserBean = arrayList.get(i).getTakePhoroUser();
									if (baseUserBean != null) {

									} else {

									}
								}
								Log.i("zzz","CCCCCCC  stringBuffer = " + stringBuffer.toString());
								mListDate.add(stringBuffer.toString());
							}
							//mListDate.add(photoSrc);
							mHandler.sendEmptyMessage(HANDLER_CAMERA_DETAILS_END);
						}
					};
					thread.start();
				}
				break;
				case HANDLER_CAMERA_DETAILS_END:{
					setListViewData();
				}
				break;
			}
		}
	};

	private void setListViewData() {
		setupViews();
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_camera_details;
	}

	@Override
	protected void afterCreated(Bundle bundle) {
		/*获取Intent中的Bundle对象*/
		if(bundle == null) {
			bundle = this.getIntent().getExtras();
		}
        /*获取Bundle中的数据，注意类型和key*/
		mLicence = bundle.getString("licence");
		mDeviceCoding = bundle.getString("device_coding");
		mTakePhotoTime = bundle.getString("date");
		mDeviceAddress = bundle.getString("device_address");

		initToolbar();
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();

		queryDataTask updateTextTask = new queryDataTask(this);
		updateTextTask.execute();
	}

	private void initView() {
		mContext = getApplicationContext();
		mHttpPost = new HttpPost();

		mLicenceView = (TextView) findViewById(R.id.licence_plate_view);
		mDeviceCodingView = (TextView) findViewById(R.id.device_coding);;
		mTakePhotoTimeView = (TextView) findViewById(R.id.lab_time);;
		mDeviceAddressView = (TextView) findViewById(R.id.device_address);;

		mLicenceView.setText(mLicence);
		mDeviceCodingView.setText(mDeviceCoding);
		mTakePhotoTimeView.setText(mTakePhotoTime);
		mDeviceAddressView.setText(mDeviceAddress);
		//mHandler.sendEmptyMessage(HANDLER_CAMERA_DETAILS_START);
	}

	private void setupViews() {
		mPhotoGridView = (ListView) findViewById(R.id.gridview_photo_list);
		mAdapter = new CameraDetailsAdapter(mContext, mListDate, CameraDetailsActivity.this);
		mPhotoGridView.setAdapter(mAdapter);
		mPhotoGridView.setOnScrollListener(mScrollListener);
	}

	OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_FLING:
				//mAdapter.setFlagBusy(true);
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:
				//mAdapter.setFlagBusy(false);
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				//mAdapter.setFlagBusy(false);
				break;
			default:
				break;
			}
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}
	};


	@Override
	protected void onDestroy() {

		ImageLoader imageLoader = mAdapter.getImageLoader();
		if (imageLoader != null){
			imageLoader.clearCache();
		}

		super.onDestroy();
	}

	private void initToolbar(){
		TextView tv_title = (TextView) findViewById(R.id.toolbar_title);
		tv_title.setText(R.string.camera_details_title);

		findViewById(R.id.btn_back).setOnClickListener(CameraDetailsActivity.this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.btn_back:
				CameraDetailsActivity.this.finish();
				break;
			default:
				break;
		}
	}

	class queryDataTask extends AsyncTask<Void,Void,Integer> {
		private Context context;
		queryDataTask(Context context) {
			this.context = context;
		}

		/**
		 * 运行在UI线程中，在调用doInBackground()之前执行
		 */
		@Override
		protected void onPreExecute() {
			//Toast.makeText(context,"开始执行",Toast.LENGTH_SHORT).show();
			//if (mListView != null) {
			//    mListView.setAdapter(null);
			//}
			showDlg(getText(R.string.dialog_load_messgae).toString());
		}
		/**
		 * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法
		 */
		@Override
		protected Integer doInBackground(Void... params) {

			if(mListDate != null) {
				mListDate.clear();
			}

			try {
				//ArrayList<EvidencePhotoBean> arrayList = mHttpPost.getPhontoList("鄂A46F52","video","2017-11-17","");
				ArrayList<EvidencePhotoBean> arrayList = mHttpPost.getPhontoList(mLicence,"video", mTakePhotoTime, mDeviceCoding);
				Log.i("zzz","CCCCCCCCC   arrayList = " + arrayList);
				if (arrayList == null || arrayList.size() == 0) {
					return QUERY_RESULTS_FAILED_CODE;
				}
					StringBuffer stringBuffer = new StringBuffer();
					for (int i=0; i< arrayList.size(); i++) {
						Log.i("zzz","arrayList.size() = " + arrayList.size() + "  & " + i + "  && " + arrayList.get(i).toString());
						String urlStr = arrayList.get(i).getSmallPhotoSrc();
						if (urlStr == null) {
							urlStr = arrayList.get(i).getPhotoSrc();
						}

						if (urlStr != null) {
							/**if (i == arrayList.size() -1) {
								stringBuffer.append(mHttpPost.getFileUrl(urlStr));
							} else {
								stringBuffer.append(mHttpPost.getFileUrl(urlStr) + ",");
							}*/
							stringBuffer.append(urlStr);
						}
					}
					Log.i("zzz","CCCCCCC  stringBuffer = " + stringBuffer.toString());
					mListDate.add(stringBuffer.toString());

			} catch (Exception e) {
				Log.e(TAG,"e : " + e.getMessage());
				return QUERY_RESULTS_EXCEPTION_CODE;
			}

			return QUERY_RESULTS_SUCCESSFUL_CODE;
		}

		/**
		 * 运行在ui线程中，在doInBackground()执行完毕后执行
		 */
		@Override
		protected void onPostExecute(Integer resultsCode) {
			super.onPostExecute(resultsCode);
			closeDlg();
			//Toast.makeText(context,"执行完毕",Toast.LENGTH_SHORT).show();
			if (resultsCode == QUERY_RESULTS_SUCCESSFUL_CODE) {
				setListViewData();
			} else if (resultsCode == QUERY_RESULTS_FAILED_CODE){
				ToastUtils.showLong(getString(R.string.device_list_null));
			} else if (resultsCode == QUERY_RESULTS_EXCEPTION_CODE) {
				ToastUtils.showLong(getString(R.string.device_list_failure));
			}
		}

		/**
		 * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
	}


	private static final String photoSrc = "http://f.hiphotos.baidu.com/image/pic/item/b219ebc4b74543a9c7b57f0617178a82b8011449.jpg" +
			",https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510765861321&di=7c603a9f41935d8051e35cdbce4fe154&imgtype=0&src=http%3A%2F%2Fc11.eoemarket.com%2Fapp0%2F119%2F119986%2Fscreen%2F1985845.png" +
			",http://f.hiphotos.baidu.com/image/pic/item/b219ebc4b74543a9c7b57f0617178a82b8011449.jpg" +
			",http://f.hiphotos.baidu.com/image/pic/item/b219ebc4b74543a9c7b57f0617178a82b8011449.jpg" +
			",https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510765861322&di=1384fe7d8c1fdba219ab0439cc45402b&imgtype=0&src=http%3A%2F%2Fh.hiphotos.baidu.com%2Fzhidao%2Fpic%2Fitem%2F7aec54e736d12f2e3e656ddd4ac2d5628535682f.jpg";


}
