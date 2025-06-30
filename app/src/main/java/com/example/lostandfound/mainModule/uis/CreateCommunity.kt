package com.example.lostandfound.mainModule.uis

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityCreateCommunityBinding
import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.mainModule.viewModels.MainViewModel
import com.example.lostandfound.utility.BaseActivity
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast
import java.util.UUID

@AndroidEntryPoint
class CreateCommunity : BaseActivity() {
    private lateinit var binding: ActivityCreateCommunityBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_community)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnCreateCommunity.setOnClickListener {
            validateAndCreateCommunity()
        }
    }

    private fun validateAndCreateCommunity() {
        val communityName = binding.etCommunityName.text.toString().trim()

        when {
            communityName.isBlank() -> {
                binding.etCommunityName.error = "Please enter community name"
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

                val communityModel = CommunityModel(
                    communityId = UUID.randomUUID().toString(),
                    communityName = communityName,
                    userId = userId,
                    communityCode = generateCommunityCode()
                )

                createCommunity(userId, communityModel)
            }
        }
    }

    private fun generateCommunityCode(): String {
        val words = listOf(
            "apple", "brave", "cloud", "dream", "eagle", "flame", "grace", "happy",
            "jolly", "magic", "noble", "ocean", "peace", "quiet", "river", "shine",
            "trust", "unity", "vivid", "water", "youth", "zebra", "amber", "bloom",
            "coral", "dance", "earth", "frost", "giant", "honor", "ivory", "jewel"
        )

        val randomWords = words.shuffled().take(6)
        return randomWords.joinToString("-").uppercase()
    }

    private fun createCommunity(userId: String, communityModel: CommunityModel) {
        mainViewModel.createCommunity(userId, communityModel).observe(this) { state ->
            Log.d("CreateCommunity", "State: $state")
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnCreateCommunity.isEnabled = false
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateCommunity.isEnabled = true

                    motionToastUtil.showSuccessToast(
                        this,
                        "Community created successfully!",
                        duration = MotionToast.LONG_DURATION
                    )

                    // Show the community code to user
                    motionToastUtil.showInfoToast(
                        this,
                        "Community Code: ${communityModel.communityCode}",
                        duration = MotionToast.LONG_DURATION
                    )

                    finish()
                }
                is UiState.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateCommunity.isEnabled = true

                    motionToastUtil.showFailureToast(
                        this,
                        state.error,
                        duration = MotionToast.SHORT_DURATION
                    )
                    Log.e("CreateCommunity", "Failed: ${state.error}")
                }
            }
        }
    }
}