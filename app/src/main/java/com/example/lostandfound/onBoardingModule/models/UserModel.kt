package com.example.lostandfound.onBoardingModule.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var uid: String? = null,
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var password: String? = null,
    var mobileNumber: String? = null,
    var imageUrl: String? = null,
    var address: String? = null,
) : Parcelable

