package me.blog.korn123.easydiary.activities

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v4.view.ViewPager
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import butterknife.ButterKnife
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.simplemobiletools.commons.helpers.BaseConfig
import kotlinx.android.synthetic.main.activity_diary_read.*
import kotlinx.android.synthetic.main.fragment_diary_read.*
import me.blog.korn123.commons.constants.Constants
import me.blog.korn123.commons.utils.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils
import java.io.FileNotFoundException
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
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mTextToSpeech: TextToSpeech? = null
    private var mShowcaseView: ShowcaseView? = null
    private var mShowcaseIndex = 1

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, intent.getStringExtra(Constants.DIARY_SEARCH_QUERY))
        val startPageIndex = when(savedInstanceState == null) {
            true -> mSectionsPagerAdapter?.sequenceToPageIndex(intent.getIntExtra(Constants.DIARY_SEQUENCE, -1)) ?: -1
            false -> mSectionsPagerAdapter?.sequenceToPageIndex(savedInstanceState?.getInt(Constants.DIARY_SEQUENCE, -1) ?: -1) ?: -1
        }

        setContentView(R.layout.activity_diary_read)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }

        setupViewPager()
        Handler().post { diaryViewPager.setCurrentItem(startPageIndex, false) }
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
        val fragment = mSectionsPagerAdapter?.getItem(diaryViewPager.currentItem)
        if (fragment is PlaceholderFragment) {
            outState?.putInt(Constants.DIARY_SEQUENCE, fragment.getSequence())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = mSectionsPagerAdapter?.getItem(diaryViewPager.currentItem)
        val fontSize = config.settingFontSize
        if (fragment is PlaceholderFragment) {
            when (item.itemId) {
                android.R.id.home ->
                    //                finish();
                    //                this.overridePendingTransition(R.anim.anim_left_to_center, R.anim.anim_center_to_right);
                    this.onBackPressed()
                R.id.zoomIn -> {
                    CommonUtils.saveFloatPreference(this@DiaryReadActivity, Constants.SETTING_FONT_SIZE, fontSize + 5)
                    fragment.setFontsSize()
                }
                R.id.zoomOut -> {
                    CommonUtils.saveFloatPreference(this@DiaryReadActivity, Constants.SETTING_FONT_SIZE, fontSize - 5)
                    fragment.setFontsSize()
                }
                R.id.delete -> {
                    val positiveListener = DialogInterface.OnClickListener { dialogInterface, i ->
                        EasyDiaryDbHelper.deleteDiary(fragment.getSequence())
                        finish()
                    }
                    val negativeListener = DialogInterface.OnClickListener { dialogInterface, i -> }
                    DialogUtils.showAlertDialog(this@DiaryReadActivity, getString(R.string.delete_confirm), positiveListener, negativeListener)
                }
                R.id.edit -> {
                    val updateDiaryIntent = Intent(this@DiaryReadActivity, DiaryUpdateActivity::class.java)
                    updateDiaryIntent.putExtra(Constants.DIARY_SEQUENCE, fragment.getSequence())
                    //                startActivity(updateDiaryIntent);
                    TransitionHelper.startActivityWithTransition(this@DiaryReadActivity, updateDiaryIntent)
                }
                R.id.speechOutButton -> textToSpeech(fragment.getDiaryContents())
                R.id.postCard -> {
                    val postCardIntent = Intent(this@DiaryReadActivity, PostCardActivity::class.java)
                    postCardIntent.putExtra(Constants.DIARY_SEQUENCE, fragment.getSequence())
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
                val fragment = mSectionsPagerAdapter?.getItem(diaryViewPager.currentItem)
                fragment?.let {
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
                .singleShot(Constants.SHOWCASE_SINGLE_SHOT_READ_DIARY_DETAIL_NUMBER.toLong())
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
        private var mRootView: ViewGroup? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            mRootView = inflater.inflate(R.layout.fragment_diary_read, container, false) as ViewGroup
            return mRootView
        }

        override fun onResume() {
            super.onResume()
            mRootView?.let {
                context?.updateTextColors(it,0,0)
            }
            initContents()
            initBottomContainer()
            setFontsTypeface()
            setFontsSize()
        }

        fun getSequence() = arguments?.getInt(Constants.DIARY_SEQUENCE) ?: -1
        
        fun getDiaryContents(): String = diaryContents.text.toString() 
        
        private fun initContents() {
            EasyDiaryDbHelper.readDiaryBy(getSequence())?.let { diaryDto ->
                if (StringUtils.isEmpty(diaryDto.title)) {
                    diaryTitle.visibility = View.GONE
                }
                diaryTitle.text = diaryDto.title
                diaryContents.text = diaryDto.contents
                date.text = DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
                initBottomContainer()

                val query = arguments?.getString(Constants.DIARY_SEARCH_QUERY)
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

                val weatherFlag = diaryDto.weather
                EasyDiaryUtils.initWeatherView(weather, weatherFlag)

                // TODO fixme elegance
                if (diaryDto.photoUris?.size ?: 0 > 0) {
                    photoContainerScrollView.visibility = View.VISIBLE
                    val onClickListener = View.OnClickListener {
                        val photoViewPager = Intent(context, PhotoViewPagerActivity::class.java)
                        photoViewPager.putExtra(Constants.DIARY_SEQUENCE, getSequence())
                        startActivity(photoViewPager)
                    }

                    if (photoContainer.childCount > 0) photoContainer.removeAllViews()
                    context?.let { appContext ->
                        diaryDto.photoUris?.map {
                            val uri = Uri.parse(it.photoUri)
                            val bitmap: Bitmap? = try {
                                BitmapUtils.decodeUri(appContext, uri, CommonUtils.dpToPixel(appContext, 70, 1), CommonUtils.dpToPixel(appContext, 65, 1), CommonUtils.dpToPixel(appContext, 45, 1))
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                                BitmapFactory.decodeResource(resources, R.drawable.question_shield)
                            } catch (se: SecurityException) {
                                se.printStackTrace()
                                BitmapFactory.decodeResource(resources, R.drawable.question_shield)
                            }

                            val imageView = ImageView(context)
                            val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(appContext, 70, 1), CommonUtils.dpToPixel(appContext, 50, 1))
                            layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(appContext, 3, 1), 0)
                            imageView.layoutParams = layoutParams
//                        imageView.setBackgroundResource(R.drawable.bg_card_thumbnail)
                            val drawable = ContextCompat.getDrawable(appContext, R.drawable.bg_card_thumbnail)
                            val gradient = drawable as GradientDrawable
                            gradient.setColor(ColorUtils.setAlphaComponent(mPrimaryColor, Constants.THUMBNAIL_BACKGROUND_ALPHA))
                            imageView.background = gradient
                            imageView.setImageBitmap(bitmap)
                            imageView.scaleType = ImageView.ScaleType.CENTER
                            photoContainer.addView(imageView)
                            imageView.setOnClickListener(onClickListener)
                        }
                    }
                } else {
                    photoContainerScrollView.visibility = View.GONE
                }
            }
        }

        private fun initBottomContainer() {
            context?.let {
                mPrimaryColor = BaseConfig(it).primaryColor
            }
        }

        fun setFontsTypeface() {
            FontUtils.setFontsTypeface(context, activity?.assets, null, diaryTitle, date, diaryContents)
        }

        fun setFontsSize() {
            context?.let {
                mRootView?.let { rootView ->
                    it.initTextSize(rootView, it)
                }
            }
        }

        companion object {
            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sequence: Int, query: String): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(Constants.DIARY_SEQUENCE, sequence)
                args.putString(Constants.DIARY_SEARCH_QUERY, query)
                fragment.arguments = args
                return fragment
            }
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager, query: String) : FragmentPagerAdapter(fm) {
        private val mDiaryList: List<DiaryDto>
        private val mFragmentList = ArrayList<PlaceholderFragment>()

        init {
            mDiaryList = EasyDiaryDbHelper.readDiary(query, CommonUtils.loadBooleanPreference(applicationContext, Constants.DIARY_SEARCH_QUERY_CASE_SENSITIVE))
            mDiaryList?.map {
                mFragmentList.add(PlaceholderFragment.newInstance(it.sequence, query))
            }
        }

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return mFragmentList[position]
        }

        fun sequenceToPageIndex(sequence: Int): Int {
            var pageIndex = 0
            if (sequence > -1) {
                for (i in mDiaryList.indices) {
                    if (mDiaryList[i].sequence == sequence) {
                        pageIndex = i
                        break
                    }
                }
            }
            return pageIndex
        }

        override fun getCount(): Int {
            return mDiaryList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return null
        }
    }
}
