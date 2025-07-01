package com.example.lostandfound.onBoardingModule.uis

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityLandingBinding
import com.example.lostandfound.mainModule.uis.MainScreen
import com.example.lostandfound.utility.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // check if user is already logged in
        if (preferenceManager.isLoggedIn || preferenceManager.isGmailLoggedIn) {
            // user is already logged in, go directly to MainScreen
            startActivity(Intent(this, MainScreen::class.java))
            finish() // so they cannot come back to landing
            return
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_landing)

        with(binding) {
            btnLogIn.setOnClickListener {
                startActivity(Intent(this@LandingActivity, LogInActivity::class.java))
            }
            btnRegister.setOnClickListener {
                startActivity(Intent(this@LandingActivity, RegisterActivity::class.java))
            }
        }
    }
}
