package com.plucky.wallet.config

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.model.User
import com.plucky.wallet.controller.DogeController
import org.json.JSONObject
import java.lang.Thread.sleep
import java.math.BigDecimal
import java.math.MathContext

/**
 * class BackgroundServiceBalance
 * @property response JSONObject
 * @property balanceValue BigDecimal
 * @property user User
 * @property bitCoinFormat BitCoinFormat
 * @property startBackgroundService Boolean
 * @property limitDepositDefault (java.math.BigDecimal..java.math.BigDecimal?)
 */
class BackgroundServiceBalance : IntentService("BackgroundServiceBalance") {
  private lateinit var response: JSONObject
  private lateinit var balanceValue: BigDecimal
  private lateinit var user: User
  private lateinit var bitCoinFormat: BitCoinFormat
  private var startBackgroundService: Boolean = false
  private var limitDepositDefault = BigDecimal(0.000000000, MathContext.DECIMAL32).setScale(8, BigDecimal.ROUND_HALF_DOWN)

  override fun onHandleIntent(intent: Intent?) {
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
        if (delta >= 5000) {
          time = System.currentTimeMillis()
          val privateIntent = Intent()
          if (startBackgroundService) {
            try {
              response = DogeController(body).execute().get()
              if (response.getInt("code") == 200) {
                balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
                privateIntent.putExtra("balanceValue", balanceValue)
                user.setString("balanceValue", balanceValue.toPlainString())
                user.setString("balanceText", "${bitCoinFormat.decimalToDoge(balanceValue).toPlainString()} DOGE")

                privateIntent.action = "net.dogearn.doge"
                LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
              } else {
                sleep(60000)
              }
            } catch (E: Exception) {
              sleep(60000)
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