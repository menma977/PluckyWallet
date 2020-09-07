package com.plucky.wallet.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import com.plucky.wallet.view.fragment.HomeFragment
import com.plucky.wallet.view.fragment.InfoFragment
import com.plucky.wallet.view.fragment.ReceivedFragment
import com.plucky.wallet.view.menu.SendBalanceActivity
import com.plucky.wallet.view.menu.UpgradeAccountActivity

class NavigationActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var loading: Loading
  private lateinit var homeButton: ImageButton
  private lateinit var receivedButton: ImageButton
  private lateinit var infoButton: ImageButton
  private lateinit var upgradeButton: ImageButton
  private lateinit var sendDoge: FloatingActionButton
  private lateinit var goTo: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    user = User(this)
    setting = Setting(this)
    bitCoinFormat = BitCoinFormat()
    loading = Loading(this)

    homeButton = findViewById(R.id.buttonHome)
    receivedButton = findViewById(R.id.buttonReceived)
    infoButton = findViewById(R.id.buttonInfo)
    upgradeButton = findViewById(R.id.buttonUpgrade)
    sendDoge = findViewById(R.id.floatingActionButtonSendDoge)
    val fragment = HomeFragment()
    addFragment(fragment)

    setNavigation()
  }

  private fun setNavigation() {
    homeButton.setOnClickListener {
      val fragment = HomeFragment()
      addFragment(fragment)
    }

    receivedButton.setOnClickListener {
      val fragment = ReceivedFragment()
      addFragment(fragment)
    }

    infoButton.setOnClickListener {
      val fragment = InfoFragment()
      addFragment(fragment)
    }

    upgradeButton.setOnClickListener {
      goTo = Intent(this, UpgradeAccountActivity::class.java)
      startActivity(goTo)
    }

    sendDoge.setOnClickListener {
      goTo = Intent(this, SendBalanceActivity::class.java)
      startActivity(goTo)
    }
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount == 1) {
      finishAffinity()
    } else {
      super.onBackPressed()
    }
  }

  private fun addFragment(fragment: Fragment) {
    val backStateName = fragment.javaClass.name
    val fragmentManager = supportFragmentManager
    val fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0)

    if (!fragmentPopped) {
      val fragmentTransaction = fragmentManager.beginTransaction()
      fragmentTransaction.setCustomAnimations(R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out)
      fragmentTransaction.replace(R.id.contentFragment, fragment)
      fragmentTransaction.addToBackStack(backStateName)
      fragmentTransaction.commit()
    }
  }
}