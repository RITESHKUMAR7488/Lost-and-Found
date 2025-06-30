package com.example.lostandfound.mainModule.uis

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityJoinCommunityBinding
import com.example.lostandfound.mainModule.viewModels.MainViewModel
import com.example.lostandfound.utility.BaseActivity
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast

@AndroidEntryPoint
class JoinCommunity : BaseActivity() {
    private lateinit var binding: ActivityJoinCommunityBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join_community)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnJoinCommunity.setOnClickListener {
            validateAndJoinCommunity()
        }
    }

    private fun validateAndJoinCommunity() {
        val communityCode = binding.etCommunityCode.text.toString().trim()

        when {
            communityCode.isBlank() -> {
                binding.etCommunityCode.error = "Please enter community code"
            }
            else -> {
                val userId = preferenceManager.userId
                if (userId.isNullOrBlank()) {
                    motionToastUtil.showFailureToast(
                        this,
                        "User not logged in",
                        duration = MotionToast.SHORT_DURATION
                    )
                    return
                }

                joinCommunity(userId, communityCode.uppercase())
            }
        }
    }

    private fun joinCommunity(userId: String, communityCode: String) {
        mainViewModel.joinCommunity(userId, communityCode).observe(this) { state ->
            Log.d("JoinCommunity", "State: $state")
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnJoinCommunity.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnJoinCommunity.isEnabled = true

                    motionToastUtil.showSuccessToast(
                        this,
                        "Successfully joined community!",
                        duration = MotionToast.LONG_DURATION
                    )

                    finish()
                }
                is UiState.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnJoinCommunity.isEnabled = true

                    motionToastUtil.showFailureToast(
                        this,
                        state.error,
                        duration = MotionToast.SHORT_DURATION
                    )
                    Log.e("JoinCommunity", "Failed: ${state.error}")
                }
            }
        }
    }
}