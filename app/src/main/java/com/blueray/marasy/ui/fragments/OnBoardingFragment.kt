package com.blueray.marasy.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.blueray.marasy.R

class OnBoardingFragment : Fragment() {

    private lateinit var id: String
    private lateinit var title: String
    private lateinit var title2: String

    private var imageResId: Int = 0

    companion object {
        private const val ID = "argId"
        private const val ARG_TITLE = "argTitle"
        private const val ARG2_TITLE = "arg2Title"

        private const val ARG_IMAGE = "argImage"

        fun newInstance(id: String,title: String,title2: String, imageResId: Int): OnBoardingFragment {
            val fragment = OnBoardingFragment()
            val bundle = Bundle().apply {
                putString(ID, id)
                putString(ARG_TITLE, title)
                putString(ARG_TITLE, title)
                putString(ARG2_TITLE, title2)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                putInt(ARG_IMAGE, imageResId)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ID) ?: ""
            title = it.getString(ARG_TITLE) ?: ""
            title2 = it.getString(ARG2_TITLE) ?: ""
            imageResId = it.getInt(ARG_IMAGE)

        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_on_boarding, container, false)

        // Use title and imageResId to populate your views
        val pageTitle: TextView = view.findViewById(R.id.pageTitle)
        val pageData: TextView = view.findViewById(R.id.pageData)
        val pageImage: ImageView = view.findViewById(R.id.pageImage)

        pageTitle.text = title
        pageData.text = title2
        pageImage.setImageResource(imageResId)

        return view
    }
}
