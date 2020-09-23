package com.plucky.wallet.view.menu.bot

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.controller.WebOldController
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import com.plucky.wallet.view.menu.bot.bot1.Bot1Activity
import com.plucky.wallet.view.menu.bot.bot2.Bot2Activity
import com.plucky.wallet.view.menu.bot.bot3.Bot3Activity
import org.json.JSONObject
import java.math.BigDecimal
import java.math.MathContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.concurrent.schedule

class BotMenuActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var loading: Loading
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var response: JSONObject
  private lateinit var botMode2: Button
  private lateinit var botMode3: Button
  private lateinit var uniqueCode: String
  private val body = HashMap<String, String>()
  private lateinit var balanceValue: BigDecimal
  private lateinit var buttonBot1: Button
  private lateinit var currentBalance: String
  private lateinit var beatingBalance: String
  private lateinit var editTextDoge: EditText
  private lateinit var editTextDoge2: EditText
  private lateinit var editTextDoge3: EditText

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_menu)

    loading = Loading(this)
    user = User(this)
    setting = Setting(this)

    buttonBot1 = findViewById(R.id.buttonBot1)
    botMode2 = findViewById(R.id.buttonBot2)
    botMode3 = findViewById(R.id.buttonBot3)
    editTextDoge = findViewById(R.id.editTextDoge)
    editTextDoge2 = findViewById(R.id.editTextDoge2)
    editTextDoge3 = findViewById(R.id.editTextDoge3)

    balanceValue = user.getString("balanceValue").toBigDecimal()

    buttonBot1.setOnClickListener {
      val balanceSet = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal())
      when {
        editTextDoge.text.toString().isEmpty() -> {
          Toast.makeText(this, "Doge cant be empty", Toast.LENGTH_SHORT).show()
        }
        editTextDoge.text.toString().toBigDecimal() > balanceSet -> {
          Toast.makeText(this, "Max doge is $balanceSet", Toast.LENGTH_SHORT).show()
        }
        else -> {
          currentBalance = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal()).toPlainString()
          beatingBalance = BitCoinFormat().decimalToDoge(BitCoinFormat().dogeToDecimal(editTextDoge.text.toString().toBigDecimal())).toPlainString()
          startBot1()
        }
      }
    }

    botMode2.setOnClickListener {
      val balanceSet = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal())
      when {
        editTextDoge2.text.toString().isEmpty() -> {
          Toast.makeText(this, "Doge cant be empty", Toast.LENGTH_SHORT).show()
        }
        editTextDoge2.text.toString().toBigDecimal() > balanceSet -> {
          Toast.makeText(this, "Max doge is $balanceSet", Toast.LENGTH_SHORT).show()
        }
        else -> {
          currentBalance = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal()).toPlainString()
          beatingBalance = BitCoinFormat().decimalToDoge(BitCoinFormat().dogeToDecimal(editTextDoge2.text.toString().toBigDecimal())).toPlainString()
          balanceValue = BitCoinFormat().dogeToDecimal(editTextDoge2.text.toString().toBigDecimal())
          startBot2()
        }
      }
    }

    botMode3.setOnClickListener {
      val balanceSet = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal())
      when {
        editTextDoge3.text.toString().isEmpty() -> {
          Toast.makeText(this, "Doge cant be empty", Toast.LENGTH_SHORT).show()
        }
        editTextDoge3.text.toString().toBigDecimal() > balanceSet -> {
          Toast.makeText(this, "Max doge is $balanceSet", Toast.LENGTH_SHORT).show()
        }
        else -> {
          currentBalance = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal()).toPlainString()
          beatingBalance = BitCoinFormat().decimalToDoge(BitCoinFormat().dogeToDecimal(editTextDoge3.text.toString().toBigDecimal())).toPlainString()
          balanceValue = BitCoinFormat().dogeToDecimal(editTextDoge3.text.toString().toBigDecimal())

          startBot3()
        }
      }
    }
  }

  private fun bodyBot(target: String): HashMap<String, String> {
    uniqueCode = UUID.randomUUID().toString()
    body["a"] = target
    body["usertrade"] = user.getString("usernameDoge")
    body["passwordtrade"] = user.getString("passwordDoge")
    body["notrx"] = uniqueCode
    body["balanceawal"] = currentBalance
    body["balancebet"] = beatingBalance
    body["ref"] = convert(user.getString("usernameDoge") + user.getString("passwordDoge") + uniqueCode + "balanceawalb0d0nk111179")
    return body
  }

  private fun startBot1() {
    loading.openDialog()
    try {
      Timer().schedule(1000) {
        response = WebOldController.Post(2, bodyBot("StartTrading1")).execute().get()
        if (response.getInt("code") == 200) {
          goTo = Intent(applicationContext, Bot1Activity::class.java)
          goTo.putExtra("profit", response.getJSONObject("data").getString("profit"))
          runOnUiThread {
            startActivity(goTo)
            finish()
            loading.closeDialog()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
        }
      }
    } catch (e: Exception) {
      Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
      loading.closeDialog()
    }
  }

  private fun startBot2() {
    loading.openDialog()
    Timer().schedule(1000) {
      response = WebOldController.Post(2, bodyBot("StartTrading")).execute().get()
      try {
        if (response.getInt("code") == 200) {
          user.setBoolean("ifPlay", response.getJSONObject("data").getBoolean("main"))
          if (response.getJSONObject("data")["main"] == true) {
            isPlaying(response)
          } else {
            goTo = Intent(applicationContext, Bot2Activity::class.java)
            goTo.putExtra("mode", 0)
            goTo.putExtra("uniqueCode", uniqueCode)
            goTo.putExtra("balance", balanceValue)
            goTo.putExtra("target", response.getJSONObject("data").getDouble("persen").div(100).toBigDecimal())
            runOnUiThread {
              startActivity(goTo)
              finish()
              loading.closeDialog()
            }
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_LONG).show()
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

  private fun startBot3() {
    loading.openDialog()
    Timer().schedule(1000) {
      response = WebOldController.Post(2, bodyBot("StartTrading2")).execute().get()
      try {
        if (response.getInt("code") == 200) {
          user.setBoolean("ifPlay", response.getJSONObject("data").getBoolean("main"))
          if (response.getJSONObject("data")["main"] == true) {
            isPlaying(response)
          } else {
            goTo = Intent(applicationContext, Bot3Activity::class.java)
            goTo.putExtra("mode", 0)
            goTo.putExtra("uniqueCode", uniqueCode)
            goTo.putExtra("balance", balanceValue)
            goTo.putExtra("target", response.getJSONObject("data").getDouble("persen").div(100).toBigDecimal())
            runOnUiThread {
              startActivity(goTo)
              finish()
              loading.closeDialog()
            }
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_LONG).show()
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

  private fun isPlaying(data: JSONObject) {
    val oldBalanceData = BigDecimal(data.getJSONObject("data")["saldoawalmain"].toString(), MathContext.DECIMAL32)
    uniqueCode = response.getJSONObject("data")["notrxlama"].toString()
    val profit = balanceValue - BitCoinFormat().decimalToDoge(oldBalanceData)
    goTo = Intent(applicationContext, ResultActivity::class.java)
    if (profit < BigDecimal(0)) {
      goTo.putExtra("status", "CUT LOSS")
      goTo.putExtra("uniqueCode", uniqueCode)
      goTo.putExtra("startBalance", oldBalanceData)
    } else {
      goTo.putExtra("status", "WIN")
      goTo.putExtra("uniqueCode", uniqueCode)
      goTo.putExtra("startBalance", oldBalanceData)
    }
    runOnUiThread {
      startActivity(goTo)
      finish()
      loading.closeDialog()
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