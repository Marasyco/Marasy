package com.blueray.marasy.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blueray.marasy.ui.activities.MyOrdersActivity
import com.blueray.marasy.ui.fragments.CanceledOrdersFragment
import com.blueray.marasy.ui.fragments.CurrentOrdersFragment
import com.blueray.marasy.ui.fragments.PastOrdersFragment

class MyOrdersPagerAdapter(activity: MyOrdersActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3 // Number of tabs/fragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CurrentOrdersFragment()    // For "Current Orders"
            1 -> PastOrdersFragment()  // For "Completed Orders"
            2 -> CanceledOrdersFragment()  // For "Cancelled Orders"
            else -> CurrentOrdersFragment()
        }
    }
}