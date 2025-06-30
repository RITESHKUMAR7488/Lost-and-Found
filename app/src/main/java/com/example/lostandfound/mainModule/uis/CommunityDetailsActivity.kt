package com.example.lostandfound.mainModule.uis

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityCommunityDetailsBinding
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.utility.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast

@AndroidEntryPoint
class CommunityDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCommunityDetailsBinding
    private var community: UserCommunityModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_community_details)

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
    }

    private fun setupUI() {
        with(binding) {
            tvCommunityName.text = community?.communityName
            tvRole.text = community?.role?.uppercase()

            // Set role badge color
            when (community?.role) {
                "admin" -> {
                    tvRole.setBackgroundResource(android.R.color.holo_red_light)
                }
                "member" -> {
                    tvRole.setBackgroundResource(android.R.color.holo_blue_light)
                }
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            btnReportMissing.setOnClickListener {
                val intent = Intent(this@CommunityDetailsActivity, ReportMissingItemActivity::class.java)
                intent.putExtra("community", community)
                startActivity(intent)
            }

            btnSeeMissingItems.setOnClickListener {
                // TODO: Navigate to see missing items screen
                motionToastUtil.showInfoToast(
                    this@CommunityDetailsActivity,
                    "See Missing Items - Coming Soon",
                    duration = MotionToast.SHORT_DURATION
                )
            }

            btnBack.setOnClickListener {
                finish()
            }
        }
    }
}