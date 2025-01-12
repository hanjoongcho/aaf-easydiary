package me.blog.korn123.easydiary.activities

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import me.blog.korn123.commons.utils.*
import me.blog.korn123.commons.utils.EasyDiaryUtils.createAttachedPhotoView
import me.blog.korn123.commons.utils.EasyDiaryUtils.createAttachedPhotoViewForFlexBox
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.*
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.DiaryFragment
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.viewmodels.DiaryReadViewModel
import org.apache.commons.lang3.StringUtils
import java.util.*


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryReadingActivity : EasyDiaryActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mBinding: ActivityDiaryReadingBinding
    private lateinit var mPopupEncryptionBinding: PopupEncryptionBinding
    private lateinit var mDialogHighlightKeywordBinding: DialogHighlightKeywordBinding
    private var mTextToSpeech: TextToSpeech? = null
    private var mShowcaseView: ShowcaseView? = null
    private var mShowcaseIndex = 1

    companion object {
        const val ENCRYPTION = "encryption"
        const val DECRYPTION = "decryption"
        const val EDITING = "editing"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDiaryReadingBinding.inflate(layoutInflater)
        mPopupEncryptionBinding = PopupEncryptionBinding.inflate(layoutInflater)
        mDialogHighlightKeywordBinding = DialogHighlightKeywordBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }

        val query = intent.getStringExtra(SELECTED_SEARCH_QUERY)
        val symbolSequence = intent.getIntExtra(SELECTED_SYMBOL_SEQUENCE, 0)
        val diaryList: List<Diary> = when (intent.getStringExtra(DiaryFragment.MODE_FLAG) == null) {
            true -> EasyDiaryDbHelper.findDiary(query, config.diarySearchQueryCaseSensitive, 0, 0, symbolSequence)
            false -> EasyDiaryUtils.applyFilter(intent.getStringExtra(DiaryFragment.MODE_FLAG))
        }

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, diaryList, query)
        val startPageIndex = when(savedInstanceState == null) {
            true -> mSectionsPagerAdapter.sequenceToPageIndex(intent.getIntExtra(DIARY_SEQUENCE, -1))
            false -> mSectionsPagerAdapter.sequenceToPageIndex(savedInstanceState.getInt(DIARY_SEQUENCE, -1))
        }

        setupViewPager()
        if (startPageIndex > 0) Handler(Looper.getMainLooper()).post { mBinding.diaryViewPager.setCurrentItem(startPageIndex, false) }
        setupShowcase()
    }

    override fun onResume() {
        super.onResume()
        initModule()
        mSectionsPagerAdapter.notifyDataSetChanged()
        mSectionsPagerAdapter.instantiateItem(mBinding.diaryViewPager, mBinding.diaryViewPager.currentItem).run {
            if (this is PlaceholderFragment) {
                if (this.isAdded) this.initContents()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        destroyModule()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val fragment = mSectionsPagerAdapter.instantiateItem(mBinding.diaryViewPager, mBinding.diaryViewPager.currentItem)
        if (fragment is PlaceholderFragment) {
            outState.putInt(DIARY_SEQUENCE, fragment.getSequence())
        }
        super.onSaveInstanceState(outState)
    }

    var mDialogSearch: AlertDialog? = null
    var mSearchIndexes = arrayListOf<Int>()
    var mCurrentIndexesSequence = 0
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val fragment = mSectionsPagerAdapter?.getItem(diaryViewPager.currentItem)
        val fragment = mSectionsPagerAdapter.instantiateItem(mBinding.diaryViewPager, mBinding.diaryViewPager.currentItem)
//        val fontSize = config.settingFontSize
        if (fragment is PlaceholderFragment) {
            when (item.itemId) {
                android.R.id.home ->
                    if (isAccessFromOutside()) {
                        startMainActivityWithClearTask()
                    } else {
                        this.onBackPressed()
                    }
                R.id.edit -> {
                    if (fragment.isEncryptContents()) {
                        showEncryptPagePopup(fragment, EDITING) { inputPass ->
                            startEditing(fragment, inputPass)
                        }
                    } else {
                        startEditing(fragment)
                    }
                }
                R.id.speechOutButton -> textToSpeech(fragment.getDiaryContents())
//                R.id.postCard -> {
//                    val postCardIntent = Intent(this@DiaryReadActivity, PostcardActivity::class.java)
//                    postCardIntent.putExtra(DIARY_SEQUENCE, fragment.getSequence())
//                    //                startActivityForResult(postCardIntent, Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER);
//                    TransitionHelper.startActivityWithTransition(this@DiaryReadActivity, postCardIntent)
//                }
//                R.id.encryptData -> {
//                    showEncryptPagePopup(fragment, ENCRYPTION)
//                }
//                R.id.decryptData -> {
//                    showEncryptPagePopup(fragment, DECRYPTION)
//                }
                R.id.popupMenu -> createCustomOptionMenu()
                R.id.highlightText -> {
                    fun highLight() {
                        // Dialog for search keyword
                        when (mDialogHighlightKeywordBinding.searchKeywordQuery.text.toString().isNotEmpty()) {
                            true -> fragment.highlightDiary(mDialogHighlightKeywordBinding.searchKeywordQuery.text.toString(), true)
                            false -> fragment.clearHighLight()
                        }
//                        mDialogSearch?.dismiss()
                    }
                    fun updateLabel() {
                        mDialogHighlightKeywordBinding.indexes.text = "$mCurrentIndexesSequence / ${mSearchIndexes.size}"
                    }
                    mDialogSearch?.show() ?: run {
                        val builder = AlertDialog.Builder(this).apply {
                            setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                            }
                        }
                        mDialogSearch = builder.create()
                        mDialogHighlightKeywordBinding.run {
                            next.setOnClickListener {
                                if (mCurrentIndexesSequence < mSearchIndexes.size) {
                                    mCurrentIndexesSequence = mCurrentIndexesSequence.plus(1)
                                    fragment.moveScroll(searchKeywordQuery.text.toString(), mSearchIndexes[mCurrentIndexesSequence.minus(1)])
                                    updateLabel()
                                }
                            }
                            previous.setOnClickListener {
                                if (mCurrentIndexesSequence > 1) {
                                    mCurrentIndexesSequence = mCurrentIndexesSequence.minus(1)
                                    fragment.moveScroll(searchKeywordQuery.text.toString(), mSearchIndexes[mCurrentIndexesSequence.minus(1)])
                                    updateLabel()
                                }
                            }
                            searchKeywordQuery.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                                    makeToast(charSequence.toString())
                                    mCurrentIndexesSequence = 0
                                    mSearchIndexes.clear()
                                    mSearchIndexes.addAll(EasyDiaryUtils.searchWordIndexes(fragment.getContents(), charSequence.toString()))
                                    highLight()
                                    if (mSearchIndexes.isNotEmpty()) mCurrentIndexesSequence = 1
                                    updateLabel()
                                }
                                override fun afterTextChanged(editable: Editable) {}
                            })
                            root.run {
                                updateTextColors(this)
                                FontUtils.setFontsTypeface(applicationContext, null, this, mCustomLineSpacing)
//                                updateAlertDialog(mDialogSearch!!, null, this, null, 200)
                                updateAlertDialogWithIcon(DialogMode.INFO, mDialogSearch!!, null, this, null, 200)
                            }
                            updateLabel()
                        }
                    }
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_diary_reading, menu)
        when (config.enableDebugMode) {
            true -> menu.findItem(R.id.highlightText).isVisible = true
            false -> menu.findItem(R.id.highlightText).isVisible = false
        }
        return true
    }

    private fun startEditing(fragment: PlaceholderFragment, inputPass: String? = null) {
        val updateDiaryIntent = Intent(this@DiaryReadingActivity, DiaryEditingActivity::class.java).apply {
            putExtra(DIARY_SEQUENCE, fragment.getSequence())
            putExtra(DIARY_CONTENTS_SCROLL_Y, fragment.getContentsPositionY())
            inputPass?.let {
                putExtra(DIARY_ENCRYPT_PASSWORD, it)
            }

        }
        TransitionHelper.startActivityWithTransition(this@DiaryReadingActivity, updateDiaryIntent)
    }

    private fun showEncryptPagePopup(fragment: PlaceholderFragment, workMode: String, callback: ((inputPass: String) -> Unit)? = null) {

        mPopupEncryptionBinding.run {
            updateDrawableColorInnerCardView(closePopup)
            var inputPass = ""
            var confirmPass = ""
            holdCurrentOrientation()
            val popupView = root.apply {
                setBackgroundColor(ColorUtils.setAlphaComponent(config.backgroundColor, 250))
                decMode1.setTextColor(config.textColor)
                decMode2.setTextColor(config.textColor)
            }

            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val popupWindow = PopupWindow(popupView, width, height, true).apply {
                showAtLocation(findViewById<ViewGroup>(android.R.id.content).rootView, Gravity.CENTER, 0, 0)
            }

            closePopup.setOnClickListener {
                it.postDelayed({
                    popupWindow.dismiss()
                    clearHoldOrientation()
                }, 300)
            }

            fun clearPassView() {
                pass1.text = ""
                pass2.text = ""
                pass3.text = ""
                pass4.text = ""
                pass5.text = ""
                pass6.text = ""
            }

            when (workMode) {
                ENCRYPTION -> {
                    description.text = getString(R.string.diary_encryption_title)
                    guideMessage.text = getString(R.string.diary_encryption_guide)
                    decMode.visibility = View.GONE
                }
                DECRYPTION -> {
                    description.text =  getString(R.string.diary_decryption_title)
                    guideMessage.text = getString(R.string.diary_decryption_guide)
                    decMode.visibility = View.VISIBLE
                }
                EDITING -> {
                    description.text =  getString(R.string.diary_decryption_title)
                    guideMessage.text = getString(R.string.diary_decryption_guide_before_editing)
                    decMode.visibility = View.GONE
                }
            }
            EasyDiaryUtils.warningString(guideMessage)

            val onclickListener = View.OnClickListener {
                clearPassView()
                when (it.id) {
                    R.id.button1 -> inputPass += "1"
                    R.id.button2 -> inputPass += "2"
                    R.id.button3 -> inputPass += "3"
                    R.id.button4 -> inputPass += "4"
                    R.id.button5 -> inputPass += "5"
                    R.id.button6 -> inputPass += "6"
                    R.id.button7 -> inputPass += "7"
                    R.id.button8 -> inputPass += "8"
                    R.id.button9 -> inputPass += "9"
                    R.id.button0 -> inputPass += "0"
                    R.id.buttonDel -> {
                        if (inputPass.isNotEmpty()) inputPass = inputPass.substring(0, inputPass.length.minus(1))
                    }
                }

                if (inputPass.isNotEmpty()) pass1.text = "*"
                if (inputPass.length > 1) pass2.text = "*"
                if (inputPass.length > 2) pass3.text = "*"
                if (inputPass.length > 3) pass4.text = "*"
                if (inputPass.length > 4) pass5.text = "*"
                if (inputPass.length > 5) pass6.text = "*"

                if (inputPass.length == 6) {

                    when {
                        confirmPass.length == 6 -> {
                            when (confirmPass == inputPass) {
                                true -> {
                                    fragment.encryptData(inputPass)
                                    popupWindow.dismiss()
                                    clearHoldOrientation()
                                }
                                false -> guideMessage.text = getString(R.string.diary_pin_number_confirm_error)
                            }
                            inputPass = ""
                            confirmPass = ""
                        }
                        workMode == DECRYPTION -> {
                            when (fragment.getPasswordHash() == JasyptUtils.sha256(inputPass)) {
                                true -> {
                                    if (decMode1.isChecked) {
                                        fragment.decryptDataOnce(inputPass)
                                    } else {
                                        fragment.decryptData(inputPass)
                                    }
                                    popupWindow.dismiss()
                                    clearHoldOrientation()
                                }
                                false -> {
                                    inputPass = ""
                                    guideMessage.text = getString(R.string.diary_pin_number_verification_error)
                                }
                            }
                        }
                        workMode == EDITING -> {
                            when (fragment.getPasswordHash() == JasyptUtils.sha256(inputPass)) {
                                true -> {
                                    callback?.invoke(inputPass)
                                    popupWindow.dismiss()
                                    clearHoldOrientation()
                                }
                                else -> {
                                    inputPass = ""
                                    guideMessage.text = getString(R.string.diary_pin_number_verification_error)
                                }
                            }
                        }
                        else -> {
                            confirmPass = inputPass
                            inputPass = ""
                            guideMessage.text = getString(R.string.diary_pin_number_confirm_guide)
                        }
                    }
                    clearPassView()
                }
            }
            button1.setOnClickListener(onclickListener)
            button2.setOnClickListener(onclickListener)
            button3.setOnClickListener(onclickListener)
            button4.setOnClickListener(onclickListener)
            button5.setOnClickListener(onclickListener)
            button6.setOnClickListener(onclickListener)
            button7.setOnClickListener(onclickListener)
            button8.setOnClickListener(onclickListener)
            button9.setOnClickListener(onclickListener)
            button0.setOnClickListener(onclickListener)
            buttonDel.setOnClickListener(onclickListener)

            run {
//            initTextSize(this, context)
                updateTextColors(this.root)
                updateAppViews(this.root)
                updateCardViewPolicy(this.root)
            }
            FontUtils.setFontsTypeface(applicationContext, null, popupView, true)
        }
    }

    private fun setupViewPager() {
        // Set up the ViewPager with the sections adapter.
        mBinding.diaryViewPager.adapter = mSectionsPagerAdapter
        mBinding.diaryViewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val fragment = mSectionsPagerAdapter.instantiateItem(mBinding.diaryViewPager, mBinding.diaryViewPager.currentItem)
                fragment.let {
//                    it.setFontsTypeface()
//                    it.setFontsSize()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun setupShowcase() {
        val margin = ((resources.displayMetrics.density * 60) as Number).toInt()

        val centerParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        centerParams.setMargins(0, 0, 0, margin)

        val showcaseViewOnClickListener = View.OnClickListener {
            mShowcaseView?.run {
                when (mShowcaseIndex) {
                    1 -> {
                        setButtonPosition(centerParams)
                        setShowcase(ViewTarget(mBinding.diaryViewPager), false)
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_1))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_1))
                    }
                    2 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.edit, this@DiaryReadingActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_2))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_2))
                    }
                    3 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.speechOutButton, this@DiaryReadingActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_3))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_3))
                        setButtonText(getString(R.string.create_diary_showcase_button_2))
                    }
                    4 -> hide()
                }
            }
            mShowcaseIndex++
        }

        mShowcaseView = ShowcaseView.Builder(this)
                .withMaterialShowcase()
                .setContentTitle(getString(R.string.read_diary_detail_showcase_title_0))
                .setContentText(getString(R.string.read_diary_detail_showcase_message_0))
                .setStyle(R.style.ShowcaseTheme)
                .singleShot(SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER.toLong())
                .setOnClickListener(showcaseViewOnClickListener)
                .blockAllTouches()
                .build()
        mShowcaseView?.run {
            setButtonText(getString(R.string.read_diary_detail_showcase_button_1))
            setButtonPosition(centerParams)
        }
    }

    private fun initModule() {
        mTextToSpeech = TextToSpeech(this@DiaryReadingActivity) { status ->
            if (status == TextToSpeech.SUCCESS) {
                mTextToSpeech?.run {
                    language = Locale.getDefault()
                    setPitch(1.3f)
                    setSpeechRate(1f)
                }
            }
        }
    }

    private fun destroyModule() {
        mTextToSpeech?.run {
            stop()
            shutdown()
            mTextToSpeech = null
        }
    }

    private fun textToSpeech(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(text)
        } else {
            ttsUnder20(text)
        }
    }

    @Suppress("DEPRECATION")
    private fun ttsUnder20(text: String) {
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"
        mTextToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {}

            override fun onError(utteranceId: String) {}

            override fun onDone(utteranceId: String) {}
        })
        mTextToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, map)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(text: String) {
        val utteranceId = this.hashCode().toString() + ""
        mTextToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun createCustomOptionMenu() {
        val pmrBinding = PopupMenuReadBinding.inflate(layoutInflater)
        var popupWindow: PopupWindow? = null
        val popupView = pmrBinding.root.apply {
            updateAppViews(this)
            updateTextColors(this)
            FontUtils.setFontsTypeface(applicationContext, null, this, true)
            val fragment = mSectionsPagerAdapter.instantiateItem(mBinding.diaryViewPager, mBinding.diaryViewPager.currentItem) as PlaceholderFragment
            pmrBinding.run {
                when (fragment.isEncryptContents()) {
                    true -> decryptData.visibility = View.VISIBLE
                    false -> encryptData.visibility = View.VISIBLE
                }
                val itemClickListener = View.OnClickListener { view ->
                    when (view.id) {
                        R.id.delete -> {
                            val positiveListener = DialogInterface.OnClickListener { _, _ ->
                                EasyDiaryDbHelper.deleteDiaryBy(fragment.getSequence())
                                TransitionHelper.finishActivityWithTransition(this@DiaryReadingActivity)
                            }
                            showAlertDialog(
                                getString(R.string.delete_confirm),
                                positiveListener,
                                { _, _ -> },
                                DialogMode.WARNING,
                                true,
                                getString(R.string.delete),
                                getString(R.string.delete)
                            )
                        }
                        R.id.postcard -> {
                            val postCardIntent = Intent(this@DiaryReadingActivity, PostcardActivity::class.java)
                            postCardIntent.putExtra(DIARY_SEQUENCE, fragment.getSequence())
                            TransitionHelper.startActivityWithTransition(this@DiaryReadingActivity, postCardIntent)
                        }
                        R.id.encryptData -> showEncryptPagePopup(fragment, ENCRYPTION)
                        R.id.decryptData -> showEncryptPagePopup(fragment, DECRYPTION)
                    }
                    popupWindow?.dismiss()
                }
                delete.setOnClickListener(itemClickListener)
                postcard.setOnClickListener(itemClickListener)
                encryptData.setOnClickListener(itemClickListener)
                decryptData.setOnClickListener(itemClickListener)

                updateDrawableColorInnerCardView(imgPostcard)
                updateDrawableColorInnerCardView(imgEncryptData)
                updateDrawableColorInnerCardView(imgDecryptData)
                updateDrawableColorInnerCardView(imgDelete)
            }
        }
        popupWindow = EasyDiaryUtils.openCustomOptionMenu(popupView, findViewById(R.id.popupMenu))
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : androidx.fragment.app.Fragment() {
        private lateinit var mRootView: ViewGroup
        private lateinit var mBinding: FragmentDiaryReadBinding
        private val mViewModel: DiaryReadViewModel by viewModels()
        private var mPrimaryColor = 0
        private var mStoredContents: String? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_diary_read, container, false)
            mBinding.lifecycleOwner = this
            mBinding.viewModel = mViewModel
            mRootView = mBinding.mainHolder
            return mRootView
//            mRootView = inflater.inflate(R.layout.fragment_diary_read, container, false) as ViewGroup
//            return mRootView
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            requireContext().changeDrawableIconColor(config.primaryColor, R.drawable.ic_map_marker_2)
            mRootView.let {
                context?.run {
                    updateTextColors(it,0,0)
                    updateAppViews(it)
                    updateCardViewPolicy(it)
                }
            }

            initBottomContainer()
            setFontsTypeface()
            setFontsSize()
            initContents()
        }

//        override fun onResume() {
//            super.onResume()
//            initContents()
//        }

        fun getSequence() = arguments?.getInt(DIARY_SEQUENCE) ?: -1

        fun getDiaryContents(): String = mBinding.diaryContents.text.toString()

        fun isEncryptContents() = EasyDiaryDbHelper.findDiaryBy(getSequence())?.isEncrypt ?: false

        fun getPasswordHash() = EasyDiaryDbHelper.findDiaryBy(getSequence())?.encryptKeyHash

        fun highlightDiary(query: String, moveScroll: Boolean = false) {
            context?.config?.run {
                if (diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(mBinding.diaryTitle, query)
                    EasyDiaryUtils.highlightString(mBinding.diaryContents, query)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(mBinding.diaryTitle, query)
                    EasyDiaryUtils.highlightStringIgnoreCase(mBinding.diaryContents, query)
                }
            }

            if (moveScroll) {
                moveScroll(query)
            }
        }

        fun moveScroll(query: String, startIndex: Int = 0) {
            val index = mBinding.diaryContents.text.toString().indexOf(query, startIndex, true)
            if (index > 0) {
                val layout = mBinding.diaryContents.layout
                mBinding.scrollDiaryContents.scrollTo(0, layout.getLineTop(layout.getLineForOffset(index)))
            }
        }

        fun getContents() = mBinding.diaryContents.text.toString()

        fun clearHighLight() {
            mBinding.diaryTitle.run {
                this.text = SpannableString(this.text).apply { EasyDiaryUtils.removeSpans(this) }
            }
            mBinding.diaryContents.run {
                this.text = SpannableString(this.text).apply { EasyDiaryUtils.removeSpans(this) }
            }
        }

        fun initContents() {
            val diaryDto = EasyDiaryDbHelper.findDiaryBy(getSequence())!!
            mBinding.run {
                if (StringUtils.isEmpty(diaryDto.title)) {
                    diaryTitle.visibility = View.GONE
                }
                diaryTitle.text = diaryDto.title

                EasyDiaryUtils.boldString(requireContext(), diaryTitle)
                if (mStoredContents != diaryDto.contents) {
                    requireActivity().applyMarkDownPolicy(diaryContents, diaryDto.contents!!)
                    mStoredContents = diaryDto.contents
                    Handler(Looper.getMainLooper()).post { scrollDiaryContents.scrollY = 0 }
                }
                date.text = when (diaryDto.isAllDay) {
                    true -> DateUtils.getDateStringFromTimeMillis(diaryDto.currentTimeMillis)
                    false -> DateUtils.getDateTimeStringForceFormatting(diaryDto.currentTimeMillis, requireContext())
                }
                initBottomContainer()

                arguments?.getString(SELECTED_SEARCH_QUERY)?.let { query ->
                    if (StringUtils.isNotEmpty(query)) {
                        highlightDiary(query)
                    }
                }

                val weatherFlag = diaryDto.weather
                FlavorUtils.initWeatherView(requireContext(), weather, weatherFlag)

                // TODO fixme elegance
                val photoCount = diaryDto.photoUris?.size ?: 0
                mBinding.run {
                    if (photoCount > 0) {
//                        photoContainerScrollView.visibility = View.VISIBLE
                        if (photoContainer.childCount > 0) {
//                            photoContainer.removeAllViews()
                            photoContainerFlexBox.removeAllViews()
                        }
                        context?.let { appContext ->
//                        val thumbnailSize = appContext.config.settingThumbnailSize
                            diaryDto.photoUrisWithEncryptionPolicy()?.forEachIndexed { index, item ->
//                                val marginRight = if (index == photoCount.minus(1)) 0F else 3F
//                                val imageView = when (requireActivity().isLandScape()) {
//                                    true -> createAttachedPhotoView(appContext, item, 0F, 0F, marginRight, 0F)
//                                    false -> createAttachedPhotoView(appContext, item, 0F, 0F, marginRight, 0F)
//                                }
//                                photoContainer.addView(imageView)
//                                imageView.setOnClickListener(PhotoClickListener(getSequence(), index))

                                requireActivity().run {
                                    val point = getDefaultDisplay()
                                    val spanCount = if (isLandScape()) config.postcardSpanCountLandscape else config.postcardSpanCountPortrait
                                    val targetX = (point.x - dpToPixel(30F) - dpToPixel(spanCount * 2f * 2f)) / spanCount
                                    val imageView2 = createAttachedPhotoViewForFlexBox(appContext, item, targetX).apply {
                                        setOnClickListener(PhotoClickListener(getSequence(), index))
                                    }
                                    photoContainerFlexBox.addView(imageView2)
                                }
                            }
                        }
                    } else {
                        photoContainerScrollView.visibility = View.GONE
                    }
                }

                context?.run {
                    mViewModel.isShowAddress.value = config.enableLocationInfo
                    if (config.enableLocationInfo) {
                        diaryDto.location?.let {
//                        locationLabel.setTextColor(config.textColor)
//                        locationContainer.background = getLabelBackground()
                            locationLabel.text = it.address
                        } ?: { mViewModel.isShowAddress.value = false } ()
                    }

                    mViewModel.isShowContentsCounting.value = config.enableCountCharacters
                    if (config.enableCountCharacters) {
                        contentsLength.run {
//                        setTextColor(config.textColor)
//                        background = getLabelBackground()
                            text = getString(R.string.diary_contents_length, diaryDto.contents?.length ?: 0)
                        }
                    }
                }
            }
        }

        private fun initBottomContainer() {
            context?.let {
                mPrimaryColor = it.config.primaryColor
            }
        }

        private fun setFontsTypeface() {
            activity?.let { it ->
                FontUtils.setFontsTypeface(it, "", mRootView)
            }
        }

        private fun setFontsSize() {
            requireContext().initTextSize(mRootView)
        }

        fun encryptData(inputPass: String) {
            context?.let {
                val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
                val diaryDto = EasyDiaryDbHelper.findDiaryBy(getSequence(), realmInstance)!!
                realmInstance.beginTransaction()
                diaryDto.isEncrypt = true
                diaryDto.title = JasyptUtils.encrypt(diaryDto.title ?: "", inputPass)
                diaryDto.contents = JasyptUtils.encrypt(diaryDto.contents ?: "", inputPass)
                diaryDto.encryptKeyHash = JasyptUtils.sha256(inputPass)
                realmInstance.commitTransaction()
                realmInstance.close()
                initContents()
            }
        }

        fun decryptDataOnce(inputPass: String) {
            EasyDiaryDbHelper.findDiaryBy(getSequence())?.let { diaryDto ->
                mBinding.run {
                    diaryTitle.text = JasyptUtils.decrypt(diaryDto.title!!, inputPass)
//                    diaryContents.text = JasyptUtils.decrypt(diaryDto.contents!!, inputPass)
                    requireActivity().applyMarkDownPolicy(diaryContents, JasyptUtils.decrypt(diaryDto.contents!!, inputPass))
                }
            }
        }

        fun decryptData(inputPass: String): Boolean {
            var result = true
            context?.let {
                val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
                val diaryDto = EasyDiaryDbHelper.findDiaryBy(getSequence(), realmInstance)!!
                if (diaryDto.encryptKeyHash == JasyptUtils.sha256(inputPass)) {
                    realmInstance.beginTransaction()
                    diaryDto.isEncrypt = false
                    diaryDto.title = JasyptUtils.decrypt(diaryDto.title ?: "", inputPass)
                    diaryDto.contents = JasyptUtils.decrypt(diaryDto.contents ?: "", inputPass)
                    realmInstance.commitTransaction()
                    realmInstance.close()
                    initContents()
                } else {
                    result = false
                }
            }
            return result
        }

        fun getContentsPositionY(): Int {
            return (mBinding.diaryContents.parent.parent as ScrollView).scrollY
        }

        companion object {
            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sequence: Int, query: String?): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(DIARY_SEQUENCE, sequence)
                args.putString(SELECTED_SEARCH_QUERY, query)
                fragment.arguments = args
                return fragment
            }
        }

        inner class PhotoClickListener(private var diarySequence: Int, var index: Int) : View.OnClickListener {
            override fun onClick(v: View) {
                val photoViewPager = Intent(context, PhotoViewPagerActivity::class.java)
                photoViewPager.putExtra(DIARY_SEQUENCE, diarySequence)
                photoViewPager.putExtra(DIARY_ATTACH_PHOTO_INDEX, index)
                TransitionHelper.startActivityWithTransition(activity, photoViewPager, TransitionHelper.BOTTOM_TO_TOP)
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(
            fm: androidx.fragment.app.FragmentManager,
            private val diaryList: List<Diary>,
            private val query: String?
    ) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): androidx.fragment.app.Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(diaryList[position].sequence, query)
        }

        fun sequenceToPageIndex(sequence: Int): Int {
            return EasyDiaryUtils.sequenceToPageIndex(diaryList, sequence)
        }

        override fun getCount(): Int {
            return diaryList.size
        }
    }
}
