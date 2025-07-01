package com.example.lostandfound.mainModule.repositories

import androidx.lifecycle.MutableLiveData
import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.mainModule.models.ImageUploadResponse
import com.example.lostandfound.mainModule.models.MissingItemModel
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.utility.UiState
import java.io.File

interface MainRepository {
    fun createCommunity(userId: String, model: CommunityModel, result: (UiState<CommunityModel>) -> Unit)
    fun joinCommunity(userId: String, communityCode: String, result: (UiState<String>) -> Unit)
    fun getUserCommunities(userId: String, result: (UiState<List<UserCommunityModel>>) -> Unit)

    // ✅ Fixed: Added missing reportMissingItem method
    fun reportMissingItem(missingItem: MissingItemModel, result: (UiState<MissingItemModel>) -> Unit)

    // ✅ Improved: Better return type for uploadImage
    fun uploadImage(
        imageFile: File,
        apiKey: String,
        result: (UiState<ImageUploadResponse>) -> Unit
    )
    fun getMissingItems(communityId: String, result: (UiState<List<MissingItemModel>>) -> Unit)

}