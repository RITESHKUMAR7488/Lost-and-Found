package com.example.lostandfound.mainModule.uis

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityMissingItemDetailsBinding
import com.example.lostandfound.mainModule.models.MissingItemModel
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.utility.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MissingItemDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityMissingItemDetailsBinding
    private var missingItem: MissingItemModel? = null
    private var community: UserCommunityModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_missing_item_details)

        // Get data from intent
        missingItem = intent.getParcelableExtra("missingItem")
        community = intent.getParcelableExtra("community")

        if (missingItem == null) {
            motionToastUtil.showFailureToast(
                this,
                "Missing item data not found",
                duration = MotionToast.SHORT_DURATION
            )
            finish()
            return
        }

        setupUI()
        setupClickListeners()
        displayMissingItemDetails()
    }

    private fun setupUI() {
        binding.tvTitle.text = "Item Details"

        // Setup status chip color based on status
        setupStatusChip()
    }

    private fun setupStatusChip() {
        val status = missingItem?.status ?: "active"
        binding.chipStatus.text = status.uppercase()

        when (status.lowercase()) {
            "active" -> {
                binding.chipStatus.setBackgroundColor(getColor(R.color.status_active))
            }
            "found" -> {
                binding.chipStatus.setBackgroundColor(getColor(R.color.status_found))
            }
            "closed" -> {
                binding.chipStatus.setBackgroundColor(getColor(R.color.status_closed))
            }
            else -> {
                binding.chipStatus.setBackgroundColor(getColor(R.color.status_default))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.ivItemImage.setOnClickListener {
            // Simple image click - could expand to full screen later if needed
            motionToastUtil.showInfoToast(
                this,
                "Item image",
                duration = MotionToast.SHORT_DURATION
            )
        }
    }

    private fun displayMissingItemDetails() {
        missingItem?.let { item ->
            // Basic item information
            binding.tvItemName.text = item.itemName ?: "Unknown Item"
            binding.tvDescription.text = item.description ?: "No description available"
            binding.tvReporterName.text = item.reporterName ?: "Anonymous"
            binding.tvLocation.text = item.locationFound ?: "Location not specified"

            // Phone number
            binding.tvPhoneNumber.text = item.mobileNumber ?: "Not provided"

            // Reported date
            val reportedAt = item.reportedAt
            if (reportedAt != null && reportedAt > 0) {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                val date = Date(reportedAt)
                binding.tvReportedDate.text = dateFormat.format(date)

                // Calculate time ago
                val timeAgo = getTimeAgo(reportedAt)
                binding.tvTimeAgo.text = timeAgo
            } else {
                binding.tvReportedDate.text = "Date not available"
                binding.tvTimeAgo.text = ""
            }

            // Community information
            binding.tvCommunityName.text = community?.communityName ?: "Unknown Community"

            // Load item image
            loadItemImage(item.imageUrl)

            // Item ID for reference
            binding.tvItemId.text = "ID: ${item.itemId?.takeLast(8) ?: "Unknown"}"
        }
    }

    private fun loadItemImage(imageUrl: String?) {
        if (!imageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_error)
                .into(binding.ivItemImage)

            binding.ivItemImage.visibility = View.VISIBLE
            binding.tvNoImage.visibility = View.GONE
        } else {
            binding.ivItemImage.visibility = View.GONE
            binding.tvNoImage.visibility = View.VISIBLE
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            years > 0 -> "${years} year${if (years > 1) "s" else ""} ago"
            months > 0 -> "${months} month${if (months > 1) "s" else ""} ago"
            weeks > 0 -> "${weeks} week${if (weeks > 1) "s" else ""} ago"
            days > 0 -> "${days} day${if (days > 1) "s" else ""} ago"
            hours > 0 -> "${hours} hour${if (hours > 1) "s" else ""} ago"
            minutes > 0 -> "${minutes} minute${if (minutes > 1) "s" else ""} ago"
            else -> "Just now"
        }
    }
}