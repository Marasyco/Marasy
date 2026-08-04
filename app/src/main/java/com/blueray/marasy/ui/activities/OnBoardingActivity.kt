package com.blueray.marasy.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.blueray.marasy.R
import com.blueray.marasy.adapters.OnBoardingAdapter
import com.blueray.marasy.databinding.ActivityOnBoardingBinding

class OnBoardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnBoardingBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        // Check if the user has already seen the OnBoarding page
        if (sharedPreferences.getBoolean("hasSeenOnBoarding", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set viewpager with indicator
        binding.viewPager.adapter = OnBoardingAdapter(supportFragmentManager, lifecycle)
        binding.indicator.attachToPager(binding.viewPager)
        binding.indicator.setDotCount(3)
        binding.viewPager.currentItem = 0

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 1) {
                    binding.next.text = getString(R.string.shop_now)
                    binding.next.icon = null
                    binding.skip.visibility = android.view.View.INVISIBLE
                } else {
                    binding.next.text = getString(R.string.next)
                    binding.next.setIconResource(R.drawable.baseline_arrow_forward_24)
                    binding.skip.visibility = android.view.View.VISIBLE
                }
            }
        })

        binding.skip.setOnClickListener {
            completeOnBoarding()
        }

        // Add fake swipe
        binding.next.setOnClickListener {
            if (binding.viewPager.currentItem < 1) {
                binding.viewPager.currentItem += 1
            } else {
                completeOnBoarding()
            }
        }
    }

    private fun completeOnBoarding() {
        // Start LogIn Activity and save that the user has seen the OnBoarding page
        sharedPreferences.edit().putBoolean("hasSeenOnBoarding", true).apply()
        startActivity(Intent(this@OnBoardingActivity, LoginActivity::class.java))
        finish()
    }

}