package com.plucky.wallet.view.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.MainActivity
import com.plucky.wallet.R
import com.plucky.wallet.config.*
import com.plucky.wallet.controller.DogeController
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class BotManualActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var dollar: TextView
  private lateinit var balance: TextView
  private lateinit var plucky: TextView
  private lateinit var lot: TextView
  private lateinit var lotProgress: TextView
  private lateinit var lotTarget: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var intentServiceUserShow: Intent
  private lateinit var intentServiceGetBalance: Intent
  private lateinit var intentServiceGetPlucky: Intent
  private lateinit var balanceValue: BigDecimal
  private lateinit var dollarValue: BigDecimal
  private lateinit var response: JSONObject
  private lateinit var fundText: TextView
  private lateinit var highText: TextView
  private lateinit var statusText: TextView
  private lateinit var highSeekBar: SeekBar
  private lateinit var inputBalance: EditText
  private lateinit var stakeButton: Button
  private lateinit var fundLinearLayout: LinearLayout
  private lateinit var highLinearLayout: LinearLayout
  private lateinit var resultLinearLayout: LinearLayout
  private lateinit var statusLinearLayout: LinearLayout
  private lateinit var profit: BigDecimal
  private lateinit var startBalance: BigDecimal
  private lateinit var goTo: Intent
  private var payIn: BigDecimal = BigDecimal(0)
  private var payInMultiple: BigDecimal = BigDecimal(1)
  private var high = BigDecimal(5)
  private var maxRow = 10
  private var seed = (0..99999).random().toString()
  private lateinit var percentTable: ArrayList<Double>
  private var percent = 1.0
  private var maxBalance = BigDecimal(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_manual)

    user = User(this)
    setting = Setting(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()
    percentTable = ArrayList()

    dollar = findViewById(R.id.textViewDollar)
    balance = findViewById(R.id.textViewBalance)
    plucky = findViewById(R.id.textViewPlucky)
    lot = findViewById(R.id.textViewLot)
    lotProgress = findViewById(R.id.textViewProgressLot)
    lotTarget = findViewById(R.id.textViewTargetLot)
    progressBar = findViewById(R.id.progressBarLot)
    fundText = findViewById(R.id.textViewFund)
    highText = findViewById(R.id.textViewHigh)
    statusText = findViewById(R.id.textViewStatus)
    inputBalance = findViewById(R.id.editTextInputBalance)
    highSeekBar = findViewById(R.id.seekBarHigh)
    stakeButton = findViewById(R.id.buttonStake)
    fundLinearLayout = findViewById(R.id.linearLayoutFund)
    highLinearLayout = findViewById(R.id.linearLayoutHigh)
    resultLinearLayout = findViewById(R.id.linearLayoutResult)
    statusLinearLayout = findViewById(R.id.linearLayoutStatus)

    plucky.text = bitCoinFormat.toPlucky(BigDecimal(user.getString("plucky"))).toPlainString()
    lot.text = user.getInteger("lot").toString()
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

    highText.text = "Possibility: ${high * BigDecimal(10)}%"
    startBalance = balanceValue
    maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balanceValue).multiply(BigDecimal(0.01)))
    fundText.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
    payIn = balanceValue.multiply(BigDecimal(0.01))

    setListTargetMaximum()

    stakeButton.setOnClickListener {
      if (inputBalance.text.isEmpty()) {
        Toast.makeText(this, "Amount cant not be empty", Toast.LENGTH_SHORT).show()
      } else {
        payIn = bitCoinFormat.dogeToDecimal(inputBalance.text.toString().toBigDecimal())
        if (payIn > maxBalance) {
          Toast.makeText(this, "Doge you can input should not be more than ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}", Toast.LENGTH_LONG).show()
        } else {
          onBetting()
        }
      }
    }

    highSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        var getProgress = progress
        if (progress == 0) {
          highSeekBar.progress = 1
          getProgress = 1
        }
        if (progress == 10) {
          highSeekBar.progress = 9
          getProgress = 9
        }
        if (progress >= 0 || progress <= 10) {
          percent = percentTable[getProgress - 1]
        }
        maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(payIn.multiply(percent.toBigDecimal())).multiply(payInMultiple))
        fundText.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
        high = getProgress.toBigDecimal()
        highText.text = "Possibility: ${getProgress * 10}%"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {}
      override fun onStopTrackingTouch(seekBar: SeekBar) {}
    })

    setDefaultView()
  }

  private fun onBetting() {
    loading.openDialog()
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "PlaceBet"
      body["s"] = user.getString("key")
      body["Low"] = "0"
      body["High"] = (high.multiply(BigDecimal(10)).multiply(BigDecimal(10000)) - BigDecimal(600)).toPlainString()
      body["PayIn"] = payIn.toPlainString()
      body["ProtocolVersion"] = "2"
      body["ClientSeed"] = seed
      body["Currency"] = "doge"
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        seed = response.getJSONObject("data")["Next"].toString()
        val puyOut = response.getJSONObject("data")["PayOut"].toString().toBigDecimal()
        var balanceRemaining = response.getJSONObject("data")["StartingBalance"].toString().toBigDecimal()

        profit = puyOut - payIn
        balanceRemaining += profit
        val winBot = profit > BigDecimal(0)

        runOnUiThread {
          balanceValue = balanceRemaining
          balance.text = "${bitCoinFormat.decimalToDoge(balanceRemaining).toPlainString()} DOGE"

          user.setString("balanceValue", balanceValue.toPlainString())
          user.setString("balanceText", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")

          setView(bitCoinFormat.decimalToDoge(payIn).toPlainString(), fundLinearLayout, false, winBot)
          setView("${high.multiply(BigDecimal(10))}%", highLinearLayout, false, winBot)
          setView(BitCoinFormat().decimalToDoge(puyOut).toPlainString(), resultLinearLayout, false, winBot)
          if (winBot) {
            setView("WIN", statusLinearLayout, false, winBot)
            stakeButton.visibility = Button.GONE
            statusText.text = "WIN"
            statusText.setTextColor(getColor(R.color.Success))
            response = WebController.Get("doge.update", user.getString("token")).execute().get()

            inputBalance.isEnabled = false
          } else {
            setView("LOSE", statusLinearLayout, false, winBot)
            statusText.text = "LOSE"
            statusText.setTextColor(getColor(R.color.Danger))
            highSeekBar.progress = 5

            payInMultiple = BigDecimal(2)

            maxBalance = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(payIn.multiply(percent.toBigDecimal())).multiply(payInMultiple))
            fundText.text = "Maximum : ${bitCoinFormat.decimalToDoge(maxBalance).toPlainString()}"
            println(payInMultiple)
            inputBalance.isEnabled = true
          }

          inputBalance.setText("")
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun setDefaultView() {
    setView("Fund", fundLinearLayout, isNew = true, isWin = false)
    setView("Possibility", highLinearLayout, isNew = true, isWin = false)
    setView("Result", resultLinearLayout, isNew = true, isWin = false)
    setView("Status", statusLinearLayout, isNew = true, isWin = false)

    for (i in 0 until maxRow) {
      setView("", fundLinearLayout, isNew = true, isWin = false)
      setView("", highLinearLayout, isNew = true, isWin = false)
      setView("", resultLinearLayout, isNew = true, isWin = false)
      setView("", statusLinearLayout, isNew = true, isWin = false)
    }
  }

  private fun setView(value: String, linearLayout: LinearLayout, isNew: Boolean, isWin: Boolean) {
    val template = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val valueView = TextView(applicationContext)
    valueView.text = value
    valueView.gravity = Gravity.CENTER
    valueView.layoutParams = template
    if (isNew) {
      valueView.setTextColor(getColor(R.color.colorAccent))
    } else {
      if (isWin) {
        valueView.setTextColor(getColor(R.color.Success))
        stakeButton.visibility = Button.GONE
        user.setBoolean("isWin", true)
      } else {
        valueView.setTextColor(getColor(R.color.Danger))
      }
    }

    if ((linearLayout.childCount - 1) == maxRow) {
      linearLayout.removeViewAt(linearLayout.childCount - 1)
      linearLayout.addView(valueView, 1)
    } else {
      linearLayout.addView(valueView)
    }
  }

  private fun setListTargetMaximum() {
    percentTable.add(0.1)
    percentTable.add(0.25)
    percentTable.add(0.4)
    percentTable.add(0.7)
    percentTable.add(1.0)
    percentTable.add(1.6)
    percentTable.add(2.4)
    percentTable.add(4.1)
    percentTable.add(9.0)
  }

  private fun onLogout() {
    Timer().schedule(100) {
      response = WebController.Get("user.logout", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        user.clear()
        setting.clear()
        goTo = Intent(applicationContext, MainActivity::class.java)
        loading.closeDialog()
        startActivity(goTo)
        finishAffinity()
      } else {
        if (response.getString("data").contains("Unauthenticated.")) {
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

  override fun onStart() {
    super.onStart()
    loading.openDialog()
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
      loading.closeDialog()
    }
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverUserShow)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverGetBalance)

    stopService(intentServiceUserShow)
    stopService(intentServiceGetBalance)
    super.onStop()
  }

  override fun onBackPressed() {
    stopService(intentServiceUserShow)
    stopService(intentServiceGetBalance)
    super.onBackPressed()
  }

  private var broadcastReceiverUserShow: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (intent.getBooleanExtra("isLogout", false)) {
        onLogout()
      } else {
        lot.text = user.getInteger("lot").toString()
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
      }
    }
  }
  private var broadcastReceiverGetBalance: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance.text = user.getString("balanceText")
      balanceValue = user.getString("balanceValue").toBigDecimal()
      val countDollar = bitCoinFormat.decimalToDoge(balanceValue) * dollarValue
      dollar.text = bitCoinFormat.toDollar(countDollar).toPlainString()
    }
  }
  private var broadcastReceiverGetPlucky: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      plucky.text = bitCoinFormat.toPlucky(BigDecimal(user.getString("plucky"))).toPlainString()
    }
  }
}