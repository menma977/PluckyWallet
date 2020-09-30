package com.plucky.wallet.view.menu.bot

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.MainActivity
import com.plucky.wallet.R
import com.plucky.wallet.config.*
import com.plucky.wallet.controller.WebController
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
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var response: JSONObject
  private lateinit var botMode2: Button
  private lateinit var botMode3: Button
  private lateinit var uniqueCode: String
  private val body = HashMap<String, String>()
  private lateinit var balanceValue: BigDecimal
  private lateinit var dollarValue: BigDecimal
  private lateinit var buttonBot1: Button
  private lateinit var currentBalance: String
  private lateinit var beatingBalance: String
  private lateinit var editTextDoge: EditText
  private lateinit var editTextDoge2: EditText
  private lateinit var editTextDoge3: EditText
  private lateinit var maxDepositText: TextView
  private lateinit var maxDepositText2: TextView
  private lateinit var maxDepositText3: TextView
  private lateinit var dollar: TextView
  private lateinit var balance: TextView
  private lateinit var plucky: TextView
  private lateinit var lot: TextView
  private lateinit var greade: TextView
  private lateinit var lotProgress: TextView
  private lateinit var lotTarget: TextView
  private lateinit var progressBar: ProgressBar
  private var lotValue: BigDecimal = BigDecimal(1)
  private var pluckyValue: BigDecimal = BigDecimal(0)
  private lateinit var intentServiceUserShow: Intent
  private lateinit var intentServiceGetBalance: Intent
  private lateinit var intentServiceGetPlucky: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_menu)

    loading = Loading(this)
    user = User(this)
    setting = Setting(this)
    bitCoinFormat = BitCoinFormat()

    loading.openDialog()

    dollar = findViewById(R.id.textViewDollar)
    balance = findViewById(R.id.textViewBalance)
    plucky = findViewById(R.id.textViewPlucky)
    lot = findViewById(R.id.textViewLot)
    greade = findViewById(R.id.textViewGrade)
    lotProgress = findViewById(R.id.textViewProgressLot)
    lotTarget = findViewById(R.id.textViewTargetLot)
    progressBar = findViewById(R.id.progressBarLot)

    buttonBot1 = findViewById(R.id.buttonBot1)
    botMode2 = findViewById(R.id.buttonBot2)
    botMode3 = findViewById(R.id.buttonBot3)
    editTextDoge = findViewById(R.id.editTextDoge)
    editTextDoge2 = findViewById(R.id.editTextDoge2)
    editTextDoge3 = findViewById(R.id.editTextDoge3)
    maxDepositText = findViewById(R.id.maxDeposit)
    maxDepositText2 = findViewById(R.id.maxDeposit2)
    maxDepositText3 = findViewById(R.id.maxDeposit3)

    greade.text = user.getString("grade")
    plucky.text = bitCoinFormat.toPlucky(BigDecimal(user.getString("plucky"))).toPlainString()
    lot.text = user.getInteger("lot").toString()
    lotValue = user.getInteger("lot").toString().toBigDecimal()
    pluckyValue = user.getString("plucky").toBigDecimal()
    val valueProgress = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotProgress")))
    val valueTarget = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotTarget")))
    dollarValue = user.getString("dollar").toBigDecimal()
    try {
      progressBar.progress = ((valueProgress * BigDecimal(100.0)) / valueTarget).toInt()
    } catch (e: Exception) {
      progressBar.progress = 0
    }
    lotProgress.text = valueProgress.toPlainString()
    lotTarget.text = valueTarget.toPlainString()

    balance.text = user.getString("balanceText")
    balanceValue = user.getString("balanceValue").toBigDecimal()
    val countDollar = bitCoinFormat.decimalToDoge(balanceValue) * dollarValue
    dollar.text = bitCoinFormat.toDollar(countDollar).toPlainString()

    buttonBot1.setOnClickListener {
      val balanceSet = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal())
      when {
        editTextDoge.text.toString().isEmpty() -> {
          Toast.makeText(this, "Doge cant be empty", Toast.LENGTH_SHORT).show()
        }
        editTextDoge.text.toString().toBigDecimal() < BigDecimal(3000) -> {
          Toast.makeText(this, "Min doge is 3000", Toast.LENGTH_SHORT).show()
        }
        editTextDoge.text.toString().toBigDecimal() > balanceSet -> {
          Toast.makeText(this, "Max doge is $balanceSet", Toast.LENGTH_SHORT).show()
        }
        editTextDoge.text.toString().toBigDecimal() > BigDecimal(22000) -> {
          Toast.makeText(this, "Max doge is 22000", Toast.LENGTH_SHORT).show()
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
        editTextDoge2.text.toString().toBigDecimal() < BigDecimal(3000) -> {
          Toast.makeText(this, "Min doge is 3000", Toast.LENGTH_SHORT).show()
        }
        editTextDoge2.text.toString().toBigDecimal() > balanceSet -> {
          Toast.makeText(this, "Max doge is $balanceSet", Toast.LENGTH_SHORT).show()
        }
        editTextDoge2.text.toString().toBigDecimal() > user.getString("maxDeposit").replace(",", ".").toBigDecimal() -> {
          Toast.makeText(this, "Max doge is ${user.getString("maxDeposit").replace(",", ".").toBigDecimal()}", Toast.LENGTH_SHORT).show()
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
        editTextDoge3.text.toString().toBigDecimal() < BigDecimal(3000) -> {
          Toast.makeText(this, "Min doge is 3000", Toast.LENGTH_SHORT).show()
        }
        editTextDoge3.text.toString().toBigDecimal() > balanceSet -> {
          Toast.makeText(this, "Max doge is $balanceSet", Toast.LENGTH_SHORT).show()
        }
        editTextDoge3.text.toString().toBigDecimal() > user.getString("maxDeposit").replace(",", ".").toBigDecimal() -> {
          Toast.makeText(this, "Max doge is ${user.getString("maxDeposit").replace(",", ".").toBigDecimal()}", Toast.LENGTH_SHORT).show()
        }
        else -> {
          currentBalance = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal()).toPlainString()
          beatingBalance = BitCoinFormat().decimalToDoge(BitCoinFormat().dogeToDecimal(editTextDoge3.text.toString().toBigDecimal())).toPlainString()
          balanceValue = BitCoinFormat().dogeToDecimal(editTextDoge3.text.toString().toBigDecimal())

          startBot3()
        }
      }
    }

    getPlucky()
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

        runOnUiThread {
          val balanceSet = BitCoinFormat().decimalToDoge(user.getString("balanceValue").toBigDecimal())
          if (balanceSet < user.getString("maxDeposit").toBigDecimal()) {
            editTextDoge2.setText(balanceSet.toPlainString())
            editTextDoge3.setText(balanceSet.toPlainString())
          } else {
            editTextDoge2.setText(user.getString("maxDeposit"))
            editTextDoge3.setText(user.getString("maxDeposit"))
          }

          if (balanceSet < BigDecimal(22000)) {
            editTextDoge.setText(balanceSet.toPlainString())
          } else {
            editTextDoge.setText(BigDecimal(22000).toPlainString())
          }

          maxDepositText2.text = "Max: " + user.getString("maxDeposit")
          maxDepositText3.text = "Max: " + user.getString("maxDeposit")

          loading.closeDialog()

          if (user.getBoolean("isLogout")) {
            onLogout()
          }
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

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverGetBalance, IntentFilter("plucky.wallet.balance.index"))

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverGetPlucky, IntentFilter("plucky.wallet.user.show.plucky"))
    }
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverUserShow)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverGetBalance)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverGetPlucky)

    stopService(intentServiceUserShow)
    stopService(intentServiceGetBalance)
    stopService(intentServiceGetPlucky)
    super.onStop()
  }

  override fun onBackPressed() {
    stopService(intentServiceUserShow)
    stopService(intentServiceGetBalance)
    stopService(intentServiceGetPlucky)
    super.onBackPressed()
  }

  private var broadcastReceiverUserShow: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      lot.text = user.getInteger("lot").toString()
      lotValue = user.getInteger("lot").toString().toBigDecimal()
      val valueProgress = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotProgress")))
      val valueTarget = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotTarget")))
      dollarValue = user.getString("dollar").toBigDecimal()
      try {
        progressBar.progress = ((valueProgress * BigDecimal(100.0)) / valueTarget).toInt()
      } catch (e: Exception) {
        progressBar.progress = 0
      }
      lotProgress.text = valueProgress.toPlainString()
      lotTarget.text = valueTarget.toPlainString()

      if (user.getBoolean("isLogout")) {
        onLogout()
      }
    }
  }
  private var broadcastReceiverGetBalance: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance.text = user.getString("balanceText")
      val countDollar = bitCoinFormat.decimalToDoge(balanceValue) * dollarValue
      dollar.text = bitCoinFormat.toDollar(countDollar).toPlainString()
    }
  }
  private var broadcastReceiverGetPlucky: BroadcastReceiver = object : BroadcastReceiver() {
    @SuppressLint("SetTextI18n")
    override fun onReceive(context: Context, intent: Intent) {
      plucky.text = bitCoinFormat.toPlucky(BigDecimal(user.getString("plucky"))).toPlainString()
      greade.text = user.getString("grade")

      maxDepositText2.text = "Max: " + user.getString("maxDeposit")
      maxDepositText3.text = "Max: " + user.getString("maxDeposit")
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

  fun onLogout() {
    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiverUserShow)
    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiverGetBalance)
    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(broadcastReceiverGetPlucky)

    stopService(intentServiceUserShow)
    stopService(intentServiceGetBalance)
    stopService(intentServiceGetPlucky)
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