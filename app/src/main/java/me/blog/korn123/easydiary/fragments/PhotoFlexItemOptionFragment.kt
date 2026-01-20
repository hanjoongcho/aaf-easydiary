package me.blog.korn123.easydiary.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.PhotoAdapter
import me.blog.korn123.easydiary.adapters.PhotoAdapter.PhotoViewHolder.Companion.applyOption
import me.blog.korn123.easydiary.databinding.DialogPostcardPhotoOptionBinding

class PhotoFlexItemOptionFragment : DialogFragment() {
    private lateinit var mBinding: DialogPostcardPhotoOptionBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.setTitle(getString(R.string.title_dialog_postcard_photo_option))
        mBinding = DialogPostcardPhotoOptionBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.run {
            spinnerViewMode.run {
                val arrayAdapter =
                    ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.options_spinner_view_mode,
                        R.layout.item_spinner,
                    )
                adapter = arrayAdapter
                setSelection(viewMode)

                onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            p1: View?,
                            position: Int,
                            p3: Long,
                        ) {
                            viewMode = position
                            applyOption(requireContext(), photoUri!!, viewMode, filterMode, imagePreview)
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }

            applyOption(requireContext(), photoUri!!, viewMode, filterMode, imagePreview)

            spinnerFilterMode.run {
                val arrayAdapter =
                    ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.options_spinner_filter_mode,
                        R.layout.item_spinner,
                    )
                adapter = arrayAdapter
                setSelection(filterMode)

                onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            p1: View?,
                            position: Int,
                            p3: Long,
                        ) {
                            filterMode = position
                            applyOption(requireContext(), photoUri!!, viewMode, filterMode, imagePreview)
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
            }

            checkForceSinglePhoto.isChecked = forceSinglePhotoPosition

            buttonOk.setOnClickListener {
                dismiss()
                positiveCallback.invoke(
                    spinnerViewMode.selectedItemPosition,
                    spinnerFilterMode.selectedItemPosition,
                    checkForceSinglePhoto.isChecked,
                )
            }
            buttonCancel.setOnClickListener { dismiss() }
        }
    }

    companion object {
        private const val ITEM_INDEX = "item_index"
        private const val VIEW_MODE = "view_index"
        private const val FILTER_MODE = "filter_index"
        private const val PHOTO_URI = "photo_uri"
        private const val FORCE_SINGLE_PHOTO_POSITION = "force_single_photo_position"

        fun newInstance(postCardPhotoItem: PhotoAdapter.PostCardPhotoItem) =
            PhotoFlexItemOptionFragment().apply {
                arguments =
                    Bundle().apply {
                        putInt(ITEM_INDEX, postCardPhotoItem.position)
                        putInt(VIEW_MODE, postCardPhotoItem.viewMode)
                        putInt(FILTER_MODE, postCardPhotoItem.filterMode)
                        putBoolean(FORCE_SINGLE_PHOTO_POSITION, postCardPhotoItem.forceSinglePhotoPosition)
                        putString(PHOTO_URI, postCardPhotoItem.photoUri)
                    }
            }
    }
}
