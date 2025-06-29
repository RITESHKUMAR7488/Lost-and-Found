package com.example.lostandfound.mainModule.repositories

import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.utility.UiState
import com.google.firebase.database.FirebaseDatabase

class MainRepositoryImpl(private val realtimeDataBase:FirebaseDatabase):MainRepository {
    override fun createCommunity(
        userId: String,
        model: CommunityModel,
        result: (UiState<CommunityModel>) -> Unit
    ) {

    }
}