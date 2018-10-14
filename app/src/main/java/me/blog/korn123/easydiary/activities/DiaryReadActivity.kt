package me.blog.korn123.easydiary.activities

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.simplemobiletools.commons.helpers.BaseConfig
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_diary_read.*
import kotlinx.android.synthetic.main.fragment_diary_read.*
import kotlinx.android.synthetic.main.layout_bottom_toolbar.*
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_read)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }

        val query = intent.getStringExtra(DIARY_SEARCH_QUERY)
        val diaryList: ArrayList<DiaryDto> = EasyDiaryDbHelper.readDiary(query, config.diarySearchQueryCaseSensitive)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, diaryList, query)
        val startPageIndex = when(savedInstanceState == null) {
            true -> mSectionsPagerAdapter.sequenceToPageIndex(intent.getIntExtra(DIARY_SEQUENCE, -1))
            false -> mSectionsPagerAdapter.sequenceToPageIndex(savedInstanceState?.getInt(DIARY_SEQUENCE, -1) ?: -1)
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

    override fun onSaveInstanceState(outState: Bundle?) {
        val fragment = mSectionsPagerAdapter.instantiateItem(diaryViewPager, diaryViewPager.currentItem)
        if (fragment is PlaceholderFragment) {
            outState?.putInt(DIARY_SEQUENCE, fragment.getSequence())
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
                    //                finish();
                    //                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                    this.onBackPressed()
                R.id.zoomIn -> {
                    config.settingFontSize = fontSize + 5
                    fragment.setFontsSize()
                }
                R.id.zoomOut -> {
                    config.settingFontSize = fontSize - 5
                    fragment.setFontsSize()
                }
                R.id.delete -> {
                    val positiveListener = DialogInterface.OnClickListener { dialogInterface, i ->
                        EasyDiaryDbHelper.deleteDiary(fragment.getSequence())
                        finish()
                    }
                    val negativeListener = DialogInterface.OnClickListener { dialogInterface, i -> }
                    showAlertDialog(getString(R.string.delete_confirm), positiveListener, negativeListener)
                }
                R.id.edit -> {
                    val updateDiaryIntent = Intent(this@DiaryReadActivity, DiaryUpdateActivity::class.java)
                    updateDiaryIntent.putExtra(DIARY_SEQUENCE, fragment.getSequence())
                    //                startActivity(updateDiaryIntent);
                    TransitionHelper.startActivityWithTransition(this@DiaryReadActivity, updateDiaryIntent)
                }
                R.id.speechOutButton -> textToSpeech(fragment.getDiaryContents())
                R.id.postCard -> {
                    val postCardIntent = Intent(this@DiaryReadActivity, PostCardActivity::class.java)
                    postCardIntent.putExtra(DIARY_SEQUENCE, fragment.getSequence())
                    //                startActivityForResult(postCardIntent, Constants.REQUEST_CODE_BACKGROUND_COLOR_PICKER);
                    TransitionHelper.startActivityWithTransition(this@DiaryReadActivity, postCardIntent)
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.diary_read, menu)
        return true
    }
    
    private fun setupViewPager() {
        // Set up the ViewPager with the sections adapter.
        diaryViewPager.adapter = mSectionsPagerAdapter
        diaryViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
                        setTarget(ViewTarget(R.id.zoomIn, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.create_diary_showcase_title_5))
                        setContentText(getString(R.string.create_diary_showcase_message_5))
                    }
                    3 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.zoomOut, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.create_diary_showcase_title_6))
                        setContentText(getString(R.string.create_diary_showcase_message_6))
                    }
                    4 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.edit, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_2))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_2))
                    }
                    5 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.speechOutButton, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_3))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_3))
                    }
                    6 -> {
                        setButtonPosition(centerParams)
                        setTarget(ViewTarget(R.id.postCard, this@DiaryReadActivity))
                        setContentTitle(getString(R.string.read_diary_detail_showcase_title_4))
                        setContentText(getString(R.string.read_diary_detail_showcase_message_4))
                    }
                    7 -> hide()
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

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {
        private var mPrimaryColor = 0
        private lateinit var mRootView: ViewGroup
        private lateinit var realmInstance: Realm

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            realmInstance = EasyDiaryDbHelper.getInstance()
        }
        
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            mRootView = inflater.inflate(R.layout.fragment_diary_read, container, false) as ViewGroup
            return mRootView
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)

            bottomToolbar.setOnClickListener {
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
        }

        override fun onResume() {
            super.onResume()
            mRootView?.let {
                context?.updateTextColors(it,0,0)
                context?.updateAppViews(it)
            }
            initContents()
            initBottomContainer()
            setFontsTypeface()
            setFontsSize()
        }

        override fun onDestroy() {
            super.onDestroy()
            realmInstance.close()
        }

        fun getSequence() = arguments?.getInt(DIARY_SEQUENCE) ?: -1
        
        fun getDiaryContents(): String = diaryContents.text.toString() 
        
        private fun initContents() {
            val diaryDto = EasyDiaryDbHelper.readDiaryBy(realmInstance, getSequence())
            if (StringUtils.isEmpty(diaryDto.title)) {
                diaryTitle.visibility = View.GONE
            }
            diaryTitle.text = diaryDto.title
            diaryContents.text = diaryDto.contents
            date.text = DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
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
            EasyDiaryUtils.initWeatherView(weather, weatherFlag)

            // TODO fixme elegance
            val photoCount = diaryDto.photoUris?.size ?: 0 
            if (photoCount > 0) {
                bottomTitle.text = String.format(getString(R.string.attached_photo_count), photoCount)
                bottomToolbar.visibility = View.VISIBLE
                photoContainerScrollView.visibility = View.VISIBLE
                val onClickListener = View.OnClickListener {
                    val photoViewPager = Intent(context, PhotoViewPagerActivity::class.java)
                    photoViewPager.putExtra(DIARY_SEQUENCE, getSequence())
                    startActivity(photoViewPager)
                }

                if (photoContainer.childCount > 0) photoContainer.removeAllViews()
                context?.let { appContext ->
                    val thumbnailSize = appContext.config.settingThumbnailSize
                    diaryDto.photoUris?.map {
                        
                        val bitmap = EasyDiaryUtils.photoUriToDownSamplingBitmap(appContext, it, 0, thumbnailSize.toInt() - 5, thumbnailSize.toInt() - 5)
                        val imageView = ImageView(context)
                        val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(appContext, thumbnailSize), CommonUtils.dpToPixel(appContext, thumbnailSize))
                        layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(appContext, 3F), 0)
                        imageView.layoutParams = layoutParams
//                        imageView.setBackgroundResource(R.drawable.bg_card_thumbnail)
                        val drawable = ContextCompat.getDrawable(appContext, R.drawable.bg_card_thumbnail)
                        val gradient = drawable as GradientDrawable
                        gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, THUMBNAIL_BACKGROUND_ALPHA))
//                        gradient.setColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(appContext, android.R.color.white), THUMBNAIL_BACKGROUND_ALPHA))
                        imageView.background = gradient
                        imageView.setImageBitmap(bitmap)
                        imageView.scaleType = ImageView.ScaleType.CENTER
                        photoContainer.addView(imageView)
                        imageView.setOnClickListener(onClickListener)
                    }
                }
            } else {
                bottomToolbar.visibility = View.GONE
                photoContainerScrollView.visibility = View.GONE
            }
        }

        private fun initBottomContainer() {
            context?.let {
                mPrimaryColor = BaseConfig(it).primaryColor
            }
        }

        fun setFontsTypeface() {
            activity?.let { it ->
                FontUtils.setFontsTypeface(it, it.assets, "", mRootView)    
            }
        }

        fun setFontsSize() {
            context?.let {
                it.initTextSize(mRootView, it)
            }
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
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(
            fm: FragmentManager,
            private val diaryList: ArrayList<DiaryDto>,
            private val query: String?
    ) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(diaryList[position].sequence, query)
        }

        fun sequenceToPageIndex(sequence: Int): Int {
            var pageIndex = 0
            if (sequence > -1) {
                for (i in diaryList.indices) {
                    if (diaryList[i].sequence == sequence) {
                        pageIndex = i
                        break
                    }
                }
            }
            return pageIndex
        }

        override fun getCount(): Int {
            return diaryList.size
        }
    }
}
