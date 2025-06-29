package com.example.lostandfound.mainModule.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.mainModule.repositories.MainRepository
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository):ViewModel() {
    fun createCommunity(
        userId:String,
        model: CommunityModel,
    ):LiveData<UiState<CommunityModel>>{
        val successData=MutableLiveData<UiState<CommunityModel>>()
        successData.value=UiState.Loading
        repository.createCommunity(userId,model){
            successData.value=it
        }
        return successData
    }
}