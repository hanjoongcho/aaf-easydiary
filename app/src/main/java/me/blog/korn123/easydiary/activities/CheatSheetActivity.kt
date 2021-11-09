package me.blog.korn123.easydiary.activities

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.CheatSheetAdapter
import me.blog.korn123.easydiary.databinding.ActivityCheatSheetBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.fragments.SettingsScheduleFragment
import me.blog.korn123.easydiary.helper.*
import java.util.*

open class CheatSheetActivity : EasyDiaryActivity() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: ActivityCheatSheetBinding
    private lateinit var mCheatSheetAdapter: CheatSheetAdapter
    private var mOriginCheatSheetList = arrayListOf<CheatSheetAdapter.CheatSheet>()
    private var mFilteredCheatSheetList = arrayListOf<CheatSheetAdapter.CheatSheet>()
    // t1 commit 01

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_cheat_sheet)
        mBinding.lifecycleOwner = this

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            title = "Cheat Sheet"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        setupCheatSheet()
        bindEvent()
        refreshList(null)
    }

    private fun bindEvent() {
        mBinding.query.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                refreshList(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    private fun refreshList(query: String?) {
        mFilteredCheatSheetList.clear()
        mFilteredCheatSheetList.addAll(if (query.isNullOrEmpty()) mOriginCheatSheetList else mOriginCheatSheetList.filter { cheatSheet -> cheatSheet.title.contains(query, true) || cheatSheet.description.contains(query, true) })
        mCheatSheetAdapter.notifyDataSetChanged()
    }


    /***************************************************************************************************
     *   test functions
     *
     ***************************************************************************************************/
    private fun setupCheatSheet() {
        mOriginCheatSheetList.run {}
        mBinding.recyclerCheatSheet.apply {

            layoutManager = LinearLayoutManager(this@CheatSheetActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            mCheatSheetAdapter =  CheatSheetAdapter(
                    this@CheatSheetActivity,
                    mFilteredCheatSheetList
            ) { _, _, position, _ ->
                val item = mFilteredCheatSheetList[position]
                TransitionHelper.startActivityWithTransition(this@CheatSheetActivity, Intent(this@CheatSheetActivity, MarkDownViewActivity::class.java).apply {
                    putExtra(MarkDownViewActivity.OPEN_URL_INFO, item.url)
                    putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, item.title)
                    putExtra(MarkDownViewActivity.FORCE_APPEND_CODE_BLOCK, item.forceAppendCodeBlock)
                })
            }
            adapter = mCheatSheetAdapter
        }
    }
}






