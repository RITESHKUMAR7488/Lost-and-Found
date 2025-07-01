package com.example.lostandfound.mainModule.uis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
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

        private const val IMAGE_UPLOAD_API_KEY = "6d207e02198a847aa98d0a2a901485a5"
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

                selectedImageFile = bitmapToFile(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_report_missing_item)

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
    }

    private fun setupUI() {
        binding.tvCommunityName.text = community?.communityName
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnSelectImage.setOnClickListener {
            showImagePickerDialog()
        }
        binding.btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            openCamera()
                        } else {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CAMERA),
                                CAMERA_PERMISSION_CODE
                            )
                        }
                    }
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.READ_MEDIA_IMAGES
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                openGallery()
                            } else {
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                                    STORAGE_PERMISSION_CODE
                                )
                            }
                        } else {
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                openGallery()
                            } else {
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    STORAGE_PERMISSION_CODE
                                )
                            }
                        }
                    }
                }
            }
            .show()
    }


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                motionToastUtil.showFailureToast(this, "Camera permission denied")
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                motionToastUtil.showFailureToast(this, "Storage permission denied")
            }
        }
    }

    private fun submitReport() {
        val itemName = binding.etItemName.text?.toString()?.trim()
        val description = binding.etDescription.text?.toString()?.trim()
        val reporterName = binding.etReporterName.text?.toString()?.trim()
        val mobileNumber = binding.etMobileNumber.text?.toString()?.trim()
        val locationFound = binding.etLocationFound.text?.toString()?.trim()

        if (itemName.isNullOrEmpty() || reporterName.isNullOrEmpty() ||
            mobileNumber.isNullOrEmpty() || locationFound.isNullOrEmpty()
        ) {
            motionToastUtil.showFailureToast(this, "Please fill all required fields")
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        if (selectedImageFile != null) {
            mainViewModel.uploadImage(selectedImageFile!!, IMAGE_UPLOAD_API_KEY)
                .observe(this) { state ->
                    when (state) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            val imageUrl = state.data.image?.url
                            createMissingItem(
                                itemName,
                                description,
                                reporterName,
                                mobileNumber,
                                locationFound,
                                imageUrl
                            )
                        }
                        is UiState.Failure -> {
                            binding.progressBar.visibility = View.GONE
                            motionToastUtil.showFailureToast(
                                this,
                                "Image upload failed: ${state.error}"
                            )
                        }
                    }
                }
        } else {
            createMissingItem(
                itemName,
                description,
                reporterName,
                mobileNumber,
                locationFound,
                null
            )
        }
    }

    private fun createMissingItem(
        itemName: String,
        description: String?,
        reporterName: String,
        mobileNumber: String,
        locationFound: String,
        imageUrl: String?
    ) {
        val missingItem = MissingItemModel(
            communityId = community?.communityId,
            userId = getCurrentUserId(),
            itemName = itemName,
            description = description,
            imageUrl = imageUrl,
            reporterName = reporterName,
            mobileNumber = mobileNumber,
            locationFound = locationFound,
            reportedAt = System.currentTimeMillis(),
            status = "active"
        )

        mainViewModel.reportMissingItem(missingItem)
            .observe(this) { state ->
                when (state) {
                    is UiState.Loading -> {}
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        motionToastUtil.showSuccessToast(
                            this,
                            "Missing item reported successfully!"
                        )
                        finish()
                    }
                    is UiState.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        motionToastUtil.showFailureToast(
                            this,
                            "Failed: ${state.error}"
                        )
                    }
                }
            }
    }

    private fun uriToFile(uri: Uri): File {
        val filePath = getRealPathFromURI(uri)
        return File(filePath)
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        var result: String? = null
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                if (idx >= 0) result = cursor.getString(idx)
            }
            cursor.close()
        }
        return result ?: ""
    }

    private fun bitmapToFile(bitmap: android.graphics.Bitmap): File {
        val file = File(cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
        val out = file.outputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
        out.close()
        return file
    }

    private fun getCurrentUserId(): String {
        // ideally replace with FirebaseAuth or SharedPref
        return preferenceManager.userId ?: ""

    }
}
