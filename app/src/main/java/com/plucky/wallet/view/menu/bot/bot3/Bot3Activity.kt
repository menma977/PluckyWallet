package com.plucky.wallet.view.menu.bot.bot3

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.controller.DogeController
import com.plucky.wallet.model.User
import com.plucky.wallet.view.menu.bot.ResultActivity
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

class Bot3Activity : AppCompatActivity() {
  private lateinit var cubicLineChart: ValueLineChart
  private lateinit var series: ValueLineSeries
  private lateinit var goTo: Intent
  private lateinit var progressBar: ProgressBar
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var balance: BigDecimal
  private lateinit var balanceTarget: BigDecimal
  private lateinit var balanceRemaining: BigDecimal
  private lateinit var payIn: BigDecimal
  private lateinit var payOut: BigDecimal
  private lateinit var profit: BigDecimal
  private lateinit var usernameView: TextView
  private lateinit var balanceView: TextView
  private lateinit var balanceRemainingView: TextView
  private lateinit var uniqueCode: String
  private var rowChart = 1
  private var loseBot = false
  private var balanceLimitTarget = BigDecimal(0.06)
  private var balanceLimitTargetLow = BigDecimal(0)
  private var formula = 1
  private var seed = (0..99999).random().toString()
  private var thread = Thread()
  private var mode = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot2)

    loading = Loading(this)
    user = User(this)
    bitCoinFormat = BitCoinFormat()

    uniqueCode = intent.getSerializableExtra("uniqueCode") as String
    mode = intent.getIntExtra("mode", 0)

    usernameView = findViewById(R.id.textViewUsername)
    balanceView = findViewById(R.id.textViewBalance)
    balanceRemainingView = findViewById(R.id.textViewRemainingBalance)
    progressBar = findViewById(R.id.progressBar)
    cubicLineChart = findViewById(R.id.cubicLineChart)

    series = ValueLineSeries()

    loading.openDialog()
    balance = intent.getSerializableExtra("balance") as BigDecimal
    balanceLimitTarget = intent.getSerializableExtra("target") as BigDecimal
    balanceLimitTargetLow = intent.getSerializableExtra("targetLow") as BigDecimal
    balanceRemaining = balance
    balanceTarget = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge((balance * balanceLimitTarget) + balance))
    payIn = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * BigDecimal(0.001))
    balanceLimitTargetLow = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * balanceLimitTargetLow)

    usernameView.text = user.getString("username")
    balanceView.text = "${bitCoinFormat.decimalToDoge(balance).toPlainString()} DOGE"
    balanceRemainingView.text = bitCoinFormat.decimalToDoge(balanceRemaining).toPlainString()

    progress(balance, balanceRemaining, balanceTarget)
    configChart()
    loading.closeDialog()
    thread = Thread {
      onBotMode()
    }
    thread.start()
  }

  override fun onBackPressed() {
    Toast.makeText(this, "Cannot Return When playing a bot", Toast.LENGTH_LONG).show()
  }

  private fun configChart() {
    series.color = getColor(R.color.colorAccent)
    cubicLineChart.axisTextColor = getColor(R.color.textSecondary)
    cubicLineChart.containsPoints()
    cubicLineChart.isUseDynamicScaling = true
    cubicLineChart.addSeries(series)
    cubicLineChart.startAnimation()
    series.addPoint(ValueLinePoint("0", bitCoinFormat.decimalToDoge(balanceRemaining).toFloat()))
  }

  private fun progress(start: BigDecimal, remaining: BigDecimal, end: BigDecimal) {
    progressBar.min = bitCoinFormat.decimalToDoge(start).setScale(0, BigDecimal.ROUND_HALF_DOWN).toPlainString().toInt()
    progressBar.progress = bitCoinFormat.decimalToDoge(remaining).setScale(0, BigDecimal.ROUND_HALF_DOWN).toPlainString().toInt()
    progressBar.max = bitCoinFormat.decimalToDoge(end).setScale(0, BigDecimal.ROUND_HALF_DOWN).toPlainString().toInt()
  }

  private fun onBotMode() {
    var time = System.currentTimeMillis()
    val trigger = Object()
    synchronized(trigger) {
      while (balanceRemaining in balanceLimitTargetLow..balanceTarget) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 1000) {
          time = System.currentTimeMillis()
          payIn *= formula.toBigDecimal()
          val body = HashMap<String, String>()
          body["a"] = "PlaceBet"
          body["s"] = user.getString("key")
          body["Low"] = "0"
          body["High"] = "500000"
          body["PayIn"] = payIn.toPlainString()
          body["ProtocolVersion"] = "2"
          body["ClientSeed"] = seed
          body["Currency"] = "doge"
          response = DogeController(body).execute().get()
          if (response["code"] == 200) {
            seed = response.getJSONObject("data")["Next"].toString()
            payOut = response.getJSONObject("data")["PayOut"].toString().toBigDecimal()
            balanceRemaining = response.getJSONObject("data")["StartingBalance"].toString().toBigDecimal()
            profit = payOut - payIn
            balanceRemaining += profit
            loseBot = profit < BigDecimal(0)
            payIn = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * BigDecimal(0.001))

            if (loseBot) {
              formula *= 2
            } else {
              formula = 1
              payIn = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * BigDecimal(0.001))
            }

            runOnUiThread {
              balanceRemainingView.text = "${bitCoinFormat.decimalToDoge(balanceRemaining).toPlainString()} DOGE"

              progress(balance, balanceRemaining, balanceTarget)
              if (rowChart >= 39) {
                series.series.removeAt(0)
              }
              series.addPoint(ValueLinePoint("$rowChart", bitCoinFormat.decimalToDoge(balanceRemaining).toFloat()))
              cubicLineChart.addSeries(series)
              cubicLineChart.refreshDrawableState()
            }

            rowChart++
          } else if (response["code"] == 404) {
            break
          } else {
            runOnUiThread {
              balanceRemainingView.text = "sleep mode Active"
              Toast.makeText(applicationContext, "sleep mode Active Wait to continue", Toast.LENGTH_LONG).show()
            }
            trigger.wait(60000)
          }
        }
      }

      goTo = Intent(applicationContext, ResultActivity::class.java)
      if (balanceRemaining >= balanceTarget) {
        goTo.putExtra("status", "WIN")
      } else {
        goTo.putExtra("status", "CUT LOSS")
      }
      goTo.putExtra("mode", mode)
      goTo.putExtra("startBalance", balance)
      goTo.putExtra("balanceRemaining", balanceRemaining)
      goTo.putExtra("uniqueCode", intent.getSerializableExtra("uniqueCode") as String)
      runOnUiThread {
        startActivity(goTo)
        finish()
      }
    }
  }
}