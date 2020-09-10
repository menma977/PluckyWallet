package com.plucky.wallet.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import com.plucky.wallet.view.NavigationActivity
import com.plucky.wallet.view.menu.BotManualActivity
import com.plucky.wallet.view.menu.HistoryInActivity
import com.plucky.wallet.view.menu.HistoryLotActivity
import com.plucky.wallet.view.menu.HistoryOutActivity
import java.math.BigDecimal

class HomeFragment : Fragment() {
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var goTo: Intent
  private lateinit var dollar: TextView
  private lateinit var balance: TextView
  private lateinit var plucky: TextView
  private lateinit var lot: TextView
  private lateinit var lotProgress: TextView
  private lateinit var lotTarget: TextView
  private lateinit var logout: ImageButton
  private lateinit var progressBar: ProgressBar
  private lateinit var parentActivity: NavigationActivity
  private lateinit var balanceValue: BigDecimal
  private lateinit var dollarValue: BigDecimal
  private lateinit var lotButton: LinearLayout
  private lateinit var incomeButton: LinearLayout
  private lateinit var outComeButton: LinearLayout
  private lateinit var automaticStakeButton: LinearLayout
  private lateinit var manualStakeButton: LinearLayout
  private lateinit var dogeChainButton: LinearLayout

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_home, container, false)

    parentActivity = activity as NavigationActivity

    user = User(parentActivity)
    setting = Setting(parentActivity)
    loading = Loading(parentActivity)
    bitCoinFormat = BitCoinFormat()

    dollar = root.findViewById(R.id.textViewDollar)
    balance = root.findViewById(R.id.textViewBalance)
    plucky = root.findViewById(R.id.textViewPlucky)
    lot = root.findViewById(R.id.textViewLot)
    lotProgress = root.findViewById(R.id.textViewProgressLot)
    lotTarget = root.findViewById(R.id.textViewTargetLot)
    progressBar = root.findViewById(R.id.progressBarLot)
    logout = root.findViewById(R.id.imageButtonLogout)
    lotButton = root.findViewById(R.id.linearLayoutLot)
    incomeButton = root.findViewById(R.id.linearLayoutIncome)
    outComeButton = root.findViewById(R.id.linearLayoutOutCome)
    automaticStakeButton = root.findViewById(R.id.linearLayoutAutomaticStake)
    manualStakeButton = root.findViewById(R.id.linearLayoutManualStake)
    dogeChainButton = root.findViewById(R.id.linearLayoutDogeChain)

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

    logout.setOnClickListener {
      loading.openDialog()
      parentActivity.onLogout()
    }

    lotButton.setOnClickListener {
      goTo = Intent(parentActivity, HistoryLotActivity::class.java)
      startActivity(goTo)
    }

    incomeButton.setOnClickListener {
      goTo = Intent(parentActivity, HistoryInActivity::class.java)
      startActivity(goTo)
    }

    outComeButton.setOnClickListener {
      goTo = Intent(parentActivity, HistoryOutActivity::class.java)
      startActivity(goTo)
    }

    dogeChainButton.setOnClickListener {
      goTo = Intent(Intent.ACTION_VIEW, Uri.parse("https://dogechain.info/address/${user.getString("wallet")}"))
      startActivity(goTo)
    }

    manualStakeButton.setOnClickListener {
      if (user.getBoolean("isWin")) {
        Toast.makeText(parentActivity, "Stake can only be used once a day", Toast.LENGTH_SHORT).show()
      } else {
        goTo = Intent(parentActivity, BotManualActivity::class.java)
        startActivity(goTo)
      }
    }

    return root
  }

  override fun onResume() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverUserShow, IntentFilter("plucky.wallet.user.show"))
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverGetBalance, IntentFilter("plucky.wallet.balance.index"))
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverGetPlucky, IntentFilter("plucky.wallet.user.show.plucky"))
    super.onResume()
  }

  override fun onDestroy() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).unregisterReceiver(broadcastReceiverUserShow)
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).unregisterReceiver(broadcastReceiverGetBalance)
    super.onDestroy()
  }

  private var broadcastReceiverUserShow: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
