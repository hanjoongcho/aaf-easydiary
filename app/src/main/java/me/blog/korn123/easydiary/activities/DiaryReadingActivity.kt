package me.blog.korn123.easydiary.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.view.isNotEmpty
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.FlavorUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityDiaryReadingBinding
import me.blog.korn123.easydiary.databinding.DialogHighlightKeywordBinding
import me.blog.korn123.easydiary.databinding.FragmentDiaryReadBinding
import me.blog.korn123.easydiary.databinding.PopupEncryptionBinding
import me.blog.korn123.easydiary.databinding.PopupMenuReadBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.EMPTY
import me.blog.korn123.easydiary.extensions.applyMarkDownPolicy
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.clearHoldOrientation
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.diaryRepository
import me.blog.korn123.easydiary.extensions.holdCurrentOrientation
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.isAccessFromOutside
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.pushMarkDown
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.startMainActivityWithClearTask
import me.blog.korn123.easydiary.extensions.updateAlertDialogWithIcon
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateCardViewPolicy
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.DIARY_ATTACH_PHOTO_INDEX
import me.blog.korn123.easydiary.helper.DIARY_CONTENTS_SCROLL_Y
import me.blog.korn123.easydiary.helper.DIARY_ENCRYPT_PASSWORD
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.DiaryComponentConstants
import me.blog.korn123.easydiary.helper.DiaryReadingConstants
import me.blog.korn123.easydiary.helper.SELECTED_SEARCH_QUERY
import me.blog.korn123.easydiary.helper.SELECTED_SYMBOL_SEQUENCE
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.helper.TreeConstants.IS_TREE_TIMELINE_LAUNCH_MODE_DEFAULT
import me.blog.korn123.easydiary.presentation.components.LoadingScreen
import me.blog.korn123.easydiary.presentation.diary.DiaryReadViewModel
import me.blog.korn123.easydiary.presentation.diary.DiaryViewModel
import me.blog.korn123.easydiary.presentation.diary.LinkedDiaryViewModel
import me.blog.korn123.easydiary.presentation.main.components.LegacyDiarySubItemCard
import me.blog.korn123.easydiary.presentation.photoviewer.PhotoViewPagerActivity
import me.blog.korn123.easydiary.presentation.settings.components.CategoryTitleCard
import me.blog.korn123.easydiary.presentation.theme.AppTheme
import me.blog.korn123.easydiary.presentation.tree.TreeTimelineActivity
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils.createAttachedPhotoViewForFlexBox
import me.blog.korn123.easydiary.utils.FontUtils
import me.blog.korn123.easydiary.utils.JasyptUtils
import java.util.Locale
import me.blog.korn123.easydiary.domain.model.Diary as DiaryDomain

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */
@AndroidEntryPoint
class DiaryReadingActivity : EasyDiaryActivity() {
    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var binding: ActivityDiaryReadingBinding
    private lateinit var popupEncryptionBinding: PopupEncryptionBinding
    private lateinit var dialogHighlightKeywordBinding: DialogHighlightKeywordBinding
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingSpeechText: String? = null
    private val diaryList: ArrayList<DiaryDomain> = arrayListOf()

    private fun getFragmentAt(position: Int = binding.diaryViewPager.currentItem): PlaceholderFragment? {
        val sequence = diaryList.getOrNull(position)?.diaryId ?: return null
        return supportFragmentManager.findFragmentByTag("f$sequence") as? PlaceholderFragment
            ?: supportFragmentManager.fragments.firstOrNull { fragment ->
                fragment is PlaceholderFragment && fragment.isAdded && fragment.getSequence() == sequence
            } as? PlaceholderFragment
    }

    private val selectValueLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val fragment = getFragmentAt() ?: return@registerForActivityResult
                val selectedValue = result.data?.getLongExtra("sequence", 0)

                showAlertDialog(
                    getString(R.string.link_entry_confirm),
                    { _, _ ->
                        lifecycleScope.launch {
                            selectedValue?.let {
                                diaryViewModel.findDiaryById(fragment.getSequence())?.let {
                                    if (it.linkedDiaries.contains(selectedValue)) {
                                        makeSnackBar(getString(R.string.link_entry_already_linked))
                                    } else {
                                        it.linkedDiaries.add(selectedValue)
                                        diaryRepository.updateDiaryWithPhotos(it)
                                        fragment.initContents()
                                    }
                                }
                            }
                        }
                    },
                    { _, _ ->
                        lifecycleScope.launch {
                            selectedValue?.let {
                                diaryViewModel.findDiaryById(selectedValue)?.let {
                                    if (it.linkedDiaries.contains(fragment.getSequence())) {
                                        makeSnackBar(getString(R.string.link_entry_already_linked))
                                    } else {
                                        it.linkedDiaries.add(fragment.getSequence())
                                        diaryRepository.updateDiaryWithPhotos(it)
                                        fragment.initContents()
                                    }
                                }
                            }
                        }
                    },
                    { _, _ -> },
                    DialogMode.INFO,
                    true,
                    getString(R.string.link_entry),
                    getString(R.string.link_entry_as_child),
                    getString(R.string.link_entry_as_parent),
                    getString(R.string.link_entry_later),
                )
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        popupEncryptionBinding = PopupEncryptionBinding.inflate(layoutInflater)
        dialogHighlightKeywordBinding = DialogHighlightKeywordBinding.inflate(layoutInflater)
        binding =
            ActivityDiaryReadingBinding.inflate(layoutInflater).apply {
                setContentView(root)
                setSupportActionBar(toolbar)
                supportActionBar?.run {
                    title = String.EMPTY
                    setDisplayHomeAsUpEnabled(true)
                }
                partialComposeLoadingScreen.composeView.setContent {
                    AppTheme {
                        AnimatedVisibility(
                            visible = diaryViewModel.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            LoadingScreen(message = diaryViewModel.loadingMessage)
                        }
                    }
                }
            }

        val query = intent.getStringExtra(SELECTED_SEARCH_QUERY)
        val symbolSequence = intent.getIntExtra(SELECTED_SYMBOL_SEQUENCE, 0)
        sectionsPagerAdapter = SectionsPagerAdapter(this, diaryList, query)
        setupViewPager()

        lifecycleScope.launch {
            diaryViewModel.isLoading = true
            diaryList.run {
                clear()
                addAll(
                    when (intent.getStringExtra(DiaryComponentConstants.MODE_FLAG) == null) {
                        true -> {
                            diaryViewModel.findDiary(
                                query,
                                config.diarySearchQueryCaseSensitive,
                                0,
                                0,
                                symbolSequence,
                            )
                        }

                        false -> {
                            diaryViewModel.applyFilter(
                                intent.getStringExtra(
                                    DiaryComponentConstants.MODE_FLAG,
                                ),
                            )
                        }
                    },
                )
            }

            sectionsPagerAdapter.notifyDataSetChanged()

            val targetIndex =
                if (savedInstanceState == null) {
                    sectionsPagerAdapter.sequenceToPageIndex(
                        intent.getLongExtra(
                            DIARY_SEQUENCE,
                            -1,
                        ),
                    )
                } else {
                    sectionsPagerAdapter.sequenceToPageIndex(
                        savedInstanceState.getLong(
                            DIARY_SEQUENCE,
                            -1,
                        ),
                    )
                }

            if (targetIndex >= 0) {
                binding.diaryViewPager.post {
                    binding.diaryViewPager.setCurrentItem(targetIndex, false)
                }
            }

            diaryViewModel.isLoading = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (diaryList.isNotEmpty()) {
            sectionsPagerAdapter.notifyDataSetChanged()
            getFragmentAt()?.run {
                if (isAdded) initContents()
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyModule()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        getFragmentAt()?.let { fragment ->
            outState.putLong(DIARY_SEQUENCE, fragment.getSequence())
        }
        super.onSaveInstanceState(outState)
    }

    var dialogSearch: AlertDialog? = null
    var searchIndexes = arrayListOf<Int>()
    var currentIndexesSequence = 0

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val fragment = getFragmentAt()
        if (fragment is PlaceholderFragment) {
            when (item.itemId) {
                android.R.id.home -> {
                    if (isAccessFromOutside()) {
                        startMainActivityWithClearTask()
                    } else {
                        finishActivityWithPauseLock()
                    }
                }

                R.id.edit -> {
                    lifecycleScope.launch {
                        if (fragment.isEncryptContents()) {
                            showEncryptPagePopup(fragment, DiaryReadingConstants.EDITING) { inputPass ->
                                startEditing(fragment, inputPass)
                            }
                        } else {
                            startEditing(fragment)
                        }
                    }
                }

                R.id.speechOutButton -> {
                    initModule(fragment.getDiaryContents())
                }

                R.id.popupMenu -> {
                    createCustomOptionMenu()
                }
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_diary_reading, menu)
        return true
    }

    private fun startEditing(
        fragment: PlaceholderFragment,
        inputPass: String? = null,
    ) {
        val updateDiaryIntent =
            Intent(this@DiaryReadingActivity, DiaryEditingActivity::class.java).apply {
                putExtra(DIARY_SEQUENCE, fragment.getSequence())
                putExtra(DIARY_CONTENTS_SCROLL_Y, fragment.getContentsPositionY())
                inputPass?.let {
                    putExtra(DIARY_ENCRYPT_PASSWORD, it)
                }
            }
        TransitionHelper.startActivityWithTransition(this@DiaryReadingActivity, updateDiaryIntent)
    }

    private fun showEncryptPagePopup(
        fragment: PlaceholderFragment,
        workMode: String,
        callback: ((inputPass: String) -> Unit)? = null,
    ) {
        popupEncryptionBinding.run {
            updateDrawableColorInnerCardView(closePopup)
            var inputPass = String.EMPTY
            var confirmPass = String.EMPTY
            holdCurrentOrientation()
            val popupView =
                root.apply {
                    setBackgroundColor(ColorUtils.setAlphaComponent(config.backgroundColor, 250))
                    decMode1.setTextColor(config.textColor)
                    decMode2.setTextColor(config.textColor)
                }

            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val popupWindow =
                PopupWindow(popupView, width, height, true).apply {
                    showAtLocation(
                        findViewById<ViewGroup>(android.R.id.content).rootView,
                        Gravity.CENTER,
                        0,
                        0,
                    )
                }

            closePopup.setOnClickListener {
                it.postDelayed({
                    popupWindow.dismiss()
                    clearHoldOrientation()
                }, 300)
            }

            configureEncryptionWorkMode(workMode)

            val onclickListener =
                View.OnClickListener { v ->
                    clearPassView()
                    inputPass = handleKeypadClick(v.id, inputPass)
                    updatePassIndicator(inputPass)

                    if (inputPass.length == 6) {
                        val (newConfirmPass, newInputPass) =
                            handlePinSubmission(
                                inputPass = inputPass,
                                confirmPass = confirmPass,
                                workMode = workMode,
                                fragment = fragment,
                                callback = callback,
                                popupWindow = popupWindow,
                            )
                        confirmPass = newConfirmPass
                        inputPass = newInputPass
                        clearPassView()
                    }
                }

            setupKeypadListeners(onclickListener)

            run {
                updateTextColors(this.root)
                updateAppViews(this.root)
                updateCardViewPolicy(this.root)
            }
            FontUtils.setFontsTypeface(applicationContext, null, popupView, true)
        }
    }

    private fun PopupEncryptionBinding.configureEncryptionWorkMode(workMode: String) {
        when (workMode) {
            DiaryReadingConstants.ENCRYPTION -> {
                description.text = getString(R.string.diary_encryption_title)
                guideMessage.text = getString(R.string.diary_encryption_guide)
                decMode.visibility = View.GONE
            }

            DiaryReadingConstants.DECRYPTION -> {
                description.text = getString(R.string.diary_decryption_title)
                guideMessage.text = getString(R.string.diary_decryption_guide)
                decMode.visibility = View.VISIBLE
            }

            DiaryReadingConstants.EDITING -> {
                description.text = getString(R.string.diary_decryption_title)
                guideMessage.text = getString(R.string.diary_decryption_guide_before_editing)
                decMode.visibility = View.GONE
            }
        }
        EasyDiaryUtils.warningString(guideMessage)
    }

    private fun PopupEncryptionBinding.clearPassView() {
        pass1.text = String.EMPTY
        pass2.text = String.EMPTY
        pass3.text = String.EMPTY
        pass4.text = String.EMPTY
        pass5.text = String.EMPTY
        pass6.text = String.EMPTY
    }

    private fun PopupEncryptionBinding.updatePassIndicator(inputPass: String) {
        if (inputPass.isNotEmpty()) pass1.text = "*"
        if (inputPass.length > 1) pass2.text = "*"
        if (inputPass.length > 2) pass3.text = "*"
        if (inputPass.length > 3) pass4.text = "*"
        if (inputPass.length > 4) pass5.text = "*"
        if (inputPass.length > 5) pass6.text = "*"
    }

    private fun handleKeypadClick(
        viewId: Int,
        currentInput: String,
    ): String {
        var input = currentInput
        when (viewId) {
            R.id.button1 -> {
                input += "1"
            }

            R.id.button2 -> {
                input += "2"
            }

            R.id.button3 -> {
                input += "3"
            }

            R.id.button4 -> {
                input += "4"
            }

            R.id.button5 -> {
                input += "5"
            }

            R.id.button6 -> {
                input += "6"
            }

            R.id.button7 -> {
                input += "7"
            }

            R.id.button8 -> {
                input += "8"
            }

            R.id.button9 -> {
                input += "9"
            }

            R.id.button0 -> {
                input += "0"
            }

            R.id.buttonDel -> {
                if (input.isNotEmpty()) {
                    input = input.substring(0, input.length - 1)
                }
            }
        }
        return input
    }

    private fun PopupEncryptionBinding.handlePinSubmission(
        inputPass: String,
        confirmPass: String,
        workMode: String,
        fragment: PlaceholderFragment,
        callback: ((inputPass: String) -> Unit)?,
        popupWindow: PopupWindow,
    ): Pair<String, String> {
        var newConfirmPass = confirmPass
        var newInputPass = inputPass

        when {
            newConfirmPass.length == 6 -> {
                when (newConfirmPass == newInputPass) {
                    true -> {
                        fragment.encryptData(newInputPass)
                        popupWindow.dismiss()
                        clearHoldOrientation()
                    }

                    false -> {
                        guideMessage.text = getString(R.string.diary_pin_number_confirm_error)
                    }
                }
                newInputPass = String.EMPTY
                newConfirmPass = String.EMPTY
            }

            workMode == DiaryReadingConstants.DECRYPTION -> {
                lifecycleScope.launch {
                    Log.i(AAF_TEST, "${fragment.getPasswordHash()},$newInputPass, ${JasyptUtils.sha256(newInputPass)}, ${fragment.getPasswordHash() == JasyptUtils.sha256(newInputPass)}")
                    when (fragment.getPasswordHash() == JasyptUtils.sha256(newInputPass)) {
                        true -> {
                            if (decMode1.isChecked) {
                                fragment.decryptDataOnce(newInputPass)
                            } else {
                                fragment.decryptData(newInputPass)
                            }
                            popupWindow.dismiss()
                            clearHoldOrientation()
                        }

                        false -> {
                            guideMessage.text = getString(R.string.diary_pin_number_verification_error)
                        }
                    }
                    newInputPass = String.EMPTY
                }
            }

            workMode == DiaryReadingConstants.EDITING -> {
                lifecycleScope.launch {
                    when (fragment.getPasswordHash() == JasyptUtils.sha256(newInputPass)) {
                        true -> {
                            callback?.invoke(newInputPass)
                            popupWindow.dismiss()
                            clearHoldOrientation()
                        }

                        false -> {
                            guideMessage.text = getString(R.string.diary_pin_number_verification_error)
                        }
                    }
                    newInputPass = String.EMPTY
                }
            }

            else -> {
                newConfirmPass = newInputPass
                newInputPass = String.EMPTY
                guideMessage.text = getString(R.string.diary_pin_number_confirm_guide)
            }
        }
        return Pair(newConfirmPass, newInputPass)
    }

    private fun PopupEncryptionBinding.setupKeypadListeners(listener: View.OnClickListener) {
        button1.setOnClickListener(listener)
        button2.setOnClickListener(listener)
        button3.setOnClickListener(listener)
        button4.setOnClickListener(listener)
        button5.setOnClickListener(listener)
        button6.setOnClickListener(listener)
        button7.setOnClickListener(listener)
        button8.setOnClickListener(listener)
        button9.setOnClickListener(listener)
        button0.setOnClickListener(listener)
        buttonDel.setOnClickListener(listener)
    }

    private fun handleFindClick(fragment: PlaceholderFragment) {
        fun highLight() {
            when (
                dialogHighlightKeywordBinding.searchKeywordQuery.text
                    .toString()
                    .isNotEmpty()
            ) {
                true -> {
                    fragment.highlightDiary(
                        dialogHighlightKeywordBinding.searchKeywordQuery.text.toString(),
                        true,
                    )
                }

                false -> {
                    fragment.clearHighLight()
                }
            }
        }

        fun updateLabel() {
            dialogHighlightKeywordBinding.indexes.text =
                getString(R.string.search_index_format, currentIndexesSequence, searchIndexes.size)
        }
        dialogSearch?.show() ?: run {
            val builder =
                AlertDialog.Builder(this@DiaryReadingActivity).apply {
                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                    }
                }
            dialogSearch = builder.create()
            dialogHighlightKeywordBinding.run {
                next.setOnClickListener {
                    if (currentIndexesSequence < searchIndexes.size) {
                        currentIndexesSequence++
                        fragment.moveScroll(
                            searchKeywordQuery.text.toString(),
                            searchIndexes[currentIndexesSequence - 1],
                        )
                        updateLabel()
                    }
                }
                previous.setOnClickListener {
                    if (currentIndexesSequence > 1) {
                        currentIndexesSequence--
                        fragment.moveScroll(
                            searchKeywordQuery.text.toString(),
                            searchIndexes[currentIndexesSequence - 1],
                        )
                        updateLabel()
                    }
                }
                searchKeywordQuery.addTextChangedListener(
                    object : TextWatcher {
                        override fun beforeTextChanged(
                            charSequence: CharSequence,
                            i: Int,
                            i1: Int,
                            i2: Int,
                        ) {
                        }

                        override fun onTextChanged(
                            charSequence: CharSequence,
                            i: Int,
                            i1: Int,
                            i2: Int,
                        ) {
                            currentIndexesSequence = 0
                            searchIndexes.clear()
                            searchIndexes.addAll(
                                EasyDiaryUtils.searchWordIndexes(
                                    fragment.getContents(),
                                    charSequence.toString(),
                                ),
                            )
                            highLight()
                            if (searchIndexes.isNotEmpty()) currentIndexesSequence = 1
                            updateLabel()
                        }

                        override fun afterTextChanged(editable: Editable) {}
                    },
                )
                root.run {
                    updateTextColors(this)
                    FontUtils.setFontsTypeface(
                        applicationContext,
                        null,
                        this,
                        customLineSpacing,
                    )
                    dialogSearch?.let { dialog ->
                        updateAlertDialogWithIcon(
                            DialogMode.INFO,
                            dialog,
                            null,
                            this,
                            null,
                            200,
                        )
                    }
                }
                updateLabel()
            }
        }
    }

    private fun setupViewPager() {
        binding.diaryViewPager.adapter = sectionsPagerAdapter
        binding.diaryViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    getFragmentAt(position)
                }
            },
        )
    }

    private fun initModule(textToSpeak: String? = null) {
        textToSpeak?.let {
            pendingSpeechText = it
        }
        if (textToSpeech == null) {
            textToSpeech =
                TextToSpeech(this@DiaryReadingActivity) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech?.run {
                            language = Locale.getDefault()
                            setPitch(1.3f)
                            setSpeechRate(1f)
                        }
                        isTtsInitialized = true
                        pendingSpeechText?.let { text ->
                            textToSpeech(text)
                            pendingSpeechText = null
                        }
                    } else {
                        isTtsInitialized = false
                    }
                }
        } else if (isTtsInitialized) {
            pendingSpeechText?.let { text ->
                textToSpeech(text)
                pendingSpeechText = null
            }
        }
    }

    private fun destroyModule() {
        textToSpeech?.run {
            stop()
            shutdown()
            textToSpeech = null
        }
        isTtsInitialized = false
        pendingSpeechText = null
    }

    private fun textToSpeech(text: String) {
        ttsGreater21(text)
    }

    private fun ttsGreater21(text: String) {
        val utteranceId = this.hashCode().toString() + ""
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun createCustomOptionMenu() {
        val pmrBinding = PopupMenuReadBinding.inflate(layoutInflater)
        var popupWindow: PopupWindow? = null
        val popupView =
            pmrBinding.root.apply {
                updateAppViews(this)
                updateTextColors(this)
                FontUtils.setFontsTypeface(applicationContext, null, this, true)
                val fragment = getFragmentAt() ?: return
                pmrBinding.run {
                    lifecycleScope.launch {
                        when (fragment.isEncryptContents()) {
                            true -> decryptData.visibility = View.VISIBLE
                            false -> encryptData.visibility = View.VISIBLE
                        }
                    }
                    val itemClickListener =
                        View.OnClickListener { view ->
                            when (view.id) {
                                R.id.delete -> {
                                    val positiveListener =
                                        DialogInterface.OnClickListener { _, _ ->
                                            lifecycleScope.launch {
                                                diaryRepository.deleteDiaryById(fragment.getSequence())
                                                TransitionHelper.finishActivityWithTransition(this@DiaryReadingActivity)
                                            }
                                        }
                                    showAlertDialog(
                                        getString(R.string.delete_confirm),
                                        positiveListener,
                                        { _, _ -> },
                                        DialogMode.WARNING,
                                        true,
                                        getString(R.string.delete),
                                        getString(R.string.delete),
                                    )
                                }

                                R.id.postcard -> {
                                    val postCardIntent =
                                        Intent(
                                            this@DiaryReadingActivity,
                                            PostcardActivity::class.java,
                                        )
                                    postCardIntent.putExtra(DIARY_SEQUENCE, fragment.getSequence())
                                    TransitionHelper.startActivityWithTransition(
                                        this@DiaryReadingActivity,
                                        postCardIntent,
                                    )
                                }

                                R.id.encryptData -> {
                                    showEncryptPagePopup(fragment, DiaryReadingConstants.ENCRYPTION)
                                }

                                R.id.decryptData -> {
                                    showEncryptPagePopup(fragment, DiaryReadingConstants.DECRYPTION)
                                }

                                R.id.push -> {
                                    lifecycleScope.launch {
                                        diaryViewModel.findDiaryById(fragment.getSequence())?.let {
                                            val title = it.title
                                            val contents = it.contents
                                            pushMarkDown(title, contents)
                                        }
                                    }
                                }

                                R.id.linkEntry -> {
                                    Intent(
                                        this@DiaryReadingActivity,
                                        TreeTimelineActivity::class.java,
                                    ).apply {
                                        putExtra(IS_TREE_TIMELINE_LAUNCH_MODE_DEFAULT, false)
                                        selectValueLauncher.launch(this)
                                    }
                                }

                                R.id.find -> {
                                    handleFindClick(fragment)
                                }
                            }
                            popupWindow?.dismiss()
                        }

                    postcard.setOnClickListener(itemClickListener)
                    encryptData.setOnClickListener(itemClickListener)
                    decryptData.setOnClickListener(itemClickListener)
                    delete.setOnClickListener(itemClickListener)
                    linkEntry.setOnClickListener(itemClickListener)

                    updateDrawableColorInnerCardView(imgPostcard)
                    updateDrawableColorInnerCardView(imgEncryptData)
                    updateDrawableColorInnerCardView(imgDecryptData)
                    updateDrawableColorInnerCardView(imgDelete)
                    updateDrawableColorInnerCardView(imgLinkEntry)

                    @Suppress("KotlinConstantConditions")
                    if (BuildConfig.FLAVOR == "lab") {
                        push.visibility = View.VISIBLE
                        push.setOnClickListener(itemClickListener)
                        updateDrawableColorInnerCardView(imgPush, Color.Red.toArgb())

                        find.visibility = View.VISIBLE
                        find.setOnClickListener(itemClickListener)
                        updateDrawableColorInnerCardView(imgFind, Color.Red.toArgb())
                    }
                }
            }
        popupWindow = EasyDiaryUtils.openCustomOptionMenu(popupView, findViewById(R.id.popupMenu))
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    @AndroidEntryPoint
    class PlaceholderFragment : androidx.fragment.app.Fragment() {
        private lateinit var rootView: ViewGroup
        private lateinit var binding: FragmentDiaryReadBinding
        private val viewModel: DiaryReadViewModel by viewModels()
        private var primaryColor = 0
        private var storedContents: String? = null

        private var linkedDiaryViewModel: LinkedDiaryViewModel = LinkedDiaryViewModel()
        private val diaryViewModel: DiaryViewModel by viewModels()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_diary_read, container, false)
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            rootView = binding.mainHolder
            return rootView
        }

        override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?,
        ) {
            super.onViewCreated(view, savedInstanceState)
            requireContext().changeDrawableIconColor(
                config.primaryColor,
                R.drawable.ic_map_marker_2,
            )
            rootView.let {
                context?.run {
                    updateTextColors(it, 0, 0)
                    updateAppViews(it)
                    updateCardViewPolicy(it)
                }
            }

            initBottomContainer()
            setFontsTypeface()
            setFontsSize()
            initContents()

            binding.composeView.setContent {
                AppTheme {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .navigationBarsPadding(),
                    ) {
                        val parentDiaries by linkedDiaryViewModel.parentDiaries.collectAsState()
                        if (parentDiaries.isNotEmpty()) {
                            CategoryTitleCard(
                                title = getString(R.string.link_entry_category_parent),
                                marginTop = 0,
                            )
                            for (parentDiary in parentDiaries) {
                                LegacyDiarySubItemCard(
                                    diary = parentDiary,
                                    itemClickCallback = {
                                        val intent =
                                            Intent(
                                                requireContext(),
                                                DiaryReadingActivity::class.java,
                                            ).apply {
                                                putExtra(DIARY_SEQUENCE, parentDiary.diaryId)
                                            }
                                        TransitionHelper.startActivityWithTransition(
                                            requireActivity(),
                                            intent,
                                        )
                                    },
                                    itemLongClickCallback = {
                                        requireActivity().showAlertDialog(
                                            "Are you sure you want to unlink this diary?",
                                            { _, _ ->
                                                lifecycleScope.launch {
                                                    diaryViewModel
                                                        .findDiaryById(parentDiary.diaryId)
                                                        ?.run {
                                                            linkedDiaries
                                                                .filter { it != getSequence() }
                                                                .let { newLinkedDiaries ->
                                                                    lifecycleScope.launch {
                                                                        linkedDiaries.clear()
                                                                        linkedDiaries.addAll(
                                                                            newLinkedDiaries,
                                                                        )
                                                                        requireContext().diaryRepository.updateDiaryWithPhotos(this@run)
                                                                        initContents()
                                                                    }
                                                                }
                                                        }
                                                }
                                            },
                                            { _, _ -> },
                                            DialogMode.INFO,
                                        )
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        val childDiaries by linkedDiaryViewModel.childDiaries.collectAsState()
                        if (childDiaries.isNotEmpty()) {
                            CategoryTitleCard(
                                title = getString(R.string.link_entry_category_child),
                                marginTop = 0,
                            )
                            for (childDiary in childDiaries) {
                                LegacyDiarySubItemCard(
                                    diary = childDiary,
                                    itemClickCallback = {
                                        val intent =
                                            Intent(
                                                requireContext(),
                                                DiaryReadingActivity::class.java,
                                            ).apply {
                                                putExtra(DIARY_SEQUENCE, childDiary.diaryId)
                                            }
                                        TransitionHelper.startActivityWithTransition(
                                            requireActivity(),
                                            intent,
                                        )
                                    },
                                    itemLongClickCallback = {
                                        requireActivity().showAlertDialog(
                                            "Are you sure you want to unlink this diary?",
                                            { _, _ ->
                                                lifecycleScope.launch {
                                                    diaryViewModel
                                                        .findDiaryById(getSequence())
                                                        ?.run {
                                                            linkedDiaries
                                                                .filter { it != childDiary.diaryId }
                                                                .let { newLinkedDiaries ->
                                                                    linkedDiaries.clear()
                                                                    linkedDiaries.addAll(
                                                                        newLinkedDiaries,
                                                                    )
                                                                    requireContext().diaryRepository.updateDiaryWithPhotos(
                                                                        this@run,
                                                                    )
                                                                    initContents()
                                                                }
                                                        }
                                                }
                                            },
                                            { _, _ -> },
                                            DialogMode.INFO,
                                        )
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }

        fun getSequence() = arguments?.getLong(DIARY_SEQUENCE) ?: -1L

        fun getDiaryContents(): String = binding.diaryContents.text.toString()

        suspend fun isEncryptContents() = diaryViewModel.findDiaryById(getSequence())?.isEncrypt ?: false

        suspend fun getPasswordHash() = diaryViewModel.findDiaryById(getSequence())?.encryptKeyHash

        fun highlightDiary(
            query: String,
            moveScroll: Boolean = false,
        ) {
            context?.config?.run {
                if (diarySearchQueryCaseSensitive) {
                    EasyDiaryUtils.highlightString(binding.diaryTitle, query)
                    EasyDiaryUtils.highlightString(binding.diaryContents, query)
                } else {
                    EasyDiaryUtils.highlightStringIgnoreCase(binding.diaryTitle, query)
                    EasyDiaryUtils.highlightStringIgnoreCase(binding.diaryContents, query)
                }
            }

            if (moveScroll) {
                moveScroll(query)
            }
        }

        fun moveScroll(
            query: String,
            startIndex: Int = 0,
        ) {
            val index =
                binding.diaryContents.text
                    .toString()
                    .indexOf(query, startIndex, true)
            if (index > 0) {
                val layout = binding.diaryContents.layout
                binding.scrollDiaryContents.scrollTo(
                    0,
                    layout.getLineTop(layout.getLineForOffset(index)),
                )
            }
        }

        fun getContents() = binding.diaryContents.text.toString()

        fun clearHighLight() {
            binding.diaryTitle.run {
                this.text = SpannableString(this.text).apply { EasyDiaryUtils.removeSpans(this) }
            }
            binding.diaryContents.run {
                this.text = SpannableString(this.text).apply { EasyDiaryUtils.removeSpans(this) }
            }
        }

        fun initContents() {
            lifecycleScope.launch {
                val diaryDto = diaryViewModel.findDiaryById(getSequence()) ?: return@launch
                binding.run {
                    diaryTitle.visibility =
                        if (diaryDto.title.isEmpty()) View.GONE else View.VISIBLE
                    diaryTitle.text = diaryDto.title

                    EasyDiaryUtils.boldString(requireContext(), diaryTitle)
                    if (storedContents != diaryDto.contents) {
                        requireActivity().applyMarkDownPolicy(diaryContents, diaryDto.contents)
                        storedContents = diaryDto.contents
                        Handler(Looper.getMainLooper()).post { scrollDiaryContents.scrollY = 0 }
                    }
                    date.text =
                        when (diaryDto.isAllDay) {
                            true -> {
                                DateUtils.getDateStringFromTimeMillis(diaryDto.currentTimeMillis)
                            }

                            false -> {
                                DateUtils.getDateTimeStringForceFormatting(
                                    diaryDto.currentTimeMillis,
                                    requireContext(),
                                )
                            }
                        }
                    initBottomContainer()

                    arguments?.getString(SELECTED_SEARCH_QUERY)?.let { query ->
                        if (query.isNotEmpty()) {
                            highlightDiary(query)
                        }
                    }

                    val weatherFlag = diaryDto.symbolSequence
                    FlavorUtils.initWeatherView(requireContext(), weather, weatherFlag)

                    val photoCount = diaryDto.photoUris.size
                    binding.run {
                        if (photoCount > 0) {
                            photoContainerFlexBox.visibility = View.VISIBLE
                            if (photoContainerFlexBox.isNotEmpty()) photoContainerFlexBox.removeAllViews()

                            context?.let { _ ->
                                diaryDto
                                    .photoUrisWithEncryptionPolicy()
                                    ?.forEachIndexed { index, item ->
                                        photoContainerFlexBox.addView(
                                            createAttachedPhotoViewForFlexBox(
                                                requireActivity(),
                                                item,
                                                photoCount,
                                            ).apply {
                                                setOnClickListener(
                                                    PhotoClickListener(
                                                        getSequence(),
                                                        index,
                                                    ),
                                                )
                                            },
                                        )
                                    }
                            }
                        } else {
                            photoContainerFlexBox.visibility = View.GONE
                        }
                    }

                    context?.run {
                        if (config.enableLocationInfo) {
                            diaryDto.location?.let {
                                locationLabel.text = it.address
                            }
                        }

                        if (config.enableCountCharacters) {
                            contentsLength.run {
                                text =
                                    getString(
                                        R.string.diary_contents_length,
                                        diaryDto.contents.length,
                                    )
                            }
                        }
                    }
                }

                val linkedDiaries = mutableListOf<DiaryDomain>()
                if (diaryDto.linkedDiaries.isNotEmpty()) {
                    for (linkedDiarySequence in diaryDto.linkedDiaries) {
                        diaryViewModel.findDiaryById(linkedDiarySequence)?.let {
                            linkedDiaries.add(it)
                        }
                    }
                }
                linkedDiaryViewModel.updateChildDiaries(linkedDiaries.sortedBy { it.currentTimeMillis })
                linkedDiaryViewModel.updateParentDiaries(
                    diaryViewModel.findParentDiariesOf(
                        diaryDto.diaryId,
                    ),
                )
            }
        }

        private fun initBottomContainer() {
            context?.let {
                primaryColor = it.config.primaryColor
            }
        }

        private fun setFontsTypeface() {
            activity?.let {
                FontUtils.setFontsTypeface(it, "", rootView)
            }
        }

        private fun setFontsSize() {
            requireContext().initTextSize(rootView)
        }

        fun encryptData(inputPass: String) {
            lifecycleScope.launch {
                var diary = diaryViewModel.findDiaryById(getSequence())
                diary?.let {
                    diary =
                        diary.copy(
                            isEncrypt = true,
                            title = JasyptUtils.encrypt(it.title, inputPass),
                            contents = JasyptUtils.encrypt(it.contents, inputPass),
                            encryptKeyHash = JasyptUtils.sha256(inputPass),
                        )
                    requireContext().diaryRepository.updateDiaryWithPhotos(diary)
                    initContents()
                }
            }
        }

        suspend fun decryptDataOnce(inputPass: String) {
            diaryViewModel.findDiaryById(getSequence())?.let { diaryDto ->
                binding.run {
                    diaryTitle.text = JasyptUtils.decrypt(diaryDto.title, inputPass)
                    requireActivity().applyMarkDownPolicy(
                        diaryContents,
                        JasyptUtils.decrypt(diaryDto.contents, inputPass),
                    )
                }
            }
        }

        fun decryptData(inputPass: String): Boolean {
            var result = true
            lifecycleScope.launch {
                var diary = diaryViewModel.findDiaryById(getSequence())
                diary?.let {
                    if (it.encryptKeyHash == JasyptUtils.sha256(inputPass)) {
                        diary =
                            diary.copy(
                                isEncrypt = false,
                                title = JasyptUtils.decrypt(it.title, inputPass),
                                contents = JasyptUtils.decrypt(it.contents, inputPass),
                            )
                        requireContext().diaryRepository.updateDiaryWithPhotos(diary)
                        initContents()
                    } else {
                        result = false
                    }
                }
            }

            return result
        }

        fun getContentsPositionY(): Int = (binding.diaryContents.parent.parent.parent as ScrollView).scrollY

        companion object {
            fun newInstance(
                sequence: Long,
                query: String?,
            ): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putLong(DIARY_SEQUENCE, sequence)
                args.putString(SELECTED_SEARCH_QUERY, query)
                fragment.arguments = args
                return fragment
            }
        }

        inner class PhotoClickListener(
            private var diarySequence: Long,
            var index: Int,
        ) : View.OnClickListener {
            override fun onClick(v: View) {
                val photoViewPager = Intent(context, PhotoViewPagerActivity::class.java)
                photoViewPager.putExtra(DIARY_SEQUENCE, diarySequence)
                photoViewPager.putExtra(DIARY_ATTACH_PHOTO_INDEX, index)
                TransitionHelper.startActivityWithTransition(
                    activity,
                    photoViewPager,
                )
            }
        }
    }

    class SectionsPagerAdapter(
        fragmentActivity: androidx.fragment.app.FragmentActivity,
        private val diaryList: List<DiaryDomain>,
        private val query: String?,
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = diaryList.size

        override fun createFragment(position: Int): androidx.fragment.app.Fragment = PlaceholderFragment.newInstance(diaryList[position].diaryId, query)

        override fun getItemId(position: Int): Long = diaryList[position].diaryId

        override fun containsItem(itemId: Long): Boolean = diaryList.any { it.diaryId == itemId }

        fun sequenceToPageIndex(sequence: Long): Int = EasyDiaryUtils.sequenceToPageIndex(diaryList, sequence)
    }
}
