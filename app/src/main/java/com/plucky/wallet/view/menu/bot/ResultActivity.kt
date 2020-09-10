package com.plucky.wallet.view.menu.bot

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plucky.wallet.MainActivity
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.controller.WebOldController
import com.plucky.wallet.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.concurrent.schedule

class ResultActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var status: TextView
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var uniqueCode: String
  private lateinit var startBalance: BigDecimal
  private lateinit var response: JSONObject
  private var mode = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_result)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    status = findViewById(R.id.textViewStatus)

    loading.openDialog()

    uniqueCode = intent.getSerializableExtra("uniqueCode") as String
    startBalance = intent.getSerializableExtra("startBalance") as BigDecimal
    mode = intent.getIntExtra("mode", 0)

    if (mode == 0) {
      sendDataToWeb("EndTrading1")
    } else {
      sendDataToWeb("EndTrading4")
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    val goTo = Intent(this, MainActivity::class.java)
    startActivity(goTo)
    finish()
  }

  private fun sendDataToWeb(targetApi: String) {
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = targetApi
      body["usertrade"] = user.getString("usernameDoge")
      body["passwordtrade"] = user.getString("passwordDoge")
      body["notrx"] = intent.getSerializableExtra("uniqueCode") as String
      body["status"] = intent.getSerializableExtra("status") as String
      body["startbalance"] = bitCoinFormat.decimalToDoge(startBalance).toPlainString()
      body["ref"] = convert(user.getString("usernameDoge") + user.getString("passwordDoge") + body["notrx"] + body["status"] + "balanceakhirb0d0nk111179")
      response = WebOldController.Post(2, body).execute().get()
      try {
        if (response["code"] == 200) {
          runOnUiThread {
            status.text = response.getJSONObject("data")["profit"].toString()
            loading.closeDialog()
          }
        } else {
          runOnUiThread {
            status.text = response["data"].toString()
            loading.closeDialog()
          }
        }
      } catch (e: Exception) {
        runOnUiThread {
          Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
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