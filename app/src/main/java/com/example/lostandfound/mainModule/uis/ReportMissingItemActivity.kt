package com.example.lostandfound.mainModule.uis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityReportMissingItemBinding
import com.example.lostandfound.mainModule.models.MissingItemModel
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.mainModule.viewModels.MainViewModel
import com.example.lostandfound.utility.BaseActivity
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast
import java.io.File

@AndroidEntryPoint
class ReportMissingItemActivity : BaseActivity() {
    private lateinit var binding: ActivityReportMissingItemBinding
    private val mainViewModel: MainViewModel by viewModels()
    private var community: UserCommunityModel? = null
    private var selectedImageUri: Uri? = null
    private var selectedImageFile: File? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101

        // ✅ Added: Replace with your actual API key
        private const val IMAGE_UPLOAD_API_KEY = "YOUR_ACTUAL_API_KEY_HERE"
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                binding.ivSelectedImage.visibility = View.VISIBLE
                binding.tvNoImageSelected.visibility = View.GONE
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(binding.ivSelectedImage)

                // Convert URI to File for upload
                selectedImageFile = uriToFile(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? android.graphics.Bitmap
            imageBitmap?.let { bitmap ->
                binding.ivSelectedImage.visibility = View.VISIBLE
                binding.tvNoImageSelected.visibility = View.GONE
                binding.ivSelectedImage.setImageBitmap(bitmap)

                // Convert bitmap to file for upload
                selectedImageFile = bitmapToFile(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_report_missing_item)

        // Get community data from intent
        community = intent.getParcelableExtra("community")

        if (community == null) {
            motionToastUtil.showFailureToast(
                this,
                "Community data not found",
                duration = MotionToast.SHORT_DURATION
            )
            finish()
            return
        }

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvCommunityName.text = community?.communityName
    }

    private fun setupClickListeners() {
        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }

            btnSelectImage.setOnClickListener {
                showImagePickerDialog()
            }

            btnSubmitReport.setOnClickListener {
                submitReport()
            }
        }
    }

    private fun showImage