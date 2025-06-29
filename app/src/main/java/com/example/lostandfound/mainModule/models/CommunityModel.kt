package com.example.lostandfound.mainModule.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommunityModel(
    var communityId: String? = null,
    var communityName: String? = null,
    var userId: String? = null,
    val communityCode: String?=null,

):Parcelable
