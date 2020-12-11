package me.blog.korn123.easydiary.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_postcard_photo_option.*
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.viewholders.PhotoViewHolder

class PhotoFlexItemOptionFragment : DialogFragment() {

    private var itemIndex: Int = -1
    private var viewMode = 0
    private var filterMode = 0
    private var forceSinglePhotoPosition = false
    private var photoUri: String? = null
    lateinit var positiveCallback: (Int, Int, Boolean) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
        } else {
            setStyle(STYLE_NORMAL, android.R.style.Theme_Dialog)
        }
        arguments?.let {
            itemIndex = it.getInt(ITEM_INDEX)
            viewMode = it.getInt(VIEW_MODE)
            filterMode = it.getInt(FILTER_MODE)
            forceSinglePhotoPosition = it.getBoolean(FORCE_SINGLE_PHOTO_POSITION)
            photoUri = it.getString(PHOTO_URI)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setTitle("Crop and filter settings")
        return inflater.inflate(R.layout.dialog_postcard_photo_option, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner_view_mode.run {
            val viewOptions = arrayListOf("Fit Center", "Crop Top", "Crop Center", "Crop Bottom")
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner, viewOptions)
            adapter = arrayAdapter
            setSelection(viewMode)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    viewMode = position
                    PhotoViewHolder.applyOption(requireContext(), photoUri!!, viewMode, filterMode, image_preview)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        PhotoViewHolder.applyOption(requireContext(), photoUri!!, viewMode, filterMode, image_preview)

        spinner_filter_mode.run {
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.item_spinner, arrayListOf("No Filter", "Cartoon", "Gray Scale"))
            adapter = arrayAdapter
            setSelection(filterMode)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    filterMode = position
                    PhotoViewHolder.applyOption(requireContext(), photoUri!!, viewMode, filterMode, image_preview)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        check_force_single_photo.isChecked = forceSinglePhotoPosition

        button_ok.setOnClickListener {
            dismiss()
            positiveCallback.invoke(spinner_view_mode.selectedItemPosition, spinner_filter_mode.selectedItemPosition, check_force_single_photo.isChecked)
        }
        button_cancel.setOnClickListener { dismiss() }
    }

    companion object {
        private const val ITEM_INDEX = "item_index"
        private const val VIEW_MODE = "view_index"
        private const val FILTER_MODE = "filter_index"
        private const val PHOTO_URI = "photo_uri"
        private const val FORCE_SINGLE_PHOTO_POSITION = "force_single_photo_position"

        fun newInstance(postCardPhotoItem: PhotoViewHolder.PostCardPhotoItem) = PhotoFlexItemOptionFragment().apply {
            arguments = Bundle().apply {
                putInt(ITEM_INDEX, postCardPhotoItem.position)
                putInt(VIEW_MODE, postCardPhotoItem.viewMode)
                putInt(FILTER_MODE, postCardPhotoItem.filterMode)
                putBoolean(FORCE_SINGLE_PHOTO_POSITION, postCardPhotoItem.forceSinglePhotoPosition)
                putString(PHOTO_URI, postCardPhotoItem.photoUri)
            }
        }
    }
}