package com.example.lostandfound.mainModule.uis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityMainScreenBinding
import com.example.lostandfound.mainModule.adapters.CommunityAdapter
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.mainModule.viewModels.MainViewModel
import com.example.lostandfound.utility.BaseActivity
import com.example.lostandfound.utility.UiState
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast

@AndroidEntryPoint
class MainScreen : BaseActivity() {
    private lateinit var binding: ActivityMainScreenBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var communityAdapter: CommunityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_screen)

        setupRecyclerView()
        setupClickListeners()
        loadUserCommunities()
    }

    override fun onResume() {
        super.onResume()
        // Refresh communities when returning to screen
        loadUserCommunities()
    }

    private fun setupRecyclerView() {
        communityAdapter = CommunityAdapter { community ->
            onCommunityClick(community)
        }

        with(binding.recyclerViewCommunities) {
            layoutManager = LinearLayoutManager(this@MainScreen)
            adapter = communityAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAdd.setOnClickListener {
            showBottomSheetDialog()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadUserCommunities()
        }
    }

    private fun loadUserCommunities() {
        val userId = preferenceManager.userId
        if (userId.isNullOrBlank()) {
            motionToastUtil.showFailureToast(
                this,
                "User not logged in",
                duration = MotionToast.SHORT_DURATION
            )
            return
        }

        mainViewModel.getUserCommunities(userId).observe(this) { state ->
            Log.d("MainScreen", "Communities state: $state")
            when (state) {
                is UiState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                is UiState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (state.data.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.recyclerViewCommunities.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerViewCommunities.visibility = View.VISIBLE
                        communityAdapter.submitList(state.data)
                    }
                }
                is UiState.Failure -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    motionToastUtil.showFailureToast(
                        this,
                        state.error,
                        duration = MotionToast.SHORT_DURATION
                    )
                    Log.e("MainScreen", "Failed to load communities: ${state.error}")
                }
            }
        }
    }

    private fun onCommunityClick(community: UserCommunityModel) {
        // ✅ Fixed: Navigate to community details screen
        val intent = Intent(this, CommunityDetailsActivity::class.java)
        intent.putExtra("community", community)
        startActivity(intent)
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = DataBindingUtil.inflate<com.example.lostandfound.databinding.BottomSheetCommunityOptionsBinding>(
            layoutInflater,
            R.layout.bottom_sheet_community_options,
            null,
            false
        )

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        with(bottomSheetBinding) {
            btnCreateCommunity.setOnClickListener {
                bottomSheetDialog.dismiss()
                startActivity(Intent(this@MainScreen, CreateCommunity::class.java))
            }

            btnJoinCommunity.setOnClickListener {
                bottomSheetDialog.dismiss()
                startActivity(Intent(this@MainScreen, JoinCommunity::class.java))
            }
        }

        bottomSheetDialog.show()
    }
}