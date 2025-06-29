package com.example.lostandfound.onBoardingModule.repositories

import android.content.Context
import com.example.lostandfound.onBoardingModule.models.UserModel
import com.example.lostandfound.utility.UiState

interface OnBoardingRepository {
    fun register(context: Context,email:String,password:String,userModel: UserModel,result: (UiState<String>)->Unit)
    fun sendUserData(context: Context,userModel: UserModel,result: (UiState<String>) -> Unit)
    fun login(context: Context,email: String,password: String,result: (UiState<String>) -> Unit)
}