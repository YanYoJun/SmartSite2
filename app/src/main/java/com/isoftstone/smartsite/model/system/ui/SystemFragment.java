package com.isoftstone.smartsite.model.system.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.isoftstone.smartsite.LoginActivity;
import com.isoftstone.smartsite.R;
import com.isoftstone.smartsite.UserUtils;
import com.isoftstone.smartsite.base.BaseFragment;
import com.isoftstone.smartsite.http.HttpPost;
import com.isoftstone.smartsite.http.UserBean;
import com.isoftstone.smartsite.utils.ImageUtils;
import com.isoftstone.smartsite.utils.ToastUtils;
import com.isoftstone.smartsite.utils.Utils;


/**
 * Created by zyf on 2017/10/15 8:00.
 */

public class SystemFragment extends BaseFragment{

    private LinearLayout mLinearLayout;//用户头像父节点LL
    private ImageView mHeadImageView;//用户头像IV
    private TextView mUserNameView;//用户名
    private TextView mUserAutographView;//签名信息
    String picPath;//头像路径
    public static final String HEAD_IMAGE_NAME = "user_head.jpg";
    private LinearLayout mIndividualCenterLayout;//个人中心Btn
    private LinearLayout mAboutUsLayout;//关于我们Btn
    private LinearLayout mLogOffLayout;//退出Btn
    private LinearLayout mOpinionFeedbackLayout;//意见反馈
    private LinearLayout mSettingsLayout;//设置

    private Fragment mCurrentFrame;
    private HttpPost mHttpPost = null;

    private static final  int HANDLER_GET_USER_INFO = 1;
    private Handler mHandler = null;
    private TextView mAppVersionView;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_system_new;
    }

    @Override
    protected void afterCreated(Bundle savedInstanceState) {
        init();
    }

    /**
     * 实例化
     */
    private void init() {
        mHttpPost = new HttpPost();
        mHandler = new Handler();

        picPath = Environment.getExternalStorageDirectory() + "/smartsite/";
        mHeadImageView = (ImageView) rootView.findViewById(R.id.head_iv);
        mUserNameView = (TextView)  rootView.findViewById(R.id.user_name_tv);
        mUserAutographView = (TextView)  rootView.findViewById(R.id.user_autograph_tv);
        mIndividualCenterLayout = (LinearLayout) rootView.findViewById(R.id.individual_center);
        mAboutUsLayout = (LinearLayout) rootView.findViewById(R.id.about_us);
        mLogOffLayout = (LinearLayout) rootView.findViewById(R.id.log_off);
        mOpinionFeedbackLayout = (LinearLayout) rootView.findViewById(R.id.opinion_feedback);
        mSettingsLayout = (LinearLayout) rootView.findViewById(R.id.settings);

        registerLinearLayoutOnClickListener();
        mCurrentFrame = SystemFragment.this;

        mAppVersionView = (TextView) rootView.findViewById(R.id.app_version);
        mAppVersionView.setText("Version " + Utils.getAppVersionName(getActivity().getPackageManager(), getActivity().getPackageName()));
    }

    private void registerLinearLayoutOnClickListener() {

        mIndividualCenterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment individualCenterFrame = new IndividualCenterFragment();
                changeToAnotherFragment(individualCenterFrame);
            }
        });

        mAboutUsLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, AboutUsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } );

        mLogOffLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    UserUtils.clearUserList(getActivity().getBaseContext());
                    Intent intent = new Intent();
                    intent.setClass(getActivity().getBaseContext(), LoginActivity.class);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mOpinionFeedbackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, OpinionFeedbackActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mSettingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(){
            @Override
            public void run() {
                UserBean userBean = mHttpPost.getLoginUser();
                MyThread myThread = new MyThread(userBean);
                mHandler.post(myThread);
            }
        }.start();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void changeToAnotherFragment(Fragment toFragment){
        //如果是不是用的v4的包，则用getActivity().getFragmentManager();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        //注意v4包的配套使用
        if(mCurrentFrame != toFragment) {
            if(!toFragment.isAdded()) {
                transaction.hide(mCurrentFrame).add(R.id.fl_system_content, toFragment).commitAllowingStateLoss();
            } else{
                transaction.hide(mCurrentFrame).show(toFragment).commitAllowingStateLoss();
            }
            mCurrentFrame = toFragment;
        }
    }

    private void initUserInfo(UserBean userBean) {
        if (null == userBean) {
            return;
        }
        Log.i("zyf", userBean.toString());
        String userName = userBean.getName();
        mUserNameView.setText(userName);
        String userAutograph = userBean.getDescription();
        if (null == userAutograph || "null".equals(userAutograph)) {
            mUserAutographView.setVisibility(View.INVISIBLE);
        } else {
            mUserAutographView.setText(userAutograph);
        }

        //Bitmap headBitmap = BitmapFactory.decodeFile(picPath + userBean.getImageData());
        //if (null != headBitmap) {
        //    mHeadImageView.setImageBitmap(headBitmap);
        //} else {
        //    mHeadImageView.setImageResource(R.drawable.default_head);
        //}
        String urlString = mHttpPost.getFileUrl(userBean.getImageData());
        //ToastUtils.showShort("system urlString = " + urlString);
        //String urlstr = "http://g.hiphotos.baidu.com/zhidao/wh%3D600%2C800/sign=edebdc82f91986184112e7827add024b/b812c8fcc3cec3fda2f3fe96d788d43f86942707.jpg";
        ImageUtils.loadImageWithPlaceHolder(mContext, mHeadImageView, urlString, R.drawable.default_head);
        mHeadImageView.invalidate();
    }

    class MyThread implements Runnable {

        private UserBean userBean;
        private MyThread (UserBean userBean) {
            this.userBean = userBean;
        }
        @Override
        public void run() {
            initUserInfo(userBean);
        }
    }
}
