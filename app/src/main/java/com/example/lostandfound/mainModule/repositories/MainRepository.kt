package com.example.lostandfound.mainModule.repositories

import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.utility.UiState

interface MainRepository {
    fun createCommunity(userId: String, model: CommunityModel, result: (UiState<CommunityModel>) -> Unit)
    fun joinCommunity(userId: String, communityCode: String, result: (UiState<String>) -> Unit)
}