package com.example.lostandfound.mainModule.repositories

import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.utility.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class MainRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val realtimeDatabase: FirebaseDatabase
) : MainRepository {
    override fun createCommunity(
        userId: String,
        model: CommunityModel,
        result: (UiState<CommunityModel>) -> Unit
    ) {

    }
}