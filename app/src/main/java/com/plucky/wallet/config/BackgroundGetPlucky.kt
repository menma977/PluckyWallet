package com.plucky.wallet.config

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.controller.WebOldController
import com.plucky.wallet.model.User
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class BackgroundGetPlucky : IntentService("BackgroundGetPlucky") {
  private lateinit var response: JSONObject
  private lateinit var user: User
  private var startBackgroundService: Boolean = false

  override fun onHandleIntent(p0: Intent?) {
    user = User(this)
    var time = System.currentTimeMillis()
    val trigger = Object()
    val body = HashMap<String, String>()
    body["a"] = "TotalPlucky"
    body["username"] = user.getString("username")
    body["ref"] = convert(user.getString("username") + "b0d0nk111179")
    body["Referrals"] = "0"
    body["Stats"] = "0"

    synchronized(trigger) {
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 10000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (startBackgroundService) {
            response = WebOldController.Post(1, body).execute().get()
            try {
              if (response.getInt("code") == 200) {
                user.setString("plucky", response.getJSONObject("data").getString("totalplucky"))
                user.setString("grade", BitCoinFormat().toGrade(response.getJSONObject("data").getString("grade").toBigDecimal()).toPlainString())
                user.setString("maxDeposit", response.getJSONObject("data").getString("maxdepo"))
                privateIntent.action = "plucky.wallet.user.show.plucky"
                LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
              } else {
                Thread.sleep(5000)
              }
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