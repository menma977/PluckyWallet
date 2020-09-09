package com.plucky.wallet.view.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.R
import com.plucky.wallet.config.*
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
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
  private lateinit var upgradeTo: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var intentServiceUserShow: Intent
  private lateinit var intentServiceGetBalance: Intent
  private lateinit var intentServiceGetPlucky: Intent
  private lateinit var balanceValue: BigDecimal
  private lateinit var dollarValue: BigDecimal

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_manual)

    user = User(this)
    setting = Setting(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    dollar = findViewById(R.id.textViewDollar)
    balance = findViewById(R.id.textViewBalance)
    plucky = findViewById(R.id.textViewPlucky)
    lot = findViewById(R.id.textViewLot)
    lotProgress = findViewById(R.id.textViewProgressLot)
    lotTarget = findViewById(R.id.textViewTargetLot)
    progressBar = findViewById(R.id.progressBarLot)
    upgradeTo = findViewById(R.id.textViewUpgradeTo)

    plucky.text = bitCoinFormat.toPlucky(BigDecimal(user.getString("plucky"))).toPlainString()
    lot.text = user.getInteger("lot").toString()
    val valueProgress = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotProgress")))
    val valueTarget = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotTarget")))
    dollarValue = user.getString("dollar").toBigDecimal()
    progressBar.progress = ((valueProgress * BigDecimal(100.0)) / valueTarget).toInt()
    lotProgress.text = valueProgress.toPlainString()
    lotTarget.text = valueTarget.toPlainString()

    balance.text = user.getString("balanceText")
    balanceValue = user.getString("balanceValue").toBigDecimal()
    val countDollar = bitCoinFormat.decimalToDoge(balanceValue) * dollarValue
    dollar.text = bitCoinFormat.toDollar(countDollar).toPlainString()
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
      lot.text = user.getInteger("lot").toString()
      val valueProgress = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotProgress")))
      val valueTarget = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotTarget")))
      dollarValue = user.getString("dollar").toBigDecimal()
      progressBar.progress = ((valueProgress * BigDecimal(100.0)) / valueTarget).toInt()
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
    }
  }
}