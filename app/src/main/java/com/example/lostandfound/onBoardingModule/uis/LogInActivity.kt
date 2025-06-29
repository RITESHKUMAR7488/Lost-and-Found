package com.example.lostandfound.onBoardingModule.uis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.MainActivity
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityLogInBinding
import com.example.lostandfound.onBoardingModule.viewModels.OnBoardingViewModel
import com.example.lostandfound.utility.BaseActivity
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast

@AndroidEntryPoint
class LogInActivity : BaseActivity() {
    private lateinit var binding: ActivityLogInBinding
    private val onBoardingViewModel: OnBoardingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=DataBindingUtil.setContentView(this,R.layout.activity_log_in)
        with(binding){
            btnLogIn.setOnClickListener{
                login()
            }

        }

    }
    private fun login(){
        with(binding){
            when{
                etEmail.text.toString().isBlank() ->{
                    etEmail.error="Please enter Email"

                }
                etPassword.text.toString().isBlank()->{
                    etPassword.error="Please enter Password"
                }
                else->{
                    val email=etEmail.text.toString().trim()
                    val password=etPassword.text.toString().trim()
                    onBoardingViewModel.login(this@LogInActivity,email,password)
                    onBoardingViewModel.login.observe(this@LogInActivity){state ->
                        Log.d("onBordingLogin",state.toString())
                        when(state){

                            is UiState.Loading ->{
                                binding.progressBar.visibility = View.VISIBLE
                                binding.txt.visibility = View.GONE
                            }
                            is UiState.Success ->{
                                binding.progressBar.visibility = View.GONE
                                binding.txt.visibility = View.VISIBLE
                                preferenceManager.isLoggedIn=true
                                startActivity(Intent(this@LogInActivity,MainActivity::class.java))
                                finish()
                            }
                            is UiState.Failure -> {
                                binding.progressBar.visibility = View.GONE
                                binding.txt.visibility = View.VISIBLE
                                motionToastUtil.showFailureToast(this@LogInActivity,state.error, duration = MotionToast.SHORT_DURATION)
                                Log.e("Login", "Login failed: ${state.error}")
                            }
                        }

                    }

                }

            }


        }

    }
}