package com.plucky.wallet.view.menu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.zxing.Result
import com.plucky.wallet.R
import com.plucky.wallet.config.BackgroundGetBalance
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.model.User
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class SendBalanceActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var response: JSONObject
  private lateinit var frameScanner: FrameLayout
  private lateinit var scannerEngine: ZXingScannerView
  private lateinit var wallet: String
  private lateinit var userBalance: TextView
  private lateinit var balanceText: EditText
  private lateinit var sendDoge: Button
  private lateinit var walletText: EditText
  private lateinit var secondaryPasswordText: EditText
  private lateinit var intentServiceGetBalance: Intent
  private lateinit var balanceValue: BigDecimal
  private var isHasCode = false
  private var isStart = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_send_balance)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    userBalance = findViewById(R.id.textViewBalance)
    walletText = findViewById(R.id.editTextWallet)
    frameScanner = findViewById(R.id.frameLayoutScanner)
    balanceText = findViewById(R.id.editTextBalance)
    secondaryPasswordText = findViewById(R.id.editTextSecondaryPassword)
    sendDoge = findViewById(R.id.buttonSend)

    initScannerView()

    frameScanner.setOnClickListener {
      if (isStart) {
        scannerEngine.startCamera()
        isStart = false
      }
    }

    sendDoge.setOnClickListener {
      loading.openDialog()
      onSendDoge()
    }
    val textBalanceView = user.getString("balanceText") + " DOGE"
    userBalance.text = textBalanceView
    balanceValue = user.getString("balanceValue").toBigDecimal()
  }

  private fun onSendDoge() {
    val doge = balanceText.text.toString().toBigDecimal()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["wallet"] = walletText.text.toString()
      body["amount"] = bitCoinFormat.dogeToDecimal(doge).toPlainString()
      body["sessionCookie"] = user.getString("key")
      body["secondary_password"] = secondaryPasswordText.text.toString()
      response = WebController.Post("doge.store", user.getString("token"), body).execute().get()
      println(response)
      if (response.getInt("code") == 200) {
        runOnUiThread {
          Toast.makeText(applicationContext, "wait until the doge balance is received", Toast.LENGTH_LONG).show()
          loading.closeDialog()
          finish()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun initScannerView() {
    scannerEngine = ZXingScannerView(this)
    scannerEngine.setAutoFocus(true)
    scannerEngine.setResultHandler(this)
    frameScanner.addView(scannerEngine)
  }

  override fun onStart() {
    super.onStart()
    loading.openDialog()
    Timer().schedule(1000) {
      intentServiceGetBalance = Intent(applicationContext, BackgroundGetBalance::class.java)
      startService(intentServiceGetBalance)

      LocalBroadcastManager.getInstance(applicationContext).registerReceiver(broadcastReceiverGetBalance, IntentFilter("plucky.wallet.balance.index"))
      loading.closeDialog()
    }
  }

  override fun onPause() {
    scannerEngine.stopCamera()
    super.onPause()
  }

  override fun onStop() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverGetBalance)
    stopService(intentServiceGetBalance)
    super.onStop()
  }

  override fun onBackPressed() {
    stopService(intentServiceGetBalance)
    super.onBackPressed()
  }

  override fun handleResult(rawResult: Result?) {
    if (rawResult?.text?.isNotEmpty()!!) {
      isHasCode = true
      wallet = rawResult.text.toString()
      walletText.setText(wallet)
    } else {
      isHasCode = false
    }
  }

  private var broadcastReceiverGetBalance: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val textBalanceView = user.getString("balanceText") + " DOGE"
      userBalance.text = textBalanceView
      balanceValue = user.getString("balanceValue").toBigDecimal()
    }
  }
}