package com.plucky.wallet.view.menu

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.plucky.wallet.R
import com.plucky.wallet.config.BitCoinFormat
import com.plucky.wallet.config.Loading
import com.plucky.wallet.controller.DogeController
import com.plucky.wallet.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HistoryOutActivity : AppCompatActivity() {
  private lateinit var containerExternal: LinearLayout
  private lateinit var containerInternal: LinearLayout
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat
  private lateinit var previewButton: Button
  private lateinit var nextButton: Button
  private lateinit var tokenList: ArrayList<String>
  private lateinit var globalToken: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history_out)

    tokenList = ArrayList()
    user = User(this)
    loading = Loading(this)
    bitCoinFormat = BitCoinFormat()

    previewButton = findViewById(R.id.buttonPreview)
    nextButton = findViewById(R.id.buttonNext)
    containerExternal = findViewById(R.id.linearLayoutDataContentExternal)
    containerExternal.removeAllViews()
    containerInternal = findViewById(R.id.linearLayoutDataContent)
    containerInternal.removeAllViews()

    previewButton.isEnabled = false
    nextButton.isEnabled = false

    previewButton.setOnClickListener {
      containerExternal.removeAllViews()
      containerInternal.removeAllViews()
      tokenList.removeAt(tokenList.size - 1)
      if (tokenList.size > 1) {
        setView(tokenList[tokenList.size - 1])
      } else {
        setView("")
      }
    }

    nextButton.setOnClickListener {
      containerExternal.removeAllViews()
      containerInternal.removeAllViews()
      tokenList.add(globalToken)
      if (tokenList.isNotEmpty()) {
        setView(tokenList[tokenList.size - 1])
      } else {
        setView("")
      }
    }

    setView("")
  }

  private fun setView(tokenDoge: String) {
    loading.openDialog()
    Timer().schedule(1000) {
      val body = HashMap<String, String>()
      body["a"] = "GetWithdrawals"
      body["s"] = user.getString("key")
      body["Token"] = tokenDoge
      response = DogeController(body).execute().get()
      if (response.getInt("code") == 200) {
        globalToken = response.getJSONObject("data").getString("Token")
        val dataGrabberExternal = response.getJSONObject("data").getJSONArray("Withdrawals")
        val dataGrabberInternal = response.getJSONObject("data").getJSONArray("Transfers")
        val lengthExternal = dataGrabberExternal.length() - 1
        val lengthInternal = dataGrabberInternal.length() - 1

        runOnUiThread {
          previewButton.isEnabled = tokenList.isNotEmpty()
          nextButton.isEnabled = !(lengthExternal <= 0 && lengthInternal <= 0)

          setExternalView(lengthExternal, dataGrabberExternal)
          setInternalView(lengthInternal, dataGrabberInternal)

          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response.getString("data"), Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun setInternalView(length: Int, dataGrabberInternal: JSONArray) {
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val iconImageParams = LinearLayout.LayoutParams(50, 50)
    val addressParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val balanceParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val dateParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)
    for (i in length downTo 0) {
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
        imageIcon.setImageResource(R.drawable.out)
        containerLinearLayoutSub1.addView(imageIcon)
        //description input sub container 1
        val address = TextView(applicationContext)
        address.layoutParams = addressParams
        address.text = dataGrabberInternal.getJSONObject(i).getString("Address").replace("XFER", "Internal EARN")
        address.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
        address.gravity = Gravity.START
        containerLinearLayoutSub1.addView(address)
        //pin sub container 2
        val balance = TextView(applicationContext)
        balance.layoutParams = balanceParams
        balance.text = "Amount: -${bitCoinFormat.decimalToDoge(dataGrabberInternal.getJSONObject(i).getString("Value").toBigDecimal()).toPlainString()} DOGE"
        balance.setTextColor(ContextCompat.getColor(applicationContext, R.color.Danger))
        balance.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(balance)
        //date input sub container 2
        val date = TextView(applicationContext)
        date.layoutParams = dateParams
        date.text = dataGrabberInternal.getJSONObject(i).getString("Completed")
        date.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
        date.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(date)
        //set sub container input main container
        containerLinearLayout.addView(containerLinearLayoutSub1)
        containerLinearLayout.addView(containerLinearLayoutSub2)
        //set container to parent container
        containerInternal.addView(containerLinearLayout)
        val wrapLine = View(applicationContext)
        wrapLine.layoutParams = line
        wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
        containerInternal.addView(wrapLine)
      }
    }
  }

  private fun setExternalView(length: Int, dataGrabberInternal: JSONArray) {
    val linearLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayoutParams.setMargins(10, 10, 10, 10)
    val iconImageParams = LinearLayout.LayoutParams(50, 50)
    val addressParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val balanceParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val dateParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
    val line = LinearLayout.LayoutParams(10, LinearLayout.LayoutParams.WRAP_CONTENT)
    for (i in length downTo 0) {
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
        imageIcon.setImageResource(R.drawable.out)
        containerLinearLayoutSub1.addView(imageIcon)
        //description input sub container 1
        val address = TextView(applicationContext)
        address.layoutParams = addressParams
        address.text = dataGrabberInternal.getJSONObject(i).getString("Address") + " | " + dataGrabberInternal.getJSONObject(i).getString("TransactionHash")
        address.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
        address.gravity = Gravity.START
        address.setOnClickListener {
          val uri = "https://dogechain.info/tx/${dataGrabberInternal.getJSONObject(i).getString("TransactionHash")}"
          val goTo = Intent(Intent.ACTION_VIEW)
          goTo.data = Uri.parse(uri)
          startActivity(goTo)
        }
        containerLinearLayoutSub1.addView(address)
        //pin sub container 2
        val balance = TextView(applicationContext)
        balance.layoutParams = balanceParams
        balance.text = "Amount: -${
          bitCoinFormat.decimalToDoge(dataGrabberInternal.getJSONObject(i).getString("Value").toBigDecimal()).toPlainString()
        } DOGE | Fee: -${
          bitCoinFormat.decimalToDoge(dataGrabberInternal.getJSONObject(i).getString("Fee").toBigDecimal()).toPlainString()
        } DOGE"
        balance.setTextColor(ContextCompat.getColor(applicationContext, R.color.Danger))
        balance.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(balance)
        //date input sub container 2
        val date = TextView(applicationContext)
        date.layoutParams = dateParams
        date.text = dataGrabberInternal.getJSONObject(i).getString("Completed")
        date.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
        date.gravity = Gravity.CENTER
        containerLinearLayoutSub2.addView(date)
        //set sub container input main container
        containerLinearLayout.addView(containerLinearLayoutSub1)
        containerLinearLayout.addView(containerLinearLayoutSub2)
        //set container to parent container
        containerExternal.addView(containerLinearLayout)
        val wrapLine = View(applicationContext)
        wrapLine.layoutParams = line
        wrapLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
        containerExternal.addView(wrapLine)
      }
    }
  }
}