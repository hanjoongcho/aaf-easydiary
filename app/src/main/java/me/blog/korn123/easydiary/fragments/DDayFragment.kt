package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.easydiary.adapters.DDayAdapter
import me.blog.korn123.easydiary.databinding.FragmentDdayBinding
import me.blog.korn123.easydiary.models.DDay

class DDayFragment : Fragment() {

    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinging: FragmentDdayBinding
    private lateinit var mDDayAdapter: DDayAdapter
    private var mDDayItems: MutableList<DDay> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinging = FragmentDdayBinding.inflate(layoutInflater)
        return mBinging.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDDayItems.add(DDay("Hello DDay!!!"))
        mDDayItems.add(DDay("Awesome Day!!!"))
        mDDayAdapter = DDayAdapter(requireActivity(), mDDayItems)

        mBinging.run {
            recyclerDays.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = mDDayAdapter
            }
//            addDDay.setOnClickListener { requireActivity().toast("ADD") }
        }
    }
}