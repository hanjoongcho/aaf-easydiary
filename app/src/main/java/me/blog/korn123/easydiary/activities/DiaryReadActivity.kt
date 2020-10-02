package me.blog.korn123.easydiary.activities

import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentPagerAdapter
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import io.github.aafactory.commons.extensions.baseConfig
import io.github.aafactory.commons.utils.DateUtils
import kotlinx.android.synthetic.main.activity_diary_read.*
import kotlinx.android.synthetic.main.fragment_diary_read.*
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import kotlinx.android.synthetic.main.popup_encription.view.*
import kotlinx.android.synthetic.main.popup_menu_read.view.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils.createAttachedPhotoView
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.commons.utils.JasyptUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils
import java.util.*


/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryReadActivity : EasyDiaryActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private var mTextToSpeech: TextToSpeech? = null
    private var mShowcaseView: ShowcaseView? = null
    private var mShowcaseIndex = 1
    var mIsEncryptData: Boolean = false

    companion object {
        const val ENCRYPTION = "encryption"
        const val DECRYPTION = "decryption"
        const val EDITING = "editing"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_read)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }

        val query = intent.getStringExtra(DIARY_SEARCH_QUERY)
        val diaryList: List<DiaryDto> = EasyDiaryDbHelper.readDiary(query, config.diarySearchQueryCaseSensitive)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, diaryList, query)
        val startPageIndex = when(savedInstanceState == null) {
            true -> mSectionsPagerAdapter.sequenceToPageIndex(intent.getIntExtra(DIARY_SEQUENCE, -1))
            false -> mSectionsPagerAdapter.sequenceToPageIndex(savedInstanceState.getInt(DIARY_SEQUENCE, -1))
        }

        setupViewPager()
        if (startPageIndex > 0) Handler().post { diaryViewPager.setCurrentItem(startPageIndex, false) }
        initShowcase()
    }

    override fun onResume() {
        super.onResume()
        initModule()
    }
    
    override fun onPause() {
        super.onPause()
        destroyModule()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val fragment = mSectionsPagerAdapter.instantiateItem(diaryViewPager, diaryViewPager.currentItem)
        if (fragment is PlaceholderFragment) {
            outState.putInt(DIARY_SEQUENCE, fragment.getSequence())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val fragment = mSectionsPagerAdapter?.getItem(diaryViewPager.currentItem)
        val fragment = mSectionsPagerAdapter.instantiateItem(diaryViewPager, diaryViewPager.currentItem)
        val fontSize = config.settingFontSize
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
                R.id.postCard -> {
                    val postCardIntent = Intent(this@DiaryReadActivity, PostCardActivity::class.java)
                    postCardIntent.putExtra(DIARY_SEQUENCE, fragment.getSequence())
                    //                startActivityForResult(postCardIntent, Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER);
                    TransitionHelper.startActivityWithTransition(this@DiaryReadActivity, postCardIntent)
                }
                R.id.encryptData -> {
                    showEncryptPagePopup(fragment, ENCRYPTION)
                }
                R.id.decryptData -> {
                    showEncryptPagePopup(fragment, DECRYPTION)
                }
                R.id.popupMenu -> createCustomOptionMenu()
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_read, menu)
        when (mIsEncryptData) {
            true -> menu.findItem(R.id.encryptData).isVisible = false
            false -> menu.findItem(R.id.decryptData).isVisible = false
        }
        return true
    }

    private fun startEditing(fragment: PlaceholderFragment, inputPass: String? = null) {
        val updateDiaryIntent = Intent(this@DiaryReadActivity, DiaryUpdateActivity::class.java).apply {
            putExtra(DIARY_SEQUENCE, fragment.getSequence())
            putExtra(DIARY_CONTENTS_SCROLL_Y, (fragment.diaryContents.parent.parent as ScrollView).scrollY)
            inputPass?.let {
                putExtra(DIARY_ENCRYPT_PASSWORD, it)
            }

        }
        TransitionHelper.startActivityWithTransition(this@DiaryReadActivity, updateDiaryIntent)
    }

    private fun showEncryptPagePopup(fragment: PlaceholderFragment, workMode: String, callback: ((inputPass: String) -> Unit)? = null) {
        updateDrawableColorInnerCardView(R.drawable.delete)

        var inputPass = ""
        var confirmPass = ""
        holdCurrentOrientation()
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = (inflater.inflate(R.layout.popup_encription, null) as ViewGroup).apply {
            setBackgroundColor(ColorUtils.setAlphaComponent(baseConfig.backgroundColor, 250))
            decMode1.setTextColor(config.textColor)
            decMode2.setTextColor(config.textColor)
        }

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val popupWindow = PopupWindow(popupView, width, height, true).apply {
            showAtLocation(findViewById<ViewGroup>(android.R.id.content).rootView, Gravity.CENTER, 0, 0)
        }
        popupView.closePopup.setOnClickListener {
            popupWindow.dismiss()
            clearHoldOrientation()
        }

        fun clearPassView() {
            popupView.pass1.text = ""
            popupView.pass2.text = ""
            popupView.pass3.text = ""
            popupView.pass4.text = ""
            popupView.pass5.text = ""
            popupView.pass6.text = ""
        }

        when (workMode) {
            ENCRYPTION -> {
                popupView.description.text = getString(R.string.diary_encryption_title)
                popupView.guideMessage.text = getString(R.string.diary_encryption_guide)
                popupView.decMode.visibility = View.GONE
            }
            DECRYPTION -> {
                popupView.description.text =  getString(R.string.diary_decryption_title)
                popupView.guideMessage.text = getString(R.string.diary_decryption_guide)
                popupView.decMode.visibility = View.VISIBLE
            }
            EDITING -> {
                popupView.description.text =  getString(R.string.diary_decryption_title)
                popupView.guideMessage.text = getString(R.string.diary_decryption_guide_before_editing)
                popupView.decMode.visibility = View.GONE
            }
        }
        EasyDiaryUtils.warningString(popupView.guideMessage)

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

            if (inputPass.isNotEmpty()) popupView.pass1.text = "*"
            if (inputPass.length > 1) popupView.pass2.text = "*"
            if (inputPass.length > 2) popupView.pass3.text = "*"
            if (inputPass.length > 3) popupView.pass4.text = "*"
            if (inputPass.length > 4) popupView.pass5.text = "*"
            if (inputPass.length > 5) popupView.pass6.text = "*"

            if (inputPass.length == 6) {

                when {
                    confirmPass.length == 6 -> {
                        when (confirmPass == inputPass) {
                            true -> {
                                fragment.encryptData(inputPass)
                                popupWindow.dismiss()
                            }
                            false -> popupView.guideMessage.text = getString(R.string.diary_pin_number_confirm_error)
                        }
                        inputPass = ""
                        confirmPass = ""
                    }
                    workMode == DECRYPTION -> {
                        when (fragment.getPasswordHash() == JasyptUtils.sha256(inputPass)) {
                            true -> {
                                if (popupView.decMode1.isChecked) {
                                    fragment.decryptDataOnce(inputPass)
                                } else {
                                    fragment.decryptData(inputPass)
                                }
                                popupWindow.dismiss()
                            }
                            false -> {
                                inputPass = ""
                                popupView.guideMessage.text = getString(R.string.diary_pin_number_verification_error)
                            }
                        }
                    }
                    workMode == EDITING -> {
                        when (fragment.getPasswordHash() == JasyptUtils.sha256(inputPass)) {
                            true -> {
                                callback?.invoke(inputPass)
                                popupWindow.dismiss()
                            }
                            else -> {
                                inputPass = ""
                                popupView.guideMessage.text = getString(R.string.diary_pin_number_verification_error)
                            }
                        }
                    }
                    else -> {
                        confirmPass = inputPass
                        inputPass = ""
                        popupView.guideMessage.text = getString(R.string.diary_pin_number_confirm_guide)
                    }
                }
               clearPassView()
            }
        }
        popupView.button1.setOnClickListener(onclickListener)
        popupView.button2.setOnClickListener(onclickListener)
        popupView.button3.setOnClickListener(onclickListener)
        popupView.button4.setOnClickListener(onclickListener)
        popupView.button5.setOnClickListener(onclickListener)
        popupView.button6.setOnClickListener(onclickListener)
        popupView.button7.setOnClickListener(onclickListener)
        popupView.button8.setOnClickListener(onclickListener)
        popupView.button9.setOnClickListener(onclickListener)
        popupView.button0.setOnClickListener(onclickListener)
        popupView.buttonDel.setOnClickListener(onclickListener)

        popupView.run {
//            initTextSize(this, context)
            updateTextColors(this)
            updateAppViews(this)
            updateCardViewPolicy(this)
        }
        FontUtils.setFontsTypeface(applicationContext, assets, null, popupView, true)
    }

    private fun setupViewPager() {
        // Set up the ViewPager with the sections adapter.
        diaryViewPager.adapter = mSectionsPagerAdapter
        diaryViewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                val fragment = mSectionsPagerAdapter.instantiateItem(diaryViewPager, diaryViewPager.currentItem)
                fragment.let {
//                    it.setFontsTypeface()
//                    it.setFontsSize()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun initShowcase() {
        val margin = ((resources.displayMetrics.density * 12) as Number).toInt()

        val centerParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        centerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        centerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        centerParams.setMargins(0, 0, 0, margin)

        val showcaseViewOnClickListener = View.OnClickListener {
            mShowcaseView?.run {
                when (mShowcaseIndex) {
                    1 -> {
                        setButtonPosition(centerParams)
                        setShowcase(ViewTarget(diaryViewPager), false)
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_1))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_1))
                    }
                    2 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.edit, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_2))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_2))
                    }
                    3 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.speechOutButton, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_3))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_3))
                    }
                    4 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.postCard, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_4))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_4))
                    }
                    5 -> hide()
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
                .build()
        mShowcaseView?.run {
            setButtonText(getString(R.string.read_diary_detail_showcase_button_1))
            setButtonPosition(centerParams)
        }
    }

    private fun initModule() {
        mTextToSpeech = TextToSpeech(this@DiaryReadActivity, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                mTextToSpeech?.run {
                    language = Locale.getDefault()
                    setPitch(1.3f)
                    setSpeechRate(1f)
                }
            }
        })
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
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId")
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
        var popupWindow: PopupWindow? = null
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = (inflater.inflate(R.layout.popup_menu_read, null) as ViewGroup).apply {
            updateAppViews(this)
            updateTextColors(this)
            FontUtils.setFontsTypeface(applicationContext, assets, null, this, true)
            val fragment = mSectionsPagerAdapter.instantiateItem(diaryViewPager, diaryViewPager.currentItem) as PlaceholderFragment
            delete.setOnClickListener {
                val positiveListener = DialogInterface.OnClickListener { _, _ ->
                    EasyDiaryDbHelper.deleteDiary(fragment.getSequence())
                    TransitionHelper.finishActivityWithTransition(this@DiaryReadActivity)
                }
                showAlertDialog(getString(R.string.delete_confirm), positiveListener, null)
                popupWindow?.dismiss()
            }
        }
        popupWindow = EasyDiaryUtils.openCustomOptionMenu(popupView, findViewById(R.id.popupMenu))
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : androidx.fragment.app.Fragment() {
        private var mPrimaryColor = 0
        private lateinit var mRootView: ViewGroup

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        }
        
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            mRootView = inflater.inflate(R.layout.fragment_diary_read, container, false) as ViewGroup
            return mRootView
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            togglePhoto.setOnClickListener {
                context?.let { context ->
                    when (photoContainerScrollView.visibility) {
                        View.VISIBLE -> {
                            photoContainerScrollView.visibility = View.GONE
                            togglePhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.expand))
                        }
                        View.GONE -> {
                            photoContainerScrollView.visibility = View.VISIBLE
                            togglePhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.collapse))
                        }
                    }    
                }
            }

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

        override fun onResume() {
            super.onResume()
            initContents()
        }

        override fun onDestroy() {
            super.onDestroy()
        }

        fun getSequence() = arguments?.getInt(DIARY_SEQUENCE) ?: -1

        fun getDiaryContents(): String = diaryContents.text.toString() 

        fun isEncryptContents() = EasyDiaryDbHelper.readDiaryBy(getSequence()).isEncrypt

        fun getPasswordHash() = EasyDiaryDbHelper.readDiaryBy(getSequence()).encryptKeyHash

        private fun initContents() {
            val diaryDto = EasyDiaryDbHelper.readDiaryBy(getSequence())

            if (StringUtils.isEmpty(diaryDto.title)) {
                diaryTitle.visibility = View.GONE
            }
            diaryTitle.text = diaryDto.title
            EasyDiaryUtils.boldString(context!!, diaryTitle)
            diaryContents.text = diaryDto.contents
            date.text = when (diaryDto.isAllDay) {
                true -> DateUtils.getFullPatternDate(diaryDto.currentTimeMillis)
                false -> DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
            }
            initBottomContainer()

            arguments?.getString(DIARY_SEARCH_QUERY)?.let { query ->
                if (StringUtils.isNotEmpty(query)) {
                    context?.config?.run {
                        if (diarySearchQueryCaseSensitive) {
                            EasyDiaryUtils.highlightString(diaryTitle, query)
                            EasyDiaryUtils.highlightString(diaryContents, query)
                        } else {
                            EasyDiaryUtils.highlightStringIgnoreCase(diaryTitle, query)
                            EasyDiaryUtils.highlightStringIgnoreCase(diaryContents, query)
                        }
                    }
                }    
            }

            val weatherFlag = diaryDto.weather
            FlavorUtils.initWeatherView(context!!, weather, weatherFlag)

            // TODO fixme elegance
            val photoCount = diaryDto.photoUris?.size ?: 0 
            if (photoCount > 0) {
                bottomTitle.text = String.format(getString(R.string.attached_photo_count), photoCount)
                bottomToolbar.visibility = View.VISIBLE
                photoContainerScrollView.visibility = View.VISIBLE

                if (photoContainer.childCount > 0) photoContainer.removeAllViews()
                context?.let { appContext ->
                    val thumbnailSize = appContext.config.settingThumbnailSize
                    diaryDto.photoUris?.forEachIndexed { index, item ->
                       val imageView = createAttachedPhotoView(appContext, item, index)
                        photoContainer.addView(imageView)
                        imageView.setOnClickListener(PhotoClickListener(getSequence(), index))
                    }
                }
            } else {
                bottomToolbar.visibility = View.GONE
                photoContainerScrollView.visibility = View.GONE
            }

            context?.run {
                if (config.enableLocationInfo) {
                    diaryDto.location?.let {
//                        locationLabel.setTextColor(config.textColor)
//                        locationContainer.background = getLabelBackground()

                        locationLabel.text = it.address
                        locationContainer.visibility = View.VISIBLE
                    } ?: { locationContainer.visibility = View.GONE } ()
                } else {
                    locationContainer.visibility = View.GONE
                }

                if (config.enableCountCharacters) {
                    contentsLength.run {
//                        setTextColor(config.textColor)
//                        background = getLabelBackground()
                        text = getString(R.string.diary_contents_length, diaryDto.contents?.length ?: 0)
                    }
                    contentsLengthContainer.visibility = View.VISIBLE
                } else {
                    contentsLengthContainer.visibility = View.GONE
                }

                (this as DiaryReadActivity).run {
                    mIsEncryptData = diaryDto.isEncrypt
                    invalidateOptionsMenu()
                }
            }
        }

        private fun initBottomContainer() {
            context?.let {
                mPrimaryColor = it.config.primaryColor
            }
        }

        fun setFontsTypeface() {
            activity?.let { it ->
                FontUtils.setFontsTypeface(it, it.assets, "", mRootView)    
            }
        }

        fun setFontsSize() {
            context?.let {
                it.initTextSize(mRootView)
            }
        }

        fun encryptData(inputPass: String) {
            context?.let {
                val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
                val diaryDto = EasyDiaryDbHelper.readDiaryBy(getSequence(), realmInstance)
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
            diaryTitle.text = JasyptUtils.decrypt(diaryTitle.text.toString(), inputPass)
            diaryContents.text = JasyptUtils.decrypt(diaryContents.text.toString(), inputPass)
        }

        fun decryptData(inputPass: String): Boolean {
            var result = true
            context?.let {
                val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
                val diaryDto = EasyDiaryDbHelper.readDiaryBy(getSequence(), realmInstance)
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

        companion object {
            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sequence: Int, query: String?): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(DIARY_SEQUENCE, sequence)
                args.putString(DIARY_SEARCH_QUERY, query)
                fragment.arguments = args
                return fragment
            }
        }

        inner class PhotoClickListener(var diarySequence: Int, var index: Int) : View.OnClickListener {
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
            private val diaryList: List<DiaryDto>,
            private val query: String?
    ) : androidx.fragment.app.FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

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
