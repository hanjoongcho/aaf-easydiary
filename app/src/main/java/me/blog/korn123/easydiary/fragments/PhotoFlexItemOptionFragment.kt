package me.blog.korn123.easydiary.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_photo_item_option.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeToast

class PhotoFlexItemOptionFragment : DialogFragment() {

    private var itemIndex: Int = -1
    lateinit var positiveCallback: (Int, Int) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
        } else {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Dialog)
        }
        arguments?.let {
            itemIndex = it.getInt(ITEM_INDEX)
        }

       dialog?.setTitle("Selected item is $itemIndex")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_photo_item_option, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner_view_mode.run {
            val viewOptions = arrayListOf("Fit Center", "Crop Top", "Crop Center", "Crop Bottom")
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner, viewOptions)
            adapter = arrayAdapter
//            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
//                    requireActivity().makeToast(parent?.getItemAtPosition(position) as String)
//                }
//                override fun onNothingSelected(parent: AdapterView<*>) {}
//            }
        }

        spinner_filter_mode.run {
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner, arrayListOf("No Filter", "Cartoon", "Gray Scale"))
            adapter = arrayAdapter
        }

        button_ok.setOnClickListener {
            dismiss()
            positiveCallback.invoke(spinner_view_mode.selectedItemPosition, spinner_filter_mode.selectedItemPosition)
        }
        button_cancel.setOnClickListener { dismiss() }
    }

    companion object {
        private const val ITEM_INDEX = "item_index"

        fun newInstance(itemIndex: Int) = PhotoFlexItemOptionFragment().apply {
            arguments = Bundle().apply {
                putInt(ITEM_INDEX, itemIndex)
            }
        }
    }
}