package com.example.lostandfound.mainModule.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.databinding.CommunityItemBinding
import com.example.lostandfound.mainModule.models.UserCommunityModel

class CommunityAdapter(
    private val onCommunityClick: (UserCommunityModel) -> Unit
) : ListAdapter<UserCommunityModel, CommunityAdapter.CommunityViewHolder>(CommunityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        val binding = CommunityItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommunityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommunityViewHolder(
        private val binding: CommunityItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(community: UserCommunityModel) {
            with(binding) {
                tvCommunityName.text = community.communityName
                tvRole.text = community.role?.uppercase()
                tvJoinedDate.text = formatJoinedDate(community.joinedAt)

                // Set role badge color based on role
                when (community.role) {
                    "admin" -> {
                        tvRole.setBackgroundResource(android.R.color.holo_red_light)
                    }
                    "member" -> {
                        tvRole.setBackgroundResource(android.R.color.holo_blue_light)
                    }
                }

                root.setOnClickListener {
                    onCommunityClick(community)
                }
            }
        }

        private fun formatJoinedDate(timestamp: Long?): String {
            if (timestamp == null) return "Unknown"

            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} minutes ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                diff < 604800000 -> "${diff / 86400000} days ago"
                else -> "${diff / 604800000} weeks ago"
            }
        }
    }

    private class CommunityDiffCallback : DiffUtil.ItemCallback<UserCommunityModel>() {
        override fun areItemsTheSame(oldItem: UserCommunityModel, newItem: UserCommunityModel): Boolean {
            return oldItem.communityId == newItem.communityId
        }

        override fun areContentsTheSame(oldItem: UserCommunityModel, newItem: UserCommunityModel): Boolean {
            return oldItem == newItem
        }
    }
}