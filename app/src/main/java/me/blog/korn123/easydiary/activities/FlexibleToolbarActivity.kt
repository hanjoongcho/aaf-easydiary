package me.blog.korn123.easydiary.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.AbsListView
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.ksoichiro.android.observablescrollview.ObservableListView
import io.github.aafactory.commons.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_flexible_toolbar.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryMainItemAdapter
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.*


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class FlexibleToolbarActivity : ToolbarControlBaseActivity<ObservableListView>() {

    private var mRecognizerIntent: Intent? = null

    private var mCurrentTimeMillis: Long = 0

    private var mDiaryMainItemAdapter: DiaryMainItemAdapter? = null

    private var mDiaryList: MutableList<DiaryDto>? = null

    private var mShowcaseIndex = 0

    private var mShowcaseView: ShowcaseView? = null
    
    override fun getLayoutResId(): Int {
        return R.layout.activity_flexible_toolbar
    }
    
    override fun createScrollable(): ObservableListView {
//        setDummyData(diaryList, 50)

        // ObservableListView uses setOnScrollListener, but it still works.
        diaryList.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            }
        })
        return diaryList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.run {
            title = getString(R.string.read_diary_title)
        }
        
        mDiaryList = EasyDiaryDbHelper.readDiary(null)
        mDiaryList?.let {
            mDiaryMainItemAdapter = DiaryMainItemAdapter(this, R.layout.item_diary_main, it)
        }
        diaryList.adapter = mDiaryMainItemAdapter
        
        bindEvent()
        migrateData()
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_main, menu)
        return true
    }

    private fun bindEvent() {
        modalContainer.setOnTouchListener { _, _ -> true }
    }

    private fun migrateData() {
        Thread(Runnable {
            val listPhotoUri = EasyDiaryDbHelper.selectPhotoUriAll()
            for ((index, dto) in listPhotoUri.withIndex()) {
//                Log.i("PHOTO-URI", dto.photoUri)
                if (dto.isContentUri()) {
                    val photoPath = Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY + UUID.randomUUID().toString()
                    CommonUtils.uriToFile(this, Uri.parse(dto.photoUri), photoPath)
                    EasyDiaryDbHelper.getInstance().beginTransaction()
                    dto.photoUri = FILE_URI_PREFIX + photoPath
                    EasyDiaryDbHelper.getInstance().commitTransaction()
                    runOnUiThread({
                        progressInfo.text = "Converting... ($index/${listPhotoUri.size})"
                    })
                }
            }

            if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                File(Environment.getExternalStorageDirectory().absolutePath + WORKING_DIRECTORY).listFiles()?.let {
                    it.forEach { file ->
                        if (file.extension.equals("jpg", true)) FileUtils.moveFileToDirectory(file, File(Environment.getExternalStorageDirectory().absolutePath + DIARY_POSTCARD_DIRECTORY), true)
                    }
                }
            }

            runOnUiThread({
                progressDialog.visibility = View.GONE
                modalContainer.visibility = View.GONE
            })
        }).start()
    }
//    protected fun setDummyData(listView: ListView, num: Int) {
//        listView.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, getDummyData(num)))
//    }
//
//    fun getDummyData(num: Int): ArrayList<String> {
//        val items = ArrayList<String>()
//        for (i in 1..num) {
//            items.add("Item $i")
//        }
//        return items
//    }
}
