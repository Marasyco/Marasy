package com.blueray.marasy.adapters

import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blueray.marasy.R
import com.blueray.marasy.ui.fragments.OnBoardingFragment

class OnBoardingAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    private val data = listOf(
        OnBoardingPage(
            1,
            "التوصيل مجاني دائماً.",
            "تسوق آلاف المنتجات لمنزلك مع سهولة الطلب والتوصيل الموثوق.",
            R.drawable.onboarding2
        ),

        OnBoardingPage(
            2,
            "المنزل بأكمله في مكان واحد",
            "كل ما تحتاجه لمنزلك في مكان واحد. تصفح الأقسام، أضف إلى سلة التسوق، وأكمل عملية الشراء بسرعة.",
            R.drawable.onboarding2
        ),
    )

    override fun getItemCount(): Int {
        return data.size
    }

    override fun createFragment(position: Int): Fragment {
        val onBoardingPage = data[position]

        return OnBoardingFragment.newInstance(
            onBoardingPage.id.toString(),
            onBoardingPage.title,
            onBoardingPage.title2,
            onBoardingPage.imageResId
        )

    }

    data class OnBoardingPage(
        val id : Int,
        val title: String,
        val title2: String,
        @DrawableRes val imageResId: Int
    )

}