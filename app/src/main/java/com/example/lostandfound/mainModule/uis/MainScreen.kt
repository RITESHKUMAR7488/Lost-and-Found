package com.example.lostandfound.mainModule.uis

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lostandfound.R
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.databinding.ActivityMainBinding
import com.example.lostandfound.databinding.ActivityMainScreenBinding
import com.example.lostandfound.mainModule.uis.CreateCommunity

import com.example.lostandfound.utility.BaseActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainScreen : BaseActivity() {
    private lateinit var binding: ActivityMainScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main_screen)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnAdd.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = DataBindingUtil.inflate<com.example.lostandfound.databinding.BottomSheetCommunityOptionsBinding>(
            layoutInflater,
            R.layout.bottom_sheet_community_options,
            null,
            false
        )

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        with(bottomSheetBinding) {
            btnCreateCommunity.setOnClickListener {
                bottomSheetDialog.dismiss()
//                startActivity(Intent(this@MainActivity, CreateCommunity::class.java))
            }

            btnJoinCommunity.setOnClickListener {
                bottomSheetDialog.dismiss()
//                startActivity(Intent(this@MainActivity, JoinCommunity::class.java))
            }
        }

        bottomSheetDialog.show()
    }
}