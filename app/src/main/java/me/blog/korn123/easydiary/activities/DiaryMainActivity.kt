package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.RelativeLayout
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_diary_main.*
import me.blog.korn123.commons.utils.CommonUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryMainItemAdapter
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryMainActivity : EasyDiaryActivity() {
    private var mRecognizerIntent: Intent? = null

    private var mCurrentTimeMillis: Long = 0

    private var mDiaryMainItemAdapter: DiaryMainItemAdapter? = null

    private var mDiaryList: MutableList<DiaryDto>? = null

    private var mShowcaseIndex = 0

    private var mShowcaseView: ShowcaseView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_main)

        // android marshmallow minor version bug workaround
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            Realm.init(this)
        }

        // application finish 확인
        if (intent.getBooleanExtra(APP_FINISH_FLAG, false)) {
            finish()
        }
        
        mCurrentTimeMillis = System.currentTimeMillis()
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.read_diary_title)
        }

        mRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        mDiaryList = EasyDiaryDbHelper.readDiary(null)
        mDiaryList?.let {
            mDiaryMainItemAdapter = DiaryMainItemAdapter(this, R.layout.item_diary_main, it)
        }
        diaryList.adapter = mDiaryMainItemAdapter

        if (!config.isInitDummyData) {
            initSampleData()
            config.isInitDummyData = true
        }

        bindEvent()
        initShowcase()
        EasyDiaryUtils.initWorkingDirectory(Environment.getExternalStorageDirectory().absolutePath + USER_CUSTOM_FONTS_DIRECTORY)
        EasyDiaryUtils.initWorkingDirectory(Environment.getExternalStorageDirectory().absolutePath + DIARY_PHOTO_DIRECTORY)
        
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
            runOnUiThread({
                progressDialog.visibility = View.GONE
                modalContainer.visibility = View.GONE
            })
        }).start()
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        refreshList()
        initTextSize(progressDialog, this)
        
        val previousActivity = config.previousActivity
        if (previousActivity == PREVIOUS_ACTIVITY_CREATE) {
            diaryList.smoothScrollToPosition(0)
            //            mDiaryListView.setSelection(0);
            config.previousActivity = -1
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    query.setText(result[0])
                    query.setSelection(result[0].length)
                }
                config.aafPinLockPauseMillis = System.currentTimeMillis()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.settings -> {
                val settingIntent = Intent(this@DiaryMainActivity, SettingsActivity::class.java)
                //                startActivity(settingIntent);
                TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, settingIntent)
            }
            R.id.chart -> {
                val chartIntent = Intent(this@DiaryMainActivity, BarChartActivity::class.java)
                //                startActivity(chartIntent);
                TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, chartIntent)
            }
            R.id.timeline -> {
                val timelineIntent = Intent(this@DiaryMainActivity, TimelineActivity::class.java)
                //                startActivity(timelineIntent);
                TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, timelineIntent)
            }
            R.id.planner -> {
                val calendarIntent = Intent(this@DiaryMainActivity, CalendarActivity::class.java)
                //                startActivity(calendarIntent);
                TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, calendarIntent)
            }
            R.id.microphone -> showSpeechDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_main, menu)
        return true
    }

    override fun onBackPressed() {
        if (progressDialog.visibility == View.GONE) ActivityCompat.finishAffinity(this@DiaryMainActivity)
    }

    private fun initShowcase() {
        val margin = ((resources.displayMetrics.density * 12) as Number).toInt()

        val centerParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        centerParams.setMargins(0, 0, 0, margin)

        val leftParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        leftParams.setMargins(margin, margin, margin, margin)
        val showcaseViewOnClickListener = View.OnClickListener {
            mShowcaseView?.run {
                when (mShowcaseIndex) {
                    0 -> {
                        setButtonPosition(centerParams)
                        setShowcase(ViewTarget(query), true)
                        setContentTitle(getString(R.string.read_diary_showcase_title_2))
                        setContentText(getString(R.string.read_diary_showcase_message_2))
                    }
                    1 -> {
                        setButtonPosition(centerParams)
                        setShowcase(ViewTarget(diaryList), true)
                        setContentTitle(getString(R.string.read_diary_showcase_title_8))
                        setContentText(getString(R.string.read_diary_showcase_message_8))
                    }
                    2 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.planner, this@DiaryMainActivity))
                        setContentTitle(getString(R.string.read_diary_showcase_title_4))
                        setContentText(getString(R.string.read_diary_showcase_message_4))
                    }
                    3 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.timeline, this@DiaryMainActivity))
                        setContentTitle(getString(R.string.read_diary_showcase_title_5))
                        setContentText(getString(R.string.read_diary_showcase_message_5))
                    }
                    4 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.microphone, this@DiaryMainActivity))
                        setContentTitle(getString(R.string.read_diary_showcase_title_3))
                        setContentText(getString(R.string.read_diary_showcase_message_3))
                    }
                    5 -> hide()
                }
            }
            mShowcaseIndex++
        }

        mShowcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(ViewTarget(insertDiaryButton))
                .setContentTitle(getString(R.string.read_diary_showcase_title_1))
                .setContentText(getString(R.string.read_diary_showcase_message_1))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER.toLong())
                .setOnClickListener(showcaseViewOnClickListener)
                .build()
        mShowcaseView?.setButtonText(getString(R.string.read_diary_showcase_button_1))
        mShowcaseView?.setButtonPosition(centerParams)
    }

    private fun bindEvent() {
        query.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                refreshList(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        diaryList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val diaryDto = adapterView.adapter.getItem(i) as DiaryDto
            val detailIntent = Intent(this@DiaryMainActivity, DiaryReadActivity::class.java)
            detailIntent.putExtra(DIARY_SEQUENCE, diaryDto.sequence)
            detailIntent.putExtra(DIARY_SEARCH_QUERY, mDiaryMainItemAdapter?.currentQuery)
            TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, detailIntent)
        }

        modalContainer.setOnTouchListener({ _, _ -> true })

        insertDiaryButton.setOnClickListener({ _ ->
            val createDiary = Intent(this@DiaryMainActivity, DiaryInsertActivity::class.java)
            //                startActivity(createDiary);
            //                DiaryMainActivity.this.overridePendingTransition(R.anim.anim_right_to_center, R.anim.anim_center_to_left);
            TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, createDiary)
        })
    }

    private fun showSpeechDialog() {
        try {
            startActivityForResult(mRecognizerIntent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            showAlertDialog(getString(R.string.recognizer_intent_not_found_message), DialogInterface.OnClickListener { dialog, which -> })
        }

    }

    private fun refreshList() {
        var queryString = ""
        if (StringUtils.isNotEmpty(query.text)) queryString = query.text.toString()
        refreshList(queryString)
    }

    fun refreshList(query: String) {
        mDiaryList?.clear()
        mDiaryList?.addAll(EasyDiaryDbHelper.readDiary(query, config.diarySearchQueryCaseSensitive))
        mDiaryMainItemAdapter?.currentQuery = query
        mDiaryMainItemAdapter?.notifyDataSetChanged()
    }

    private fun initSampleData() {
        EasyDiaryDbHelper.insertDiary(DiaryDto(
                -1,
                this.mCurrentTimeMillis - 395000000L, getString(R.string.sample_diary_title_1), getString(R.string.sample_diary_1),
                1
        ))
        EasyDiaryDbHelper.insertDiary(DiaryDto(
                -1,
                this.mCurrentTimeMillis - 263000000L, getString(R.string.sample_diary_title_2), getString(R.string.sample_diary_2),
                2
        ))
        EasyDiaryDbHelper.insertDiary(DiaryDto(
                -1,
                this.mCurrentTimeMillis - 132000000L, getString(R.string.sample_diary_title_3), getString(R.string.sample_diary_3),
                3
        ))
        EasyDiaryDbHelper.insertDiary(DiaryDto(
                -1,
                this.mCurrentTimeMillis - 4000000L, getString(R.string.sample_diary_title_4), getString(R.string.sample_diary_4),
                4
        ))
    }
}
