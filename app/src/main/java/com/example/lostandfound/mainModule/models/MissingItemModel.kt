package com.example.lostandfound.mainModule.models


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MissingItemModel(
    var itemId: String? = null,
    var communityId: String? = null,
    var userId: String? = null,
    var itemName: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var reporterName: String? = null,
    var mobileNumber: String? = null,
    var locationFound: String? = null,
    var reportedAt: Long? = null,
    var status: String? = "active" // active, found, closed
) : Parcelable
