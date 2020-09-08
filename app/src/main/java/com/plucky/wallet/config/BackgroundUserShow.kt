package com.plucky.wallet.config

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.model.User
import org.json.JSONObject

class BackgroundUserShow : IntentService("BackgroundUserShow") {
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
        if (delta >= 10000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (startBackgroundService) {
            response = WebController.Get("user", user.getString("token")).execute().get()
            try {
              if (response.getInt("code") == 200) {
                user.setString("email", response.getJSONObject("data").getJSONObject("user").getString("email"))
                user.setInteger("lot", response.getJSONObject("data").getJSONObject("user").getInt("lot"))
                user.setString("lotTarget", response.getJSONObject("data").getString("lotTarget"))
                user.setString("lotProgress", response.getJSONObject("data").getString("lotProgress"))
                user.setBoolean("onQueue", response.getJSONObject("data").getBoolean("onQueue"))
                user.setString("dollar", response.getJSONObject("data").getString("dollar"))
                user.setBoolean("suspend", response.getJSONObject("data").getJSONObject("user").getInt("suspend") == 1)

                privateIntent.putExtra("isLogout", false)
              } else {
                if (response.getString("data").contains("Unauthenticated.")) {
                  privateIntent.putExtra("isLogout", true)
                } else {
                  privateIntent.putExtra("isLogout", false)
                  Thread.sleep(5000)
                }
              }
              privateIntent.action = "plucky.wallet.user.show"
              LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
            } catch (e: Exception) {
              Thread.sleep(5000)
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