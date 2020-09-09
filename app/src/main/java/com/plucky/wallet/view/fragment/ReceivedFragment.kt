package com.plucky.wallet.view.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.plucky.wallet.R
import com.plucky.wallet.config.Loading
import com.plucky.wallet.model.User
import com.plucky.wallet.view.NavigationActivity

class ReceivedFragment : Fragment() {
  private lateinit var parentActivity: NavigationActivity
  private lateinit var backButton: ImageButton
  private lateinit var clipboardManager: ClipboardManager
  private lateinit var clipData: ClipData
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var imageQR: ImageView
  private lateinit var wallet: TextView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_received, container, false)

    parentActivity = activity as NavigationActivity

    user = User(parentActivity)
    loading = Loading(parentActivity)

    backButton = root.findViewById(R.id.imageButtonBack)
    imageQR = root.findViewById(R.id.imageViewQR)
    wallet = root.findViewById(R.id.textViewWallet)
    val barcodeEncoder = BarcodeEncoder()
    val bitmap = barcodeEncoder.encodeBitmap(user.getString("wallet"), BarcodeFormat.QR_CODE, 500, 500)
    imageQR.setImageBitmap(bitmap)
    wallet.text = user.getString("wallet")

    backButton.setOnClickListener {
      parentActivity.supportFragmentManager.popBackStack()
    }

    wallet.setOnClickListener {
      clipboardManager = parentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
      clipData = ClipData.newPlainText("Wallet", wallet.text.toString())
      clipboardManager.setPrimaryClip(clipData)
      Toast.makeText(parentActivity.applicationContext, "Doge wallet has been copied", Toast.LENGTH_LONG).show()
    }

    return root
  }
}
