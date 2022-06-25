package me.blog.korn123.easydiary.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.nineoldandroids.view.ViewHelper
import com.zhpan.bannerview.constants.PageStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.BaseDevActivity.Companion.NOTIFICATION_INFO
import me.blog.korn123.easydiary.activities.BaseDiaryEditingActivity.Companion.DIARY_SEQUENCE_INIT
import me.blog.korn123.easydiary.adapters.DiaryMainItemAdapter
import me.blog.korn123.easydiary.databinding.PopupMenuMainBinding
import me.blog.korn123.easydiary.dialogs.DashboardDialogFragment
import me.blog.korn123.easydiary.enums.DiaryMode
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.views.FastScrollObservableRecyclerView
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryMainActivity : ToolbarControlBaseActivity<FastScrollObservableRecyclerView>() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mPopupMenuBinding: PopupMenuMainBinding
    private lateinit var mGridLayoutManager: GridLayoutManager
    private var mDiaryMainItemAdapter: DiaryMainItemAdapter? = null
    private var mDiaryList: ArrayList<Diary> = arrayListOf()
    private var mShowcaseIndex = 0
    private var mShowcaseView: ShowcaseView? = null
    private var mPopupWindow: PopupWindow? = null
    private var mLastHistoryCheckMillis = System.currentTimeMillis()
    private val mRequestSpeechInputLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let {
                mBinding.query.setText(it[0])
                mBinding.query.setSelection(it[0].length)
            }
        }
        pauseLock()
    }
    private val mRequestSAFForHtmlBookLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                mDiaryMainItemAdapter?.getSelectedItems()?.run {
                    EasyDiaryDbHelper.copyFromRealm(this).also { cloneItems ->
                        mBinding.progressCoroutine.visibility = View.VISIBLE
                        CoroutineScope(Dispatchers.IO).launch {
                            exportHtmlBook(it.data, cloneItems)
                            withContext(Dispatchers.Main) {
                                mBinding.progressCoroutine.visibility = View.GONE
                                cloneItems.forEach {
                                    it.isSelected = false
                                    EasyDiaryDbHelper.updateDiaryBy(it)
                                }
                                mDiaryMainItemAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
        pauseLock()
    }
    var mDiaryMode = DiaryMode.READ


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun createScrollable(): FastScrollObservableRecyclerView {
        return mBinding.diaryListView
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPopupMenuBinding = PopupMenuMainBinding.inflate(layoutInflater)
        forceInitRealmLessThanOreo()
        supportActionBar?.run {
            title = getString(R.string.read_diary_title)
        }

//        mDiaryList.addAll(EasyDiaryDbHelper.findDiary(null))
        initDiaryGrid()
        initDummyData()
        updateDrawableColorInnerCardView(mBinding.imgClearQuery)
        bindEvent()
        initShowcase()
        EasyDiaryUtils.initWorkingDirectory(this@DiaryMainActivity)
        migrateData(mBinding)
        setupPopupMenu()
        checkBundle(savedInstanceState)
        setupReviewFlow()
        setupPhotoHighlight()
        checkIntent()
//        clearLockSettingsTemporary()

        // test code
        if (config.enableDebugMode) {
            makeToast("Notification id is ${intent.getIntExtra(BaseDevActivity.NOTIFICATION_ID, -1)}")
            intent.getStringExtra(NOTIFICATION_INFO)?.let { makeToast("Notification info is $it") }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(DIARY_MODE, mDiaryMode)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        refreshList()
        initTextSize(mBinding.progressDialog)
        invalidateOptionsMenu()

        if (config.previousActivity == PREVIOUS_ACTIVITY_CREATE) {
//            diaryListView.smoothScrollToPosition(0)
//            mBinding.diaryListView.setSelection(0)
            mBinding.diaryListView.layoutManager?.scrollToPosition(0)
            config.previousActivity = -1
        }

        if (ViewHelper.getTranslationY(mBinding.appBar) < 0) mBinding.searchCard.useCompatPadding = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_EXTERNAL_STORAGE -> if (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                openPostcardViewer()
            } else {
                makeSnackBar(findViewById(android.R.id.content), getString(R.string.guide_message_3))
            }
            else -> {}
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDiaryMode = DiaryMode.READ
                invalidateOptionsMenu()
                mDiaryMainItemAdapter?.notifyDataSetChanged()
                return true
            }
            R.id.delete -> {
                mDiaryMainItemAdapter?.getSelectedItems()?.run {
                    when (this.isNotEmpty()) {
                        true -> {
                            showAlertDialog(getString(R.string.delete_selected_items_confirm, this.size), { _, _ ->
                                this.forEach { EasyDiaryDbHelper.deleteDiaryBy(it.sequence) }
                                refreshList()
                            }, null)
                        }
                        false -> {
                            showAlertDialog(getString(R.string.no_items_warning), null)
                        }
                    }
                }
            }
            R.id.duplication -> {
                mDiaryMainItemAdapter?.getSelectedItems()?.run {
                    when (this.isNotEmpty()) {
                        true -> {
                            showAlertDialog(getString(R.string.duplicate_selected_items_confirm, this.size), { _, _ ->
                                this.reversed().map {
                                    EasyDiaryDbHelper.beginTransaction()
                                    it.isSelected = false
                                    EasyDiaryDbHelper.commitTransaction()
                                    // EasyDiaryDbHelper.updateDiaryBy(it)
                                    EasyDiaryDbHelper.duplicateDiaryBy(it)
                                }
                                refreshList()
                                Handler(Looper.getMainLooper()).post { mBinding.diaryListView.layoutManager?.scrollToPosition(0) }
                            }, null)
                        }
                        false -> {
                            showAlertDialog(getString(R.string.no_items_warning), null)
                        }
                    }
                }
            }
            R.id.saveAsHtml -> {
//                writeFileWithSAF("${DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DELIMITER)}.html", MIME_TYPE_HTML, REQUEST_CODE_SAF_HTML_BOOK)
                EasyDiaryUtils.writeFileWithSAF("${DateUtils.getCurrentDateTime(DateUtils.DATE_TIME_PATTERN_WITHOUT_DASH)}.html", MIME_TYPE_HTML, mRequestSAFForHtmlBookLauncher)
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
            R.id.popupMenu -> openCustomOptionMenu()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (mDiaryMode) {
            DiaryMode.READ -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                menuInflater.inflate(R.menu.activity_diary_main, menu)
//                menu.findItem(R.id.devConsole).run {
//                    applyFontToMenuItem(this)
//                    if (config.enableDebugMode) this.setVisible(true) else this.setVisible(false)
//                }
            }
            DiaryMode.DELETE -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                menuInflater.inflate(R.menu.activity_diary_main_delete, menu)
                applyFontToMenuItem(menu.findItem(R.id.delete))
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (mBinding.progressDialog.visibility == View.GONE) ActivityCompat.finishAffinity(this@DiaryMainActivity)
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun checkIntent() {
        when (intent.getBooleanExtra(EXECUTION_MODE_WELCOME_DASHBOARD, false)) {
            true -> {
                intent.removeExtra(EXECUTION_MODE_WELCOME_DASHBOARD)
//                TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, Intent(this@DiaryMainActivity, DashboardActivity::class.java))
                DashboardDialogFragment().apply { show(supportFragmentManager, "DashboardDialog") }
            }
        }
    }

    private fun checkBundle(savedInstanceState: Bundle?) {
        when (savedInstanceState == null) {
            true -> checkWhatsNewDialog()
            false -> {
                mDiaryMode = savedInstanceState.getSerializable(DIARY_MODE) as DiaryMode
            }
        }
    }

    private fun setupReviewFlow() {
        if (config.enableReviewFlow) {
            config.appExecutionCount = config.appExecutionCount.plus(1)
            if (config.appExecutionCount > 30 && EasyDiaryDbHelper.countDiaryAll() > 300) startReviewFlow()
            if (config.enableDebugMode) makeToast("appExecutionCount: ${config.appExecutionCount}")
        }
    }

    private fun setupPhotoHighlight() {
        supportFragmentManager.beginTransaction().run {
            replace(R.id.layout_banner_container, PhotoHighlightFragment().apply {
                arguments = Bundle().apply {
                    putInt(PhotoHighlightFragment.PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                    putFloat(PhotoHighlightFragment.REVEAL_WIDTH, 20F)
                    putFloat(PhotoHighlightFragment.PAGE_MARGIN, 5F)
                    putBoolean(PhotoHighlightFragment.AUTO_PLAY, true)
                }
                togglePhotoHighlightCallback = { isVisible: Boolean -> togglePhotoHighlight(isVisible) }
            })
            commit()
        }
    }

    private fun togglePhotoHighlight(isVisible: Boolean) {
        if (config.enableDebugMode) makeToast("History Highlight Last updated time: ${System.currentTimeMillis().minus(mLastHistoryCheckMillis) / 1000}seconds ago")


        when (isVisible) {
            true -> {
                mBinding.layoutBannerContainer.visibility = View.VISIBLE
                if (isLandScape()) {
                    val point = getDefaultDisplay()
                    val historyWidth = (point.x / 2.5).toInt()
                    mBinding.layoutBannerContainer.layoutParams.width = historyWidth
                    mBinding.diaryListView.layoutParams.width = point.x.minus(historyWidth)
//                    setRevealWidth(dpToPixel(10F))
//                    create(historyItems)
                } else {
//                    setRevealWidth(dpToPixel(50F))
//                    create(historyItems)
                }
            }
            false -> {
                mBinding.run {
                    layoutBannerContainer.visibility = View.GONE
                    if (isLandScape()) {
                        diaryListView.layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
                    }
                }
            }
        }

//        if (config.enablePhotoHighlight) {
////            if (mPhotoHighlightFragment.countHighlightItems() > 0) mBinding.layoutBannerContainer.visibility = View.VISIBLE
//
//        } else {
//
//        }
    }

    private fun setupPopupMenu() {
        val customItemClickListener = View.OnClickListener {
            when (it.id) {
                R.id.postCard -> {
                    when (checkPermission(EXTERNAL_STORAGE_PERMISSIONS)) {
                        true -> openPostcardViewer()
                        false -> {
                            confirmPermission(EXTERNAL_STORAGE_PERMISSIONS, REQUEST_CODE_EXTERNAL_STORAGE)
                        }
                    }
                }
                R.id.dashboard -> TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, Intent(this@DiaryMainActivity, DashboardActivity::class.java))
                R.id.chart -> TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, Intent(this@DiaryMainActivity, StatisticsActivity::class.java))
                R.id.settings -> TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, Intent(this@DiaryMainActivity, SettingsActivity::class.java))
                R.id.devConsole -> TransitionHelper.startActivityWithTransition(this, Intent(this, DevActivity::class.java))
                R.id.gridLayout -> openGridSettingDialog(mBinding.mainHolder, 1) { spanCount ->
                    mGridLayoutManager.spanCount = spanCount
                    mBinding.diaryListView.invalidateItemDecorations()
//                    mDiaryMainItemAdapter?.notifyDataSetChanged()
                }
            }
            Handler(Looper.getMainLooper()).post { mPopupWindow?.dismiss() }
        }

        mPopupMenuBinding.run {
            updateAppViews(this.root)
            updateTextColors(this.root)
            FontUtils.setFontsTypeface(this@DiaryMainActivity, null, this.root, true)
            postCard.setOnClickListener(customItemClickListener)
            dashboard.setOnClickListener(customItemClickListener)
            chart.setOnClickListener(customItemClickListener)
            settings.setOnClickListener(customItemClickListener)
            devConsole.setOnClickListener(customItemClickListener)
            gridLayout.setOnClickListener(customItemClickListener)
        }
    }

    private fun openCustomOptionMenu() {
        FontUtils.setFontsTypeface(this@DiaryMainActivity, null, mPopupMenuBinding.root, true)
        mPopupMenuBinding.devConsole.visibility = if (config.enableDebugMode) View.VISIBLE else View.GONE
        mPopupWindow = EasyDiaryUtils.openCustomOptionMenu(mPopupMenuBinding.root, findViewById(R.id.popupMenu))
        mPopupMenuBinding.run {
            updateDrawableColorInnerCardView(imgDevConsole)
            updateDrawableColorInnerCardView(imgPostcard)
            updateDrawableColorInnerCardView(imgDashboard)
            updateDrawableColorInnerCardView(imgStatistics)
            updateDrawableColorInnerCardView(imgSettings)
            updateDrawableColorInnerCardView(imgGridLayout)
        }
    }

    private fun openPostcardViewer() {
        val postCardViewer = Intent(this@DiaryMainActivity, PostcardViewerActivity::class.java)
        TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, postCardViewer)
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
                        setShowcase(ViewTarget(mBinding.query), true)
                        setContentTitle(getString(R.string.read_diary_showcase_title_2))
                        setContentText(getString(R.string.read_diary_showcase_message_2))
                    }
                    1 -> {
                        setButtonPosition(centerParams)
                        setShowcase(ViewTarget(mBinding.diaryListView), true)
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
                        setButtonText(getString(R.string.create_diary_showcase_button_2))
                    }
                    5 -> hide()
                }
            }
            mShowcaseIndex++
        }

        mShowcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setTarget(ViewTarget(mBinding.insertDiaryButton))
                .setContentTitle(getString(R.string.read_diary_showcase_title_1))
                .setContentText(getString(R.string.read_diary_showcase_message_1))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(SHOWCASE_SINGLE_SHOT_READ_DIARY_NUMBER.toLong())
                .setOnClickListener(showcaseViewOnClickListener)
                .blockAllTouches()
                .build()
        mShowcaseView?.setButtonText(getString(R.string.read_diary_showcase_button_1))
        mShowcaseView?.setButtonPosition(centerParams)
    }

    private fun bindEvent() {
        mBinding.query.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                refreshList(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        mBinding.imgClearQuery.setOnClickListener {
            selectFeelingSymbol()
            mBinding.query.text = null
        }

        EasyDiaryUtils.disableTouchEvent(mBinding.modalContainer)

        mBinding.insertDiaryButton.setOnClickListener{
            val createDiary = Intent(this@DiaryMainActivity, DiaryWritingActivity::class.java)
            //                startActivity(createDiary);
            //                DiaryMainActivity.this.overridePendingTransition(R.anim.anim_right_to_center, R.anim.anim_center_to_left);
            TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, createDiary)
        }

        mBinding.feelingSymbolButton.setOnClickListener { openFeelingSymbolDialog(getString(R.string.diary_symbol_search_message), viewModel.symbol.value ?: 0) { symbolSequence ->
            selectFeelingSymbol(symbolSequence)
            refreshList()
        }}
    }

    private fun selectFeelingSymbol(index: Int = SYMBOL_SELECT_ALL) {
        updateSymbolSequence(if (index == 0) SYMBOL_SELECT_ALL else index)
    }

    private fun showSpeechDialog() {
        try {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }.run { mRequestSpeechInputLauncher.launch(this) }
        } catch (e: ActivityNotFoundException) {
            showAlertDialog(getString(R.string.recognizer_intent_not_found_message), { _, _ -> })
        }
    }

    private fun refreshList() {
        var queryString = ""
        if (StringUtils.isNotEmpty(mBinding.query.text)) queryString = mBinding.query.text.toString()
        refreshList(queryString)
    }

    private fun refreshList(query: String) {
        mDiaryList.clear()
        mDiaryList.addAll(EasyDiaryDbHelper.findDiary(query, config.diarySearchQueryCaseSensitive, 0, 0, viewModel.symbol.value
                ?: 0))
        mDiaryMainItemAdapter?.currentQuery = query
        mDiaryMainItemAdapter?.notifyDataSetChanged()
        mBinding.run {
            when (mDiaryList.isEmpty()) {
                true -> {
                    diaryListView.visibility = View.GONE
                    textNoDiary.visibility = View.VISIBLE
                }
                false -> {
                    diaryListView.visibility = View.VISIBLE
                    textNoDiary.visibility = View.GONE
                }
            }
        }
    }

    private fun initSampleData() {
        EasyDiaryDbHelper.insertDiary(Diary(
                DIARY_SEQUENCE_INIT,
                System.currentTimeMillis() - 395000000L, getString(R.string.sample_diary_title_1), getString(R.string.sample_diary_1),
                1
        ))
        EasyDiaryDbHelper.insertDiary(Diary(
                DIARY_SEQUENCE_INIT,
                System.currentTimeMillis() - 263000000L, getString(R.string.sample_diary_title_2), getString(R.string.sample_diary_2),
                2
        ))
        EasyDiaryDbHelper.insertDiary(Diary(
                DIARY_SEQUENCE_INIT,
                System.currentTimeMillis() - 132000000L, getString(R.string.sample_diary_title_3), getString(R.string.sample_diary_3),
                3
        ))
        EasyDiaryDbHelper.insertDiary(Diary(
                DIARY_SEQUENCE_INIT,
                System.currentTimeMillis() - 4000000L, getString(R.string.sample_diary_title_4), getString(R.string.sample_diary_4),
                4
        ))
    }

    private fun initDummyData() {
        if (!config.isInitDummyData) {
            initSampleData()
            config.isInitDummyData = true
        }
    }

    private fun initDiaryGrid() {
        mDiaryMainItemAdapter = DiaryMainItemAdapter(this, mDiaryList, {
            val detailIntent = Intent(this@DiaryMainActivity, DiaryReadingActivity::class.java)
            detailIntent.putExtra(DIARY_SEQUENCE, it.sequence)
            detailIntent.putExtra(SELECTED_SEARCH_QUERY, mDiaryMainItemAdapter?.currentQuery)
            detailIntent.putExtra(SELECTED_SYMBOL_SEQUENCE, viewModel.symbol.value ?: 0)
            TransitionHelper.startActivityWithTransition(this@DiaryMainActivity, detailIntent)
        }) {
            EasyDiaryDbHelper.clearSelectedStatus()
            mDiaryMode = DiaryMode.DELETE
            invalidateOptionsMenu()
            refreshList()
            //            mDiaryMainItemAdapter?.notifyDataSetChanged()
        }

        mGridLayoutManager = GridLayoutManager(this@DiaryMainActivity, diaryMainSpanCount())
        mBinding.diaryListView.run {
            adapter = mDiaryMainItemAdapter
            //            layoutManager = LinearLayoutManager(this@DiaryMainActivity, LinearLayoutManager.VERTICAL, false)
            layoutManager = mGridLayoutManager
            addItemDecoration(
                GridItemDecorationDiaryMain(
                    resources.getDimensionPixelSize(R.dimen.component_margin_small),
                    this@DiaryMainActivity
                )
            )
            setPopUpTypeface(FontUtils.getCommonTypeface(this@DiaryMainActivity))
        }
    }
}
