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
import com.example.lostandfound.databinding.ActivityMissingItemsListBinding
import com.example.lostandfound.mainModule.adapters.MissingItemsAdapter
import com.example.lostandfound.mainModule.models.MissingItemModel
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.mainModule.viewModels.MainViewModel
import com.example.lostandfound.utility.BaseActivity
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.AndroidEntryPoint
import www.sanju.motiontoast.MotionToast

@AndroidEntryPoint
class MissingItemsListActivity : BaseActivity() {

    private lateinit var binding: ActivityMissingItemsListBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var missingItemsAdapter: MissingItemsAdapter
    private var community: UserCommunityModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_missing_items_list)

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
        setupRecyclerView()
        setupClickListeners()
        loadMissingItems()
    }

    override fun onResume() {
        super.onResume()
        // Refresh missing items when returning to screen
        loadMissingItems()
    }

    private fun setupUI() {
        binding.tvCommunityName.text = community?.communityName
        binding.tvTitle.text = "Missing Items"
    }

    private fun setupRecyclerView() {
        missingItemsAdapter = MissingItemsAdapter { missingItem ->
            onMissingItemClick(missingItem)
        }

        with(binding.recyclerViewMissingItems) {
            layoutManager = LinearLayoutManager(this@MissingItemsListActivity)
            adapter = missingItemsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabAddMissingItem.setOnClickListener {
            val intent = Intent(this, ReportMissingItemActivity::class.java)
            intent.putExtra("community", community)
            startActivity(intent)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadMissingItems()
        }
    }

    private fun loadMissingItems() {
        val communityId = community?.communityId
        if (communityId.isNullOrBlank()) {
            motionToastUtil.showFailureToast(
                this,
                "Invalid community ID",
                duration = MotionToast.SHORT_DURATION
            )
            return
        }

        mainViewModel.getMissingItems(communityId).observe(this) { state ->
            Log.d("MissingItemsList", "Missing items state: $state")
            when (state) {
                is UiState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }
                is UiState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    if (state.data.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.recyclerViewMissingItems.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerViewMissingItems.visibility = View.VISIBLE
                        missingItemsAdapter.submitList(state.data)
                    }
                }
                is UiState.Failure -> {
                    binding.swipeRefreshLayout.isRefreshing = false

                    motionToastUtil.showFailureToast(
                        this,
                        state.error,
                        duration = MotionToast.SHORT_DURATION
                    )
                    Log.e("MissingItemsList", "Failed to load missing items: ${state.error}")
                }

                else -> {}
            }
        }
    }

    private fun onMissingItemClick(missingItem: MissingItemModel) {
        val intent = Intent(this, MissingItemDetailsActivity::class.java)
        intent.putExtra("missingItem", missingItem)
        intent.putExtra("community", community)
        startActivity(intent)
    }
}