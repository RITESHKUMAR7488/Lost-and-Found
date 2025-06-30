package com.example.lostandfound.mainModule.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.mainModule.models.ImageUploadResponse
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.mainModule.repositories.MainRepository
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    fun createCommunity(
        userId: String,
        model: CommunityModel,
    ): LiveData<UiState<CommunityModel>> {
        val successData = MutableLiveData<UiState<CommunityModel>>()
        successData.value = UiState.Loading
        repository.createCommunity(userId, model) {
            successData.value = it
        }
        return successData
    }

    fun joinCommunity(
        userId: String,
        communityCode: String
    ): LiveData<UiState<String>> {
        val successData = MutableLiveData<UiState<String>>()
        successData.value = UiState.Loading
        repository.joinCommunity(userId, communityCode) {
            successData.value = it
        }
        return successData
    }

    fun getUserCommunities(
        userId: String
    ): LiveData<UiState<List<UserCommunityModel>>> {
        val successData = MutableLiveData<UiState<List<UserCommunityModel>>>()
        successData.value = UiState.Loading
        repository.getUserCommunities(userId) {
            successData.value = it
        }
        return successData
    }
    fun uploadImage(
        imageFile: File,
        apiKey: String
    ): LiveData<UiState<ImageUploadResponse>> {
        val successData = MutableLiveData<UiState<ImageUploadResponse>>()
        successData.value = UiState.Loading

        val data = MutableLiveData<ImageUploadResponse>()
        val error = MutableLiveData<Throwable>()

        repository.uploadImage(imageFile, apiKey, data, error)

        data.observeForever { response ->
            response?.let {
                successData.value = UiState.Success(it)  // ✅ Update UiState with success response
            }
        }

        error.observeForever { throwable ->
            throwable?.let {
                successData.value =
                    UiState.Failure(it.message ?: "Unknown error")  // ✅ Handle errors
            }
        }

        return successData
    }
}