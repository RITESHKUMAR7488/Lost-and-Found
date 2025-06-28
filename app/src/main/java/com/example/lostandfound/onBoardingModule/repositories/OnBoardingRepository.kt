package com.example.lostandfound.onBoardingModule.repositories

import android.content.Context
import com.example.lostandfound.onBoardingModule.models.UserModel
import com.example.lostandfound.utility.UiState

interface OnBoardingRepository {
    fun register(context: Context,email:String,password:String,userModel: UserModel,result: (UiState<String>)->Unit)
}