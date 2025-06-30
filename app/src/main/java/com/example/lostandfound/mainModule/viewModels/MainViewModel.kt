package com.example.lostandfound.mainModule.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.mainModule.models.ImageUploadResponse
import com.example.lostandfound.mainModule.models.MissingItemModel
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.mainModule.repositories.MainRepository
import com.example.lostandfound.utility.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {

    // ✅ Added: Error handling LiveData for uploads
    private val _uploadError = MutableLiveData<Throwable>()
    val uploadError: LiveData<Throwable> = _uploadError

    fun createCommunity(
        userId: String,
        model: CommunityModel,
    ): LiveData<UiState<CommunityModel>> {
        val result = MutableLiveData<UiState<CommunityModel>>()
        repository.createCommunity(userId, model) {
            result.value = it
        }
        return result
    }

    fun joinCommunity(
        userId: String,
        communityCode: String
    ): LiveData<UiState<String>> {
        val result = MutableLiveData<UiState<String>>()
        repository.joinCommunity(userId, communityCode) {
            result.value = it
        }
        return result
    }

    fun getUserCommunities(
        userId: String
    ): LiveData<UiState<List<UserCommunityModel>>> {
        val result = MutableLiveData<UiState<List<UserCommunityModel>>>()
        repository.getUserCommunities(userId) {
            result.value = it
        }
        return result
    }

    // ✅ Fixed: Added missing reportMissingItem method
    fun reportMissingItem(
        missingItem: MissingItemModel
    ): LiveData<UiState<MissingItemModel>> {
        val result = MutableLiveData<UiState<MissingItemModel>>()
        repository.reportMissingItem(missingItem) {
            result.value = it
        }
        return result
    }

    // ✅ Improved: Better implementation for uploadImage
    fun uploadImage(
        imageFile: File,
        apiKey: String
    ): LiveData<UiState<ImageUploadResponse>> {
        val result = MutableLiveData<UiState<ImageUploadResponse>>()

        repository.uploadImage(imageFile, apiKey) { state ->
            result.value = state

            // Handle error case for upload error LiveData
            if (state is UiState.Failure) {
                _uploadError.value = Exception(state.error)
            }
        }

        return result
    }
}