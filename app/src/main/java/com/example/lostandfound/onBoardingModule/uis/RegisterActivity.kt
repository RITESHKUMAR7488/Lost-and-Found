package com.example.lostandfound.onBoardingModule.uis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityRegisterBinding
import com.example.lostandfound.mainModule.uis.MainScreen
import com.example.lostandfound.onBoardingModule.models.UserModel
import com.example.lostandfound.onBoardingModule.viewModels.OnBoardingViewModel
import com.example.lostandfound.utility.BaseActivity
import com.example.lostandfound.utility.MotionToastUtil
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val onBoardingViewModel: OnBoardingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        with(binding) {
            btnRegister.setOnClickListener {
                validate()
            }

        }

    }

    private fun validate() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val city = binding.etCity.text.toString().trim()

        if (username.isBlank()) {
            binding.etUsername.error = "Please Enter Name"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email"
        } else if (password.length < 8) {
            binding.etPassword.error = "Password must be at least 8 character"
        } else if (confirmPassword != password) {
            binding.etConfirmPassword.error = "Password does not match"
        } else if (mobile.length < 10) {
            binding.etMobile.error = "Enter Valid Number"
        } else {
            val model = UserModel()
            model.email = email
            model.password = password
            model.firstName = username
            model.mobileNumber = mobile
            model.address = city

            onBoardingViewModel.registerUser(this, email, password, model)
            onBoardingViewModel.register.observe(this) {
                Log.d("onboardingStatess", it.toString())
                when (it) {
                    is UiState.Failure -> {
                        motionToastUtil.showFailureToast(
                            this,
                            it.error,
                            duration = MotionToast.SHORT_DURATION
                        )
                        binding.progressBar.visibility = View.GONE
                        binding.textView11.visibility = View.VISIBLE

                    }

                    UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textView11.visibility = View.GONE
                    }

                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.textView11.visibility = View.VISIBLE
                        preferenceManager.isLoggedIn = true
                        motionToastUtil.showSuccessToast(
                            this,
                            "Registration Successfull",
                            duration = MotionToast.SHORT_DURATION
                        )
                        startActivity(Intent(this@RegisterActivity, MainScreen::class.java))
                        finish()


                    }
                }
            }

        }

    }
}