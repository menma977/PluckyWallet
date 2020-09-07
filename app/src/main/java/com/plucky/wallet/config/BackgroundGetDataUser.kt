package com.plucky.wallet.config

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.model.User
import com.plucky.wallet.controller.WebController
import org.json.JSONObject
import java.lang.Thread.sleep

class BackgroundGetDataUser : IntentService("BackgroundGetDataUser") {
  private lateinit var response: JSONObject
  private lateinit var user: User
  private var startBackgroundService: Boolean = false

  override fun onHandleIntent(p0: Intent?) {
    user = User(this)
    var time = System.currentTimeMillis()
    val trigger = Object()

    synchronized(trigger) {
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 5000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (startBackgroundService) {
            response = WebController.Get("user.show", user.getString("token")).execute().get()
            try {
              if (response.getInt("code") == 200) {
                if (response.getJSONObject("data").getString("grade") == "0" || response.getJSONObject("data").getString("grade") == "null") {
                  user.setString("gradeTarget", "0")
                  user.setString("gradeLevel", "0")
                } else {
                  user.setString("gradeTarget", response.getJSONObject("data").getString("gradeTarget"))
                  user.setString("gradeLevel", response.getJSONObject("data").getJSONObject("grade").getString("id"))
                }

                user.setInteger("pin", response.getJSONObject("data").getInt("pin"))
                user.setString("wallet", response.getJSONObject("data").getJSONObject("user").getString("wallet"))
                user.setString("progressGrade", response.getJSONObject("data").getString("progressGrade"))
                user.setString("phone", response.getJSONObject("data").getJSONObject("user").getString("phone"))
                user.setString("email", response.getJSONObject("data").getJSONObject("user").getString("email"))
                user.setInteger("onQueue", response.getJSONObject("data").getInt("onQueue"))
                user.setString("phoneSponsor", response.getJSONObject("data").getString("phoneSponsor"))
                user.setString("dollar", response.getJSONObject("data").getString("dollar"))
                if (response.getJSONObject("data").getInt("isUserWin") == 0) {
                  user.setBoolean("isUserWin", false)
                } else {
                  user.setBoolean("isUserWin", true)
                }
                user.setInteger("lot", response.getJSONObject("data").getInt("lot"))

                privateIntent.action = "net.dogearn.web"
                LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
              } else {
                if (response.getString("data").contains("Unauthenticated.")) {
                  privateIntent.putExtra("isLogout", true)
                } else {
                  privateIntent.putExtra("isLogout", false)
                  sleep(5000)
                }
              }
              privateIntent.action = "net.dogearn.web.logout"
              LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
            } catch (e: Exception) {
              sleep(5000)
            }
          } else {
            break
          }
        }
      }
    }
  }

  override fun onCreate() {
    startBackgroundService = true
    super.onCreate()
  }

  override fun onDestroy() {
    startBackgroundService = false
    super.onDestroy()
  }
}