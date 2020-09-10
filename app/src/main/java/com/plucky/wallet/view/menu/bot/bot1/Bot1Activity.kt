package com.plucky.wallet.view.menu.bot.bot1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.plucky.wallet.MainActivity
import com.plucky.wallet.R
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User

class Bot1Activity : AppCompatActivity() {
  private lateinit var balance: TextView
  private lateinit var username: TextView
  private lateinit var progressBar: ProgressBar
  private lateinit var user: User
  private lateinit var setting: Setting

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot1)

    user = User(this)
    setting = Setting(this)

    setContentView(R.layout.activity_bot1)
    balance = findViewById(R.id.textViewBalance)
    username = findViewById(R.id.textViewUsername)
    progressBar = findViewById(R.id.progressBar)

    username.text = user.getString("username")

    Thread() {
      var time = System.currentTimeMillis()
      var i = 0
      while (i in 0..10) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 1000) {
          time = System.currentTimeMillis()
          if (i == 100) {
            progressBar.progress = 100
            break
          } else {
            progressBar.progress = i
          }
          i++
        }
      }
      runOnUiThread {
        progressBar.visibility = ProgressBar.GONE
        try {
          balance.text = intent.getStringExtra("profit")
        } catch (e: Exception) {
          balance.text = e.message
        }
      }
    }.start()
  }

  override fun onBackPressed() {
    val goTo = Intent(this, MainActivity::class.java)
    startActivity(goTo)
    finishAffinity()
  }
}