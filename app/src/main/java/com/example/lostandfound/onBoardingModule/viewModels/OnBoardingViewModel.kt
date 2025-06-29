package com.example.lostandfound.onBoardingModule.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lostandfound.onBoardingModule.models.UserModel
import com.example.lostandfound.onBoardingModule.repositories.OnBoardingRepository
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class OnBoardingViewModel @Inject constructor(private val repository: OnBoardingRepository):ViewModel() {
     private val _register= MutableLiveData<UiState<String>>()
     val register : LiveData<UiState<String>> =_register

    private val _login=MutableLiveData<UiState<String>>()
    val login:LiveData<UiState<String>> =_login


    fun registerUser(context: Context, email: String, passWord:String,userModel: UserModel){
        _register.value= UiState.Loading
        repository.register(context,email,passWord,userModel){
            _register.value=it
        }
    }
    fun login(context: Context,email: String,passWord: String){
        _login.value=UiState.Loading
        repository.login(context,email,passWord){
            Log.d("somethingReturn",it.toString())
            _login.value=it
        }
    }

}