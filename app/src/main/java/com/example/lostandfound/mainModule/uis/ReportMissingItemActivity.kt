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
import com.example.lostandfound.utility.Constant
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

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Image")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(galleryIntent)
        }
    }

    private fun submitReport() {
        with(binding) {
            val itemName = etItemName.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val reporterName = etReporterName.text.toString().trim()
            val mobileNumber = etMobileNumber.text.toString().trim()
            val locationFound = etLocationFound.text.toString().trim()

            // Validation
            if (itemName.isEmpty()) {
                etItemName.error = "Item name is required"
                etItemName.requestFocus()
                return
            }

            if (description.isEmpty()) {
                etDescription.error = "Description is required"
                etDescription.requestFocus()
                return
            }

            if (reporterName.isEmpty()) {
                etReporterName.error = "Your name is required"
                etReporterName.requestFocus()
                return
            }

            if (mobileNumber.isEmpty()) {
                etMobileNumber.error = "Mobile number is required"
                etMobileNumber.requestFocus()
                return
            }

            if (locationFound.isEmpty()) {
                etLocationFound.error = "Location is required"
                etLocationFound.requestFocus()
                return
            }

            if (selectedImageFile == null) {
                motionToastUtil.showWarningToast(
                    this@ReportMissingItemActivity,
                    "Please select an image of the missing item",
                    duration = MotionToast.SHORT_DURATION
                )
                return
            }

            // Show loading
            progressBar.visibility = View.VISIBLE
            btnSubmitReport.isEnabled = false

            // First upload image, then create report
            mainViewModel.uploadImage(
                selectedImageFile!!,
                "YOUR_IMAGE_UPLOAD_API_KEY" // Replace with your actual API key
            ).observe(this@ReportMissingItemActivity) { response ->
                if (response != null && response.image?.url != null) {
                    // Image uploaded successfully, now create the report
                    createMissingItemReport(
                        itemName, description, reporterName,
                        mobileNumber, locationFound, response.image.url
                    )
                } else {
                    // Image upload failed
                    progressBar.visibility = View.GONE
                    btnSubmitReport.isEnabled = true
                    motionToastUtil.showFailureToast(
                        this@ReportMissingItemActivity,
                        "Failed to upload image",
                        duration = MotionToast.SHORT_DURATION
                    )
                }
            }
        }
    }

    private fun createMissingItemReport(
        itemName: String,
        description: String,
        reporterName: String,
        mobileNumber: String,
        locationFound: String,
        imageUrl: String
    ) {
        val missingItem = MissingItemModel(
            communityId = community?.communityId,
            userId = preferenceManager.userId,
            itemName = itemName,
            description = description,
            imageUrl = imageUrl,
            reporterName = reporterName,
            mobileNumber = mobileNumber,
            locationFound = locationFound,
            reportedAt = System.currentTimeMillis(),
            status = "active"
        )

        mainViewModel.reportMissingItem(missingItem).observe(this) { state ->
            binding.progressBar.visibility = View.GONE
            binding.btnSubmitReport.isEnabled = true

            when (state) {
                is UiState.Loading -> {
                    // Already handled above
                }
                is UiState.Success<*> -> {
                    motionToastUtil.showSuccessToast(
                        this,
                        "Missing item reported successfully!",
                        duration = MotionToast.SHORT_DURATION
                    )
                    finish()
                }
                is UiState.Failure -> {
                    motionToastUtil.showFailureToast(
                        this,
                        state.error,
                        duration = MotionToast.SHORT_DURATION
                    )
                    Log.e("ReportMissingItem", "Failed to report item: ${state.error}")
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe any upload errors
        mainViewModel.uploadError.observe(this) { error ->
            binding.progressBar.visibility = View.GONE
            binding.btnSubmitReport.isEnabled = true
            motionToastUtil.showFailureToast(
                this,
                "Upload failed: ${error.message}",
                duration = MotionToast.SHORT_DURATION
            )
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ReportMissingItem", "Error converting URI to File", e)
            null
        }
    }

    private fun bitmapToFile(bitmap: android.graphics.Bitmap): File? {
        return try {
            val tempFile = File(cacheDir, "temp_camera_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ReportMissingItem", "Error converting Bitmap to File", e)
            null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    motionToastUtil.showFailureToast(
                        this,
                        "Camera permission denied",
                        duration = MotionToast.SHORT_DURATION
                    )
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    motionToastUtil.showFailureToast(
                        this,
                        "Storage permission denied",
                        duration = MotionToast.SHORT_DURATION
                    )
                }
            }
        }
    }
}