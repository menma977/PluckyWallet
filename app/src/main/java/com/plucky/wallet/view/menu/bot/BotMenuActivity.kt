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
  private lateinit var editTextDoge: EditText
  private lateinit var buttonBot1: Button
  private lateinit var targetBalance: String
  private lateinit var highSeekBar2: SeekBar
  private lateinit var highSeekBar3: SeekBar
  private lateinit var highText2: TextView
  private lateinit var balanceText2: TextView
  private lateinit var highText3: TextView
  private lateinit var balanceText3: TextView
  private var getProgress2: Int = 9
  private var getProgress3: Int = 9

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
    highSeekBar2 = findViewById(R.id.seekBarHigh2)
    highSeekBar3 = findViewById(R.id.seekBarHigh3)
    highText2 = findViewById(R.id.textViewHigh2)
    balanceText2 = findViewById(R.id.textViewBalance2)
    highText3 = findViewById(R.id.textViewHigh3)
    balanceText3 = findViewById(R.id.textViewBalance3)

    balanceValue = user.getString("balanceValue").toBigDecimal()

    getProgress2 = (getProgress2 + 1) * 10
    highSeekBar2.progress = getProgress2
    highText2.text = "The Capital: $getProgress2%"
    balanceText2.text = ((BitCoinFormat().decimalToDoge(balanceValue) * getProgress2.toBigDecimal()) / BigDecimal(100)).toPlainString() + " DOGE"

    highSeekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        getProgress2 = (progress + 1) * 10
        highText2.text = "The Capital: $getProgress2%"
        balanceText2.text = ((BitCoinFormat().decimalToDoge(balanceValue) * getProgress2.toBigDecimal()) / BigDecimal(100)).toPlainString() + " DOGE"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    getProgress3 = (getProgress3 + 1) * 10
    highSeekBar3.progress = getProgress3
    highText3.text = "The Capital: $getProgress3%"
    balanceText3.text = ((BitCoinFormat().decimalToDoge(balanceValue) * getProgress3.toBigDecimal()) / BigDecimal(100)).toPlainString() + " DOGE"

    highSeekBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        getProgress3 = (progress + 1) * 10
        highText3.text = "The Capital: $getProgress3%"
        balanceText3.text = ((BitCoinFormat().decimalToDoge(balanceValue) * getProgress3.toBigDecimal()) / BigDecimal(100)).toPlainString() + " DOGE"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    buttonBot1.setOnClickListener {
      when {
        editTextDoge.text.toString().isEmpty() -> {
          Toast.makeText(this, "Doge cant be empty", Toast.LENGTH_SHORT).show()
        }
        editTextDoge.text.toString().toBigDecimal() < BigDecimal(3000) -> {
          Toast.makeText(this, "Minim doge 3000", Toast.LENGTH_SHORT).show()
        }
        editTextDoge.text.toString().toBigDecimal() > BigDecimal(21000) -> {
          Toast.makeText(this, "Max doge is 21000", Toast.LENGTH_SHORT).show()
        }
        else -> {
          targetBalance = BitCoinFormat().decimalToDoge(BitCoinFormat().dogeToDecimal(editTextDoge.text.toString().toBigDecimal())).toPlainString()
          startBot1()
        }
      }
    }

    botMode2.setOnClickListener {
      targetBalance = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal()).toPlainString()
      startBot2()
    }

    botMode3.setOnClickListener {
      targetBalance = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal()).toPlainString()
      startBot3()
    }
  }

  private fun bodyBot(target: String): HashMap<String, String> {
    uniqueCode = UUID.randomUUID().toString()
    body["a"] = target
    body["usertrade"] = user.getString("usernameDoge")
    body["passwordtrade"] = user.getString("passwordDoge")
    body["notrx"] = uniqueCode
    body["balanceawal"] = targetBalance
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
            goTo.putExtra("targetLow", getProgress2.toBigDecimal() / BigDecimal(100))
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
            goTo.putExtra("targetLow", getProgress3.toBigDecimal() / BigDecimal(100))
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