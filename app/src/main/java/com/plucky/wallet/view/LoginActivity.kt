package com.plucky.wallet.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.plucky.wallet.R
import com.plucky.wallet.config.Loading
import com.plucky.wallet.controller.DogeController
import com.plucky.wallet.controller.WebController
import com.plucky.wallet.model.Doge
import com.plucky.wallet.model.Setting
import com.plucky.wallet.model.User
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class LoginActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var user: User
  private lateinit var setting: Setting
  private lateinit var response: JSONObject
  private lateinit var loading: Loading
  private lateinit var version: TextView
  private lateinit var username: EditText
  private lateinit var password: EditText
  private lateinit var login: Button
  private lateinit var newApp: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    user = User(this)
    setting = Setting(this)
    loading = Loading(this)

    version = findViewById(R.id.textViewVersion)
    username = findViewById(R.id.editTextUsername)
    password = findViewById(R.id.editTextTextPassword)
    login = findViewById(R.id.buttonLogin)
    newApp = findViewById(R.id.buttonNewApp)

    if (intent.getBooleanExtra("isUpdate", false)) {
      login.visibility = Button.GONE
      newApp.visibility = Button.VISIBLE
      version.text = intent.getStringExtra("version")
    } else {
      login.visibility = Button.VISIBLE
      newApp.visibility = Button.GONE
      version.text = intent.getStringExtra("version")
    }

    if (intent.getBooleanExtra("lock", true)) {
      username.isEnabled = false
      password.isEnabled = false
      login.visibility = Button.GONE
      version.text = intent.getStringExtra("version")
    } else {
      username.isEnabled = true
      password.isEnabled = true
      login.visibility = Button.VISIBLE
      version.text = intent.getStringExtra("version")
    }

    username.setText("PLUCKY8")
    password.setText("qwerty")

    login.setOnClickListener {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
          this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
      ) {
        onLogin()
      } else {
        doRequestPermission()
      }
    }

    newApp.setOnClickListener {
      user.clear()
      setting.clear()
      goTo = Intent(Intent.ACTION_VIEW, Uri.parse("https://pluckywin.com/wallet"))
      startActivity(goTo)
      finish()
    }
  }

  private fun onLogin() {
    loading.openDialog()
    when {
      username.text.isEmpty() -> {
        loading.closeDialog()
        Toast.makeText(this, "the whatsapp number or e-mail that you enter cannot be empty", Toast.LENGTH_SHORT).show()
      }
      password.text.isEmpty() -> {
        loading.closeDialog()
        Toast.makeText(this, "password cannot be empty", Toast.LENGTH_SHORT).show()
      }
      else -> {
        Timer().schedule(100) {
          val body = HashMap<String, String>()
          body["username"] = username.text.toString()
          body["password"] = password.text.toString()
          response = WebController.Post("login", "", body).execute().get()
          if (response.getInt("code") == 200) {
            user.setString("token", response.getJSONObject("data").getString("token"))
            user.setString("wallet", response.getJSONObject("data").getString("wallet"))
            user.setString("phone", response.getJSONObject("data").getString("phone"))
            user.setString("username", response.getJSONObject("data").getString("username"))
            user.setString("password", response.getJSONObject("data").getString("password"))
            user.setString("usernameDoge", response.getJSONObject("data").getString("usernameDoge"))
            user.setString("passwordDoge", response.getJSONObject("data").getString("passwordDoge"))
            loginDoge()
          } else {
            runOnUiThread {
              loading.closeDialog()
              Toast.makeText(applicationContext, response.getString("data").replace("phone", "Input"), Toast.LENGTH_LONG).show()
            }
          }
        }
      }
    }
  }

  private fun loginDoge() {
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "Login"
      body["key"] = Doge().key()
      body["username"] = user.getString("usernameDoge")
      body["password"] = user.getString("passwordDoge")
      body["Totp"] = "''"
      Timer().schedule(100) {
        response = DogeController(body).execute().get()
        if (response["code"] == 200) {
          user.setString("key", response.getJSONObject("data")["SessionCookie"].toString())
          goTo = Intent(applicationContext, NavigationActivity::class.java)
          runOnUiThread {
            startActivity(goTo)
            finishAffinity()
            loading.closeDialog()
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_SHORT).show()
            loading.closeDialog()
          }
        }
      }
    }
  }

  private fun doRequestPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
      ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
    ) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 100)
      }
    }
  }
}