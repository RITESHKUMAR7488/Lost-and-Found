package com.example.lostandfound.mainModule.uis

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityCreateCommunityBinding
import com.example.lostandfound.databinding.DialogCommunityCodeBinding
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

                    // Show custom dialog with community code
                    showCommunityCodeDialog(communityModel)
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

    private fun showCommunityCodeDialog(communityModel: CommunityModel) {
        val dialogBinding = DataBindingUtil.inflate<DialogCommunityCodeBinding>(
            LayoutInflater.from(this),
            R.layout.dialog_community_code,
            null,
            false
        )

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // Make dialog background transparent to show custom card background
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        with(dialogBinding) {
            // Set community data
            tvCommunityName.text = communityModel.communityName
            tvCommunityCode.text = communityModel.communityCode

            // Close button click
            btnClose.setOnClickListener {
                dialog.dismiss()
                finish()
            }

            // Copy button click
            btnCopy.setOnClickListener {
                copyToClipboard(communityModel.communityCode ?: "")
            }

            // Done button click
            btnDone.setOnClickListener {
                dialog.dismiss()
                finish()
            }
        }

        dialog.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Community Code", text)
        clipboardManager.setPrimaryClip(clipData)

        motionToastUtil.showSuccessToast(
            this,
            "Community code copied to clipboard!",
            duration = MotionToast.SHORT_DURATION
        )
    }
}