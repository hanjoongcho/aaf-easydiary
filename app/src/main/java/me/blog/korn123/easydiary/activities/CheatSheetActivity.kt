package me.blog.korn123.easydiary.activities

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
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
        mOriginCheatSheetList.run {
            add(CheatSheetAdapter.CheatSheet("Cheat Sheet", "This page is a collection of useful link information such as open source projects and development related guides.", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/README.md"))
            add(CheatSheetAdapter.CheatSheet("Regular Expression", "Basic Chapter-01", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/regular-expression/chapter01.md"))
            add(CheatSheetAdapter.CheatSheet("Git-01", "Git and Git Flow Cheat Sheet", "https://raw.githubusercontent.com/arslanbilal/git-cheat-sheet/master/other-sheets/git-cheat-sheet-ko.md"))
            add(CheatSheetAdapter.CheatSheet("Android-01", "Project guidelines", "https://raw.githubusercontent.com/hanjoongcho/android-guidelines/master/project_and_code_guidelines.md"))
            add(CheatSheetAdapter.CheatSheet("Android-02", "Architecture Guidelines / MVP", "https://raw.githubusercontent.com/hanjoongcho/android-guidelines/master/architecture_guidelines/android_architecture.md"))
            add(CheatSheetAdapter.CheatSheet("Kotlin-01", "Explanation of kotlin basic functions", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/kotlin/kotlin.md"))
            add(CheatSheetAdapter.CheatSheet("Kotlin-02", "Explanation of kotlin collection functions", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/kotlin/kotlin.collections.md"))
            add(CheatSheetAdapter.CheatSheet("Kotlin-03", "Coding Conventions", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/kotlin/coding-conventions.md"))
            add(CheatSheetAdapter.CheatSheet("ES6-01", "var, let, const", "https://gist.githubusercontent.com/hanjoongcho/983fe388a669f1da9df13cf64f63c5f3/raw/d1587f1da1d7ead1ba695e50094dbf52daaf6e1e/var-let-const.md"))
            add(CheatSheetAdapter.CheatSheet("ES6-02", "Promise", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/es6/promise.md"))
            add(CheatSheetAdapter.CheatSheet("Java-01", "Describes annotations mainly used in Spring Framework", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/annotations/spring.md"))
            add(CheatSheetAdapter.CheatSheet("Java-02", "Java 8 - Lambda Expression", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/lambda/LambdaExpressionTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-03", "Java 8 - Default Methods", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/defaultmethod/DefaultMethodTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-04", "Java 8 - Functions", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/functions/FunctionFunctionalInterfaceTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-05", "Java 8 - Stream Count", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithCountTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-06", "Java 8 - Stream with Filter", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithFilterTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-07", "Java 8 - Stream with Map", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithMapTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-08", "Java 8 - Stream with Sorted", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithSortedTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-09", "Java 8 - Stream with Match", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamWithMatchTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-10", "Java 8 - Stream Reduce", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/streams/StreamReduceTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-11", "Java 8 - Stream Consumer", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/consumer/ConsumerFunctionalInterfaceTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-12", "Java 8 - Predicate", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/predicate/PredicateFunctionalInterfaceTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-13", "Java 8 - Comparator", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/comparator/ComparatorFunctionalInterfaceTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Java-14", "Java 8 - Suppliers", "https://raw.githubusercontent.com/hanjoongcho/java8-guides-tutorials/master/src/test/java/suppliers/SupplierFunctionalInterfaceTest.java", true))
            add(CheatSheetAdapter.CheatSheet("Data-Structure-01", "국가상수도데이터베이스표준화지침(20210101 개정)", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/design/database/standardization.md"))
            add(CheatSheetAdapter.CheatSheet("Data-Structure-02", "Unicode", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/data-structure/unicode.md"))
            add(CheatSheetAdapter.CheatSheet("Data-Structure-03", "ASCII", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/data-structure/ascii.md"))
            add(CheatSheetAdapter.CheatSheet("Database-01", "Oracle", "https://raw.githubusercontent.com/hanjoongcho/CheatSheet/master/database/oracle-01.md"))
        }

        mBinding.recyclerCheatSheet.apply {
            layoutManager = LinearLayoutManager(this@CheatSheetActivity, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(SettingsScheduleFragment.SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.card_layout_padding)))
            mCheatSheetAdapter =  CheatSheetAdapter(
                    this@CheatSheetActivity,
                    mFilteredCheatSheetList,
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        val item = mFilteredCheatSheetList[position]
                        TransitionHelper.startActivityWithTransition(this@CheatSheetActivity, Intent(this@CheatSheetActivity, MarkDownViewActivity::class.java).apply {
                            putExtra(MarkDownViewActivity.OPEN_URL_INFO, item.url)
                            putExtra(MarkDownViewActivity.OPEN_URL_DESCRIPTION, item.title)
                            putExtra(MarkDownViewActivity.FORCE_APPEND_CODE_BLOCK, item.forceAppendCodeBlock)
                        })
                    }
            )
            adapter = mCheatSheetAdapter
        }
    }
}






