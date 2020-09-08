package com.plucky.wallet.config

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.controller.DogeController
import com.plucky.wallet.model.User
import org.json.JSONObject
import java.math.BigDecimal

class BackgroundGetBalance : IntentService("BackgroundGetBalance") {
  private lateinit var response: JSONObject
  private lateinit var balanceValue: BigDecimal
  private lateinit var user: User
  private lateinit var bitCoinFormat: BitCoinFormat
  private var startBackgroundService: Boolean = false

  override fun onHandleIntent(p0: Intent?) {
    user = User(this)
    bitCoinFormat = BitCoinFormat()
    val body = HashMap<String, String>()
    body["a"] = "GetBalance"
    body["s"] = user.getString("key")
    body["Currency"] = "doge"
    body["Referrals"] = "0"
    body["Stats"] = "0"
    var time = System.currentTimeMillis()
    val trigger = Object()

    synchronized(trigger) {
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 10000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (startBackgroundService) {
            try {
              response = DogeController(body).execute().get()
              if (response.getInt("code") == 200) {
                balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
                user.setString("balanceValue", balanceValue.toPlainString())
                user.setString("balanceText", bitCoinFormat.decimalToDoge(balanceValue).toPlainString())

                privateIntent.action = "plucky.wallet.balance.index"
                LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
              } else {
                Thread.sleep(60000)
              }
            } catch (e: Exception) {
              e.printStackTrace()
              Thread.sleep(60000)
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