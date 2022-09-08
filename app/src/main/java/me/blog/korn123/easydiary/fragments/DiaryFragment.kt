package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Sort
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.adapters.DiaryMainItemAdapter
import me.blog.korn123.easydiary.databinding.FragmentDiaryBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.*
import me.blog.korn123.easydiary.models.Diary

class DiaryFragment : Fragment() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentDiaryBinding
    private var mDiaryMainItemAdapter: DiaryMainItemAdapter? = null
    private var mDiaryList: ArrayList<Diary> = arrayListOf()


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDiaryBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDiaryMainItemAdapter = DiaryMainItemAdapter(requireActivity(), mDiaryList, {
            val detailIntent = Intent(requireContext(), DiaryReadingActivity::class.java)
            detailIntent.putExtra(DIARY_SEQUENCE, it.sequence)
            detailIntent.putExtra(SELECTED_SEARCH_QUERY, mDiaryMainItemAdapter?.currentQuery)
            detailIntent.putExtra(SELECTED_SYMBOL_SEQUENCE, 0)
            TransitionHelper.startActivityWithTransition(requireActivity(), detailIntent)
        }, {})

        mBinding.recyclerDiary.run {
            adapter = mDiaryMainItemAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                GridItemDecorationDiaryMain(
                    resources.getDimensionPixelSize(R.dimen.component_margin_small),
                    requireActivity()
                )
            )
        }

        refreshList()
    }

    private fun refreshList() {
        mDiaryList.clear()
        mDiaryList.addAll(
            EasyDiaryDbHelper.findDiary(
                null, config.diarySearchQueryCaseSensitive, 0, 0, 0)
        )
        mDiaryMainItemAdapter?.notifyDataSetChanged()
    }
}