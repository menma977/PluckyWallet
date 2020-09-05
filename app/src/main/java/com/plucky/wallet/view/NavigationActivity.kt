package com.plucky.wallet.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.plucky.wallet.R
import com.plucky.wallet.view.fragment.HomeFragment

class NavigationActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    val fragment = HomeFragment()
    addFragment(fragment)
  }

  private fun addFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().setCustomAnimations(
      R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out
    ).replace(R.id.contentFragment, fragment, fragment.javaClass.simpleName).addToBackStack("back").commit()
  }
}