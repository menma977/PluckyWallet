package com.plucky.wallet.view.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.MainActivity
import com.plucky.wallet.R
import com.plucky.wallet.config.*
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class UpgradeAccountActivity : AppCompatActivity() {
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var dollar: TextView
  private lateinit var balance: TextView
  private lateinit var plucky: TextView
  private lateinit var lot: TextView
  private lateinit var greade: TextView
  private lateinit var lotProgress: TextView
  private lateinit var lotTarget: TextView
  private lateinit var upgradeTo: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var intentServiceUserShow: Intent
  private lateinit var intentServiceGetBalance: Intent
  private lateinit var intentServiceGetPlucky: Intent
  private lateinit var balanceValue: BigDecimal
  private lateinit var dollarValue: BigDecimal
  private lateinit var lotPrice: TextView
  private lateinit var requestPlucky: TextView
  private lateinit var secondaryPassword: TextView
  private lateinit var buy: Button
  private var idValueServer: Int = 0
  private var lotValue: BigDecimal = BigDecimal(1)
  private var lotValueMax: BigDecimal = BigDecimal(1)
  private var pluckyValue: BigDecimal = BigDecimal(0)
  private var pluckyValueServer: BigDecimal = BigDecimal(0)
  private lateinit var container: LinearLayout
  private lateinit var goTo: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_upgrade_account)

    user = User(this)
    setting = Setting(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    dollar = findViewById(R.id.textViewDollar)
    balance = findViewById(R.id.textViewBalance)
    plucky = findViewById(R.id.textViewPlucky)
    lot = findViewById(R.id.textViewLot)
    greade = findViewById(R.id.textViewGrade)
    lotProgress = findViewById(R.id.textViewProgressLot)
    lotTarget = findViewById(R.id.textViewTargetLot)
    progressBar = findViewById(R.id.progressBarLot)
    upgradeTo = findViewById(R.id.textViewUpgradeTo)
    lotPrice = findViewById(R.id.textViewLotPrice)
    requestPlucky = findViewById(R.id.textViewRequestPlucky)
    secondaryPassword = findViewById(R.id.editTextSecondaryPassword)
    container = findViewById(R.id.linearLayoutDataContent)
    buy = findViewById(R.id.buttonBuy)

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

    buy.setOnClickListener {
      upgradeSet(1)
    }

    upgradeSet(0)
  }

  private fun upgradeSet(type: Int) {
    buy.visibility = Button.GONE
    loading.openDialog()
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val descriptionParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)

    Timer().schedule(1000) {
      response = WebController.Get("lot.create", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        val idValue = response.getJSONObject("data").getJSONObject("nextLot").getString("id")
        val lotValue = response.getJSONObject("data").getJSONObject("nextLot").getString("price")
        val pluckyValue = response.getJSONObject("data").getJSONObject("nextLot").getString("plucky")

        idValueServer = idValue.toInt()
        pluckyValueServer = pluckyValue.toBigDecimal()
        lotValueMax = idValue.toBigDecimal()

        runOnUiThread {
          if (type == 0) {
            val dataGrabber = response.getJSONObject("data").getJSONArray("dataQueue")
            for (i in 0 until dataGrabber.length()) {
              runOnUiThread {
                //body container
                val containerLinearLayout = LinearLayout(applicationContext)
                containerLinearLayout.layoutParams = linearLayoutParams
                containerLinearLayout.gravity = Gravity.CENTER
                containerLinearLayout.orientation = LinearLayout.VERTICAL
                containerLinearLayout.setBackgroundResource(R.drawable.card_default_2)
                containerLinearLayout.setPadding(10, 10, 10, 10)
                containerLinearLayout.elevation = 20F
                //description input sub container 1
                val user = TextView(applicationContext)
                user.layoutParams = descriptionParams
                user.text = dataGrabber.getJSONObject(i).getString("user")
                user.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
                user.gravity = Gravity.CENTER
                containerLinearLayout.addView(user)
                val value = TextView(applicationContext)
                value.layoutParams = descriptionParams
                value.text = "${bitCoinFormat.decimalToDoge(dataGrabber.getJSONObject(i).getString("value").toBigDecimal()).toPlainString()} DOGE"
                value.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
                value.gravity = Gravity.CENTER
                containerLinearLayout.addView(value)
                //set container to parent container
                container.addView(containerLinearLayout)
                val wrapLine = View(applicationContext)
                wrapLine.layoutParams = line
                wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.Dark))
                container.addView(wrapLine)
              }
            }

            upgradeTo.text = "Upgrade To Level $idValue"
            lotPrice.text = "Request DOGE : ${bitCoinFormat.decimalToDoge(BigDecimal(lotValue))}"
            requestPlucky.text = "Request Plucky : ${bitCoinFormat.toPlucky(BigDecimal(pluckyValue))}"
            loading.closeDialog()
            buy.visibility = Button.VISIBLE
          } else {
            onBuy()
          }
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, "error when opening data. open the menu again to proceed", Toast.LENGTH_LONG).show()
          loading.closeDialog()
          buy.visibility = Button.VISIBLE
          finish()
        }
      }
    }
  }

  private fun onBuy() {
    Timer().schedule(2500) {
      when {
        secondaryPassword.text.isEmpty() -> {
          runOnUiThread {
            Toast.makeText(applicationContext, "Secondary Password is required", Toast.LENGTH_SHORT).show()
            secondaryPassword.requestFocus()
            loading.closeDialog()
            buy.visibility = Button.VISIBLE
          }
        }
        lotValue >= lotValueMax -> {
          runOnUiThread {
            Toast.makeText(applicationContext, "You are input max upgrade", Toast.LENGTH_SHORT).show()
            loading.closeDialog()
            buy.visibility = Button.VISIBLE
          }
        }
        pluckyValue < pluckyValueServer -> {
          runOnUiThread {
            Toast.makeText(applicationContext, "your Plucky are less than demand", Toast.LENGTH_SHORT).show()
            loading.closeDialog()
            buy.visibility = Button.VISIBLE
          }
        }
        else -> {
          val body = HashMap<String, String>()
          body["lot"] = idValueServer.toString()
          body["balance"] = balanceValue.toPlainString()
          body["secondary_password"] = secondaryPassword.text.toString()
          response = WebController.Post("lot.store", user.getString("token"), body).execute().get()
          if (response.getInt("code") == 200) {
            runOnUiThread {
              user.setBoolean("onQueue", true)
              Toast.makeText(applicationContext, response.getJSONObject("data").getString("message"), Toast.LENGTH_SHORT).show()
              loading.closeDialog()
              goTo = Intent(applicationContext, MainActivity::class.java)
              finishAffinity()
              startActivity(goTo)
            }
          } else {
            runOnUiThread {
              Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
              loading.closeDialog()
              buy.visibility = Button.VISIBLE
            }
          }
        }
      }
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
      greade.text = user.getString("grade")
    }
  }
}