package me.blog.korn123.easydiary.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.makeToast

class PhotoFlexItemOptionFragment : DialogFragment() {

    private var itemIndex: Int = -1
    lateinit var positiveCallback: (Int) -> Unit

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo_item_option, container, false)
        val spinner: Spinner = view.findViewById(R.id.spinner_view_mode)
        val viewOptions = arrayListOf("TOP", "BOTTOM")
        val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner, viewOptions)
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                requireActivity().makeToast(parent?.getItemAtPosition(position) as String)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val okButton: Button = view.findViewById(R.id.button_ok)
        okButton.setOnClickListener(View.OnClickListener {
            dismiss()
            positiveCallback.invoke(itemIndex)
        })

        return view
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