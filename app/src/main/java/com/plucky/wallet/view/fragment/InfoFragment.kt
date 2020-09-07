package com.plucky.wallet.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.plucky.wallet.R
import com.plucky.wallet.view.NavigationActivity

class InfoFragment : Fragment() {
  private lateinit var parentActivity: NavigationActivity
  private lateinit var backButton: ImageButton

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_info, container, false)

    parentActivity = activity as NavigationActivity

    backButton = root.findViewById(R.id.imageButtonBack)

    backButton.setOnClickListener {
      parentActivity.supportFragmentManager.popBackStack()
    }

    return root
  }
}
