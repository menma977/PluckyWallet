package com.plucky.wallet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import com.plucky.wallet.view.LoginActivity
import com.plucky.wallet.view.NavigationActivity
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var response: JSONObject

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    user = User(this)
    setting = Setting(this)

    getData()
  }

  private fun getData() {
    Timer().schedule(1000) {
      response = WebController.Get("login.show", user.getString("token")).execute().get()
      goTo = Intent(applicationContext, LoginActivity::class.java)
      if (response.getInt("code") == 200) {
        if (response.getJSONObject("data").getString("version") == BuildConfig.VERSION_CODE.toString()) {
          runOnUiThread {
            if (user.getString("token").isNotEmpty()) {
              isLogin()
            } else {
              isNotLogin()
            }
          }
        } else {
          runOnUiThread {
            isUpdate()
          }
        }
      } else {
        runOnUiThread {
          isError()
        }
      }
    }
  }

  private fun isLogin() {
    goTo = Intent(applicationContext, NavigationActivity::class.java)
    startActivity(goTo)
    finish()
  }

  private fun isNotLogin() {
    goTo.putExtra("lock", false)
    goTo.putExtra("isUpdate", false)
    goTo.putExtra("version", "Version ${BuildConfig.VERSION_CODE}")
    startActivity(goTo)
    finish()
  }

  private fun isUpdate() {
    user.clear()
    setting.clear()
    goTo.putExtra("lock", true)
    goTo.putExtra("isUpdate", true)
    goTo.putExtra("version", "New Version ${response.getJSONObject("data").getString("version")}")
    startActivity(goTo)
    finish()
  }

  private fun isError() {
    user.clear()
    setting.clear()
    goTo.putExtra("lock", true)
    goTo.putExtra("isUpdate", false)
    goTo.putExtra("version", "problematic connection. please close the app and reopen it to continue")
    startActivity(goTo)
    finish()
  }
}