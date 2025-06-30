package com.example.lostandfound.mainModule.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserCommunityModel(
    var communityId: String? = null,
    var communityName: String? = null,
    var role: String? = null, // "admin" or "member"
    var joinedAt: Long? = null
) : Parcelable