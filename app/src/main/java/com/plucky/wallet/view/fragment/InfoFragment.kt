package com.plucky.wallet.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import com.plucky.wallet.view.NavigationActivity
import java.math.BigDecimal

class InfoFragment : Fragment() {
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var dollar: TextView
  private lateinit var balance: TextView
  private lateinit var plucky: TextView
  private lateinit var lot: TextView
  private lateinit var username: TextView
  private lateinit var email: TextView
  private lateinit var wallet: TextView
  private lateinit var sponsor: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var balanceValue: BigDecimal
  private lateinit var dollarValue: BigDecimal
  private lateinit var parentActivity: NavigationActivity
  private lateinit var backButton: ImageButton

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_info, container, false)

    parentActivity = activity as NavigationActivity

    user = User(parentActivity)
    setting = Setting(parentActivity)
    loading = Loading(parentActivity)
    bitCoinFormat = BitCoinFormat()

    backButton = root.findViewById(R.id.imageButtonBack)
    dollar = root.findViewById(R.id.textViewDollar)
    balance = root.findViewById(R.id.textViewBalance)
    plucky = root.findViewById(R.id.textViewPlucky)
    lot = root.findViewById(R.id.textViewLot)
    progressBar = root.findViewById(R.id.progressBarLot)
    username = root.findViewById(R.id.textViewUsername)
    email = root.findViewById(R.id.textViewEmail)
    wallet = root.findViewById(R.id.textViewWallet)
    sponsor = root.findViewById(R.id.textViewSponsor)

    username.text = user.getString("username")
    email.text = user.getString("email")
    wallet.text = user.getString("wallet")
    sponsor.text = user.getString("phone")

    lot.text = user.getInteger("lot").toString()
    val valueProgress = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotProgress")))
    val valueTarget = bitCoinFormat.decimalToDoge(BigDecimal(user.getString("lotTarget")))
    dollarValue = user.getString("dollar").toBigDecimal()
    progressBar.progress = valueProgress.toInt()
    progressBar.max = valueTarget.toInt()

    balance.text = user.getString("balanceText")
    balanceValue = user.getString("balanceValue").toBigDecimal()
    val countDollar = bitCoinFormat.decimalToDoge(balanceValue) * dollarValue
    dollar.text = bitCoinFormat.toDollar(countDollar).toPlainString()

    backButton.setOnClickListener {
      parentActivity.supportFragmentManager.popBackStack()
    }

    return root
  }

  override fun onResume() {
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverUserShow, IntentFilter("plucky.wallet.user.show"))
    LocalBroadcastManager.getInstance(parentActivity.applicationContext).registerReceiver(broadcastReceiverGetBalance, IntentFilter("plucky.wallet.balance.index"))
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
      progressBar.progress = valueProgress.toInt()
      progressBar.max = valueTarget.toInt()
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
}
