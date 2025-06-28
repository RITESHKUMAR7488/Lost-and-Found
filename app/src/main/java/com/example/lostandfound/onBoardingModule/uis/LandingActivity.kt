package com.example.lostandfound.onBoardingModule.uis

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityLandingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=DataBindingUtil.setContentView(this,R.layout.activity_landing)
        with(binding){
            btnLogIn.setOnClickListener{
                startActivity(Intent(this@LandingActivity, LogInActivity::class.java))
            }
            btnRegister.setOnClickListener{
                startActivity(Intent(this@LandingActivity, RegisterActivity::class.java))
            }
        }
    }
}