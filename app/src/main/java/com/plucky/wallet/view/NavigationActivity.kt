package com.plucky.wallet.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.plucky.wallet.MainActivity
import com.plucky.wallet.R
import com.plucky.wallet.config.*
import com.plucky.wallet.controller.DogeController
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.controller.WebOldController
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import com.plucky.wallet.view.fragment.HomeFragment
import com.plucky.wallet.view.fragment.InfoFragment
import com.plucky.wallet.view.fragment.ReceivedFragment
import com.plucky.wallet.view.menu.SendBalanceActivity
import com.plucky.wallet.view.menu.UpgradeAccountActivity
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.concurrent.schedule

class NavigationActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var homeButton: LinearLayout
  private lateinit var receivedButton: LinearLayout
  private lateinit var infoButton: LinearLayout
  private lateinit var upgradeButton: LinearLayout
  private lateinit var sendDoge: FloatingActionButton
  private lateinit var goTo: Intent
  private lateinit var intentServiceUserShow: Intent
  private lateinit var intentServiceGetBalance: Intent
  private lateinit var intentServiceGetPlucky: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    user = User(this)
    setting = Setting(this)
    bitCoinFormat = BitCoinFormat()
    loading = Loading(this)

    homeButton = findViewById(R.id.linearLayoutHome)
    receivedButton = findViewById(R.id.linearLayoutReceived)
    infoButton = findViewById(R.id.linearLayoutInfo)
    upgradeButton = findViewById(R.id.linearLayoutUpgrade)
    sendDoge = findViewById(R.id.floatingActionButtonSendDoge)

    setNavigation()
    onQueue()

    getUser()
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

  private fun onQueue() {
    if (user.getBoolean("onQueue") || user.getBoolean("pending")) {
      sendDoge.isEnabled = false
      upgradeButton.isEnabled = false
    } else {
      sendDoge.isEnabled = true
      upgradeButton.isEnabled = true
    }
  }

  override fun onStart() {
    super.onStart()
    Timer().schedule(1000) {
      intentServiceUserShow = Intent(applicationContext, BackgroundUserShow::class.java)
      startService(intentServiceUserShow)

      intentServiceGetBalance = Intent(applicationContext, BackgroundGetBalance::class.java)
      startService(intentServiceGetBalance)

      intentServiceGetPlucky = Intent(applicationContext, BackgroundGetPlucky::class.java)
      startService(intentServiceGetPlucky)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverUserShow, IntentFilter("plucky.wallet.user.show"))
    }
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverUserShow)

    stopService(intentServiceUserShow)
    stopService(intentServiceGetBalance)
    super.onStop()
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount == 1) {
      stopService(intentServiceUserShow)
      stopService(intentServiceGetBalance)
      finishAffinity()
    } else {
      super.onBackPressed()
    }
  }

  private var broadcastReceiverUserShow: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      onQueue()
      if (intent.getBooleanExtra("isLogout", false) || user.getBoolean("suspend")) {
        onLogout()
      }
    }
  }

  private fun getUser() {
    loading.openDialog()
    Timer().schedule(100) {
      response = WebController.Get("user", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        if (response.getJSONObject("data").getJSONObject("user").getInt("suspend") == 1) {
          onLogout()
        } else {
          user.setString("email", response.getJSONObject("data").getJSONObject("user").getString("email"))
          user.setInteger("lot", response.getJSONObject("data").getJSONObject("user").getInt("lot"))
          user.setString("lotTarget", response.getJSONObject("data").getString("lotTarget"))
          user.setString("lotProgress", response.getJSONObject("data").getString("lotProgress"))
          user.setBoolean("onQueue", response.getJSONObject("data").getBoolean("onQueue"))
          user.setString("dollar", response.getJSONObject("data").getString("dollar"))
          user.setBoolean("suspend", response.getJSONObject("data").getJSONObject("user").getInt("suspend") == 1)
          user.setBoolean("isWin", response.getJSONObject("data").getBoolean("isWin"))

          runOnUiThread {
            getBalance()
          }
        }
      } else {
        if (response.getString("data").contains("Unauthenticated.")) {
          loading.closeDialog()
          onLogout()
        } else {
          runOnUiThread {
            loading.closeDialog()
            Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          }
        }
      }
    }
  }

  private fun getBalance() {
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "GetBalance"
      body["s"] = user.getString("key")
      body["Currency"] = "doge"
      body["Referrals"] = "0"
      body["Stats"] = "0"
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        val balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
        user.setString("balanceValue", balanceValue.toPlainString())
        user.setString("balanceText", bitCoinFormat.decimalToDoge(balanceValue).toPlainString())

        runOnUiThread {
          getPlucky()
        }
      } else {
        runOnUiThread {
          loading.closeDialog()
          Toast.makeText(applicationContext, "Balance Error", Toast.LENGTH_LONG).show()
          user.setString("balanceValue", "0")
          user.setString("balanceText", "0")
        }
      }
    }
  }

  private fun getPlucky() {
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["a"] = "TotalPlucky"
      body["username"] = user.getString("username")
      body["ref"] = convert(user.getString("username") + "b0d0nk111179")
      body["Referrals"] = "0"
      body["Stats"] = "0"
      response = WebOldController.Post(1, body).execute().get()
      if (response.getInt("code") == 200) {
        user.setString("plucky", response.getJSONObject("data").getString("totalplucky"))
        user.setString("grade", BitCoinFormat().toGrade(response.getJSONObject("data").getString("grade").toBigDecimal()).toPlainString())
        user.setString("maxDeposit", response.getJSONObject("data").getString("maxdepo"))
        user.setBoolean("pending", response.getJSONObject("data").getBoolean("pending"))
        user.setString("maxBot1", response.getJSONObject("data").getString("maxbot1"))

        println(response.getJSONObject("data").getString("maxbot1"))

        runOnUiThread {
          loading.closeDialog()
          val fragment = HomeFragment()
          addFragment(fragment)
        }
      }
    }
  }

  fun onLogout() {
    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiverUserShow)
    stopService(intentServiceUserShow)
    stopService(intentServiceGetBalance)
    Timer().schedule(100) {
      response = WebController.Get("logout", user.getString("token")).execute().get()
      runOnUiThread {
        if (response.getInt("code") == 200) {
          loading.closeDialog()
          user.clear()
          setting.clear()
          goTo = Intent(applicationContext, MainActivity::class.java)
          loading.closeDialog()
          startActivity(goTo)
          finishAffinity()
        } else {
          if (response.getString("data").contains("Unauthenticated.")) {
            loading.closeDialog()
            user.clear()
            setting.clear()
            goTo = Intent(applicationContext, MainActivity::class.java)
            loading.closeDialog()
            startActivity(goTo)
            finishAffinity()
          }
        }
      }
    }
  }

  private fun addFragment(fragment: Fragment) {
    val backStateName = fragment.javaClass.simpleName
    val fragmentManager = supportFragmentManager
    val fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0)

    if (!fragmentPopped && fragmentManager.findFragmentByTag(backStateName) == null) {
      val fragmentTransaction = fragmentManager.beginTransaction()
      fragmentTransaction.setCustomAnimations(R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out)
      fragmentTransaction.replace(R.id.contentFragment, fragment, backStateName)
      fragmentTransaction.addToBackStack(backStateName)
      fragmentTransaction.commit()
    }
  }

  fun convert(input: String): String {
    val md5 = "MD5"
    try {
      val digest = MessageDigest.getInstance(md5)
      digest.update(input.toByteArray())
      val messageDigest = digest.digest()
      val hexString = StringBuilder()
      for (aMessageDigest in messageDigest) {
        var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
        while (h.length < 2) h = "0$h"
        hexString.append(h)
      }
      return hexString.toString()
    } catch (e: NoSuchAlgorithmException) {
      //e.printStackTrace()
    }
    return ""
  }
}