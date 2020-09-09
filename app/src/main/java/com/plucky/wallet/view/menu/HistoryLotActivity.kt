package com.plucky.wallet.view.menu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import kotlin.concurrent.schedule

class HistoryLotActivity : AppCompatActivity() {
  private lateinit var container: LinearLayout
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history_lot)

    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    container = findViewById(R.id.linearLayoutDataContent)
    container.removeAllViews()

    setView()
  }

  private fun setView() {
    loading.openDialog()
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val iconImageParams = LinearLayout.LayoutParams(50, 50)
    val descriptionParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val totalDogeParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val dateParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)

    Timer().schedule(1000) {
      response = WebController.Get("lot", user.getString("token")).execute().get()
      if (response.getInt("code") == 200) {
        val dataGrabber = response.getJSONObject("data").getJSONArray("lot")
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
            //sub container 1
            val containerLinearLayoutSub1 = LinearLayout(applicationContext)
            containerLinearLayoutSub1.layoutParams = linearLayoutParams
            containerLinearLayoutSub1.gravity = Gravity.CENTER
            containerLinearLayoutSub1.orientation = LinearLayout.HORIZONTAL
            //sub container 2
            val containerLinearLayoutSub2 = LinearLayout(applicationContext)
            containerLinearLayoutSub2.layoutParams = linearLayoutParams
            containerLinearLayoutSub2.gravity = Gravity.CENTER
            containerLinearLayoutSub2.orientation = LinearLayout.HORIZONTAL
            //image input sub container 1
            val imageIcon = ImageView(applicationContext)
            imageIcon.layoutParams = iconImageParams
            if (dataGrabber.getJSONObject(i).getString("debit").toBigDecimal() == BigDecimal(0)) {
              imageIcon.setImageResource(R.drawable.out)
            } else {
              imageIcon.setImageResource(R.drawable.input)
            }
            containerLinearLayoutSub1.addView(imageIcon)
            //description input sub container 1
            val description = TextView(applicationContext)
            description.layoutParams = descriptionParams
            val textDescription = if (dataGrabber.getJSONObject(i).getString("debit").toBigDecimal() == BigDecimal(0)) {
              "${
                dataGrabber.getJSONObject(i).getString("wallet")
              } Send: ${
                bitCoinFormat.decimalToDoge(dataGrabber.getJSONObject(i).getString("credit").toBigDecimal()).toPlainString()
              } DOGE Upgrade LOT: ${
                dataGrabber.getJSONObject(i).getInt("lot")
              } To : ${
                dataGrabber.getJSONObject(i).getString("walletTarget")
              }"
            } else {
              "${
                dataGrabber.getJSONObject(i).getString("wallet")
              } Received: ${
                bitCoinFormat.decimalToDoge(dataGrabber.getJSONObject(i).getString("debit").toBigDecimal()).toPlainString()
              } DOGE Upgrade LOT: ${
                dataGrabber.getJSONObject(i).getInt("lot")
              } From : ${
                dataGrabber.getJSONObject(i).getString("walletTarget")
              }"
            }
            description.text = textDescription
            description.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            description.gravity = Gravity.START
            containerLinearLayoutSub1.addView(description)
            //pin sub container 2
            val doge = TextView(applicationContext)
            doge.layoutParams = totalDogeParams
            if (dataGrabber.getJSONObject(i).getString("debit").toBigDecimal() == BigDecimal(0)) {
              doge.text = "Amount: -${bitCoinFormat.decimalToDoge(dataGrabber.getJSONObject(i).getString("credit").toBigDecimal()).toPlainString()} DOGE"
              doge.setTextColor(ContextCompat.getColor(applicationContext, R.color.Danger))
            } else {
              doge.text = "Amount: +${bitCoinFormat.decimalToDoge(dataGrabber.getJSONObject(i).getString("debit").toBigDecimal()).toPlainString()} DOGE"
              doge.setTextColor(ContextCompat.getColor(applicationContext, R.color.Success))
            }
            doge.gravity = Gravity.CENTER
            containerLinearLayoutSub2.addView(doge)
            //date input sub container 2
            val date = TextView(applicationContext)
            date.layoutParams = dateParams
            date.text = dataGrabber.getJSONObject(i).getString("date")
            date.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            date.gravity = Gravity.CENTER
            containerLinearLayoutSub2.addView(date)
            //set sub container input main container
            containerLinearLayout.addView(containerLinearLayoutSub1)
            containerLinearLayout.addView(containerLinearLayoutSub2)
            //set container to parent container
            container.addView(containerLinearLayout)
            val wrapLine = View(applicationContext)
            wrapLine.layoutParams = line
            wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            container.addView(wrapLine)
          }
        }
        loading.closeDialog()
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }
}