package com.isoftstone.smartsite.model.dirtcar.activity

import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.isoftstone.smartsite.R
import com.isoftstone.smartsite.base.BaseActivity
import com.isoftstone.smartsite.common.widget.PullToRefreshListView
import com.isoftstone.smartsite.http.HttpPost
import com.isoftstone.smartsite.http.muckcar.BayonetGrabInfoBean
import com.isoftstone.smartsite.http.pageable.PageableBean
import com.isoftstone.smartsite.model.dirtcar.adapter.DirtCarAdapter
import com.isoftstone.smartsite.utils.ToastUtils

/**
 * Created by yanyongjun on 2017/11/14.
 */
class DirtCarListActivity : BaseActivity() {
    var mListView: PullToRefreshListView? = null
    var mList: ArrayList<BayonetGrabInfoBean> = ArrayList<BayonetGrabInfoBean>()
    var mAdapter: DirtCarAdapter = DirtCarAdapter(this, mList)
    var mHttpPost: HttpPost = HttpPost()

    var mIsUIInSearchMode = false
    var mIsDataInSearchMode = false

    //分页开始
    private var mCurPageNum = -1
    var isLoading = false

    override fun getLayoutRes(): Int {
        return R.layout.activity_dircarlist;
    }

    override fun afterCreated(savedInstanceState: Bundle?) {
        initListView()
        initSearchView()
        if (!mIsUIInSearchMode) {
            queryData("",true)
        } else {
            val search_edit_text = findViewById(R.id.search_edit_text) as EditText
            queryData(search_edit_text.text.toString(),true)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun initSearchView() {
        var toolbar_search = findViewById(R.id.toolbar_search)
        var toolbar_default = findViewById(R.id.toolbar_default)
        val cancel_search = findViewById(R.id.search_btn_icon_right) as TextView
        val search_edit_text = findViewById(R.id.search_edit_text) as EditText
        var btn_search = findViewById(R.id.btn_search)
        var search_btn_back = findViewById(R.id.search_btn_back)

        if (mIsUIInSearchMode) {
            enterSearchMode()
        } else {
            exitSearchMode()
        }


        btn_search.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                enterSearchMode()
            }
        })


        cancel_search.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (TextUtils.isEmpty(search_edit_text.text.toString())) {
                    exitSearchMode()
                } else {
                    queryData(search_edit_text.text.toString(),true)
                }
            }
        })


        search_btn_back.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                exitSearchMode()
            }
        })


        search_edit_text.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (count == 0) {
                            cancel_search.setText(R.string.cancel)
                        } else {
                            cancel_search.setText(R.string.serach)
                        }
                    }
                })
    }

    fun enterSearchMode() {
        var toolbar_search = findViewById(R.id.toolbar_search)
        var toolbar_default = findViewById(R.id.toolbar_default)
        val cancel_search = findViewById(R.id.search_btn_icon_right) as TextView
        val search_edit_text = findViewById(R.id.search_edit_text) as EditText
        var btn_search = findViewById(R.id.btn_search)
        var search_btn_back = findViewById(R.id.search_btn_back)
        toolbar_default.visibility = View.INVISIBLE
        toolbar_search.visibility = View.VISIBLE
        mIsUIInSearchMode = true
        search_edit_text.setText("")
        cancel_search.setText(R.string.cancel)
    }

    fun exitSearchMode() {
        var toolbar_search = findViewById(R.id.toolbar_search)
        var toolbar_default = findViewById(R.id.toolbar_default)
        val cancel_search = findViewById(R.id.search_btn_icon_right) as TextView
        val search_edit_text = findViewById(R.id.search_edit_text) as EditText
        var btn_search = findViewById(R.id.btn_search)
        var search_btn_back = findViewById(R.id.search_btn_back)
        toolbar_default.visibility = View.VISIBLE
        toolbar_search.visibility = View.INVISIBLE
        mIsUIInSearchMode = false
        if (mIsDataInSearchMode) {
            queryData("",true)
        }

    }

    fun initListView() {
        mListView = findViewById(R.id.listview) as PullToRefreshListView
        val search_edit_text = findViewById(R.id.search_edit_text) as EditText
        val listener = object : PullToRefreshListView.OnRefreshListener {
            override fun onRefresh() {
                if (isLoading) {
                    //mListView.onRefreshComplete();
                } else {
                    mCurPageNum = -1
                    //QueryMsgTask(true).execute()
                    if (mIsUIInSearchMode) {
                        queryData(search_edit_text.text.toString(),true)
                    } else {
                        queryData("",true)
                    }
                }
            }

            override fun onLoadMore() {
                if (isLoading) {
                    // mListView.onLoadMoreComplete();
                } else {
                    //QueryMsgTask(false).execute()
                    if (mIsUIInSearchMode) {
                        queryData(search_edit_text.text.toString(),false)
                    } else {
                        queryData("",false)
                    }
                }
            }
        }
        mListView?.setOnRefreshListener(listener)
        mListView?.adapter = mAdapter
    }

    fun queryData(licence: String, isReload: Boolean) {
        if(isReload){
            mCurPageNum = -1
        }
        var query = object : AsyncTask<Void, Void, Boolean>() {
            override fun onPreExecute() {
                isLoading = true
                //this@DirtCarListActivity.showDlg("正在获取列表")
                super.onPreExecute()
            }

            override fun doInBackground(vararg params: Void?): Boolean? {
                var bean = PageableBean()
                bean.setPage((mCurPageNum + 1).toString())
                bean.setSize(BaseActivity.DEFAULT_PAGE_SIZE)
                var result = mHttpPost.getTrackList(licence, bean)
                if (result == null || result.rawRecords== null || result.rawRecords.size == 0) {
                    return false
                }
                if (isReload) {
                    mList.clear()
                }
                if (result != null && result.content != null && result.content.size > 0) {
                    mCurPageNum++
                }
                mList.addAll(result.rawRecords)

                if (TextUtils.isEmpty(licence)) {
                    mIsDataInSearchMode = false
                } else {
                    mIsDataInSearchMode = true
                }
                return true
            }

            override fun onPostExecute(result: Boolean?) {
                super.onPostExecute(result)
                isLoading = false
                //this@DirtCarListActivity.closeDlg()
                var temp = if (result == null) false else result
                mListView?.onLoadMoreComplete()
                mListView?.onRefreshComplete()
                if (temp) {
                    mAdapter.notifyDataSetChanged()
                } else {
                    ToastUtils.showLong(getString(R.string.no_data))
                }
            }
        }
        query.execute()
    }

}