package com.example.lostandfound.mainModule.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostandfound.R
import com.example.lostandfound.databinding.MissingItemBinding
import com.example.lostandfound.mainModule.models.MissingItemModel
import java.text.SimpleDateFormat
import java.util.*

class MissingItemsAdapter(
    private val onItemClick: (MissingItemModel) -> Unit
) : ListAdapter<MissingItemModel, MissingItemsAdapter.MissingItemViewHolder>(MissingItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissingItemViewHolder {
        val binding = MissingItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MissingItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MissingItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MissingItemViewHolder(
        private val binding: MissingItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(missingItem: MissingItemModel) {
            with(binding) {
                // Set basic item information
                tvItemName.text = missingItem.itemName
                tvReporterName.text = "Reported by: ${missingItem.reporterName}"
                tvLocationFound.text = "Location: ${missingItem.locationFound}"
                tvReportedDate.text = formatDate(missingItem.reportedAt)

                // Set status
                tvStatus.text = missingItem.status?.uppercase()
                setStatusColor(missingItem.status)

                // Set description (truncated for list view)
                val description = missingItem.description
                if (!description.isNullOrEmpty()) {
                    tvDescription.text = if (description.length > 100) {
                        "${description.substring(0, 100)}..."
                    } else {
                        description
                    }
                } else {
                    tvDescription.text = "No description provided"
                }

                // Load image if available
                if (!missingItem.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(missingItem.imageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .centerCrop()
                        .into(ivItemImage)
                } else {
                    ivItemImage.setImageResource(R.drawable.ic_no_image)
                }

                // Set click listener
                root.setOnClickListener {
                    onItemClick(missingItem)
                }
            }
        }

        private fun setStatusColor(status: String?) {
            when (status?.lowercase()) {
                "active" -> {
                    binding.tvStatus.setBackgroundResource(R.color.status_active)
                }
                "found" -> {
                    binding.tvStatus.setBackgroundResource(R.color.status_found)
                }
                "closed" -> {
                    binding.tvStatus.setBackgroundResource(R.color.status_closed)
                }
                else -> {
                    binding.tvStatus.setBackgroundResource(R.color.status_unknown)
                }
            }
        }

        private fun formatDate(timestamp: Long?): String {
            if (timestamp == null) return "Unknown date"

            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} minutes ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                diff < 604800000 -> "${diff / 86400000} days ago"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }

    private class MissingItemDiffCallback : DiffUtil.ItemCallback<MissingItemModel>() {
        override fun areItemsTheSame(oldItem: MissingItemModel, newItem: MissingItemModel): Boolean {
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: MissingItemModel, newItem: MissingItemModel): Boolean {
            return oldItem == newItem
        }
    }
}