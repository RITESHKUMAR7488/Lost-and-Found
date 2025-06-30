package com.example.lostandfound.mainModule.repositories

import android.util.Log
import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.utility.Constant
import com.example.lostandfound.utility.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
        result.invoke(UiState.Loading)

        val communityRef = realtimeDatabase.getReference(Constant.COMMUNITIES)
        val communityId = model.communityId ?: communityRef.push().key

        if (communityId == null) {
            result.invoke(UiState.Failure("Failed to generate community ID"))
            return
        }

        model.communityId = communityId

        // Create community in realtime database
        communityRef.child(communityId).setValue(model)
            .addOnSuccessListener {
                Log.d("CreateCommunity", "Community created successfully")

                // Add user as admin to the community
                val userCommunityRef = realtimeDatabase.getReference(Constant.MY_COMMUNITIES)
                    .child(userId)
                    .child(communityId)

                val userCommunityData = hashMapOf(
                    "communityId" to communityId,
                    "communityName" to model.communityName,
                    "role" to Constant.ADMIN,
                    "joinedAt" to System.currentTimeMillis()
                )

                userCommunityRef.setValue(userCommunityData)
                    .addOnSuccessListener {
                        Log.d("CreateCommunity", "User added as admin successfully")
                        result.invoke(UiState.Success(model))
                    }
                    .addOnFailureListener { exception ->
                        Log.e("CreateCommunity", "Failed to add user as admin", exception)
                        result.invoke(UiState.Failure(exception.localizedMessage ?: "Failed to add user as admin"))
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("CreateCommunity", "Failed to create community", exception)
                result.invoke(UiState.Failure(exception.localizedMessage ?: "Failed to create community"))
            }
    }

    override fun joinCommunity(
        userId: String,
        communityCode: String,
        result: (UiState<String>) -> Unit
    ) {
        result.invoke(UiState.Loading)

        val communityRef = realtimeDatabase.getReference(Constant.COMMUNITIES)

        // Find community by code
        communityRef.orderByChild("communityCode").equalTo(communityCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val communitySnapshot = snapshot.children.first()
                        val community = communitySnapshot.getValue(CommunityModel::class.java)

                        if (community != null) {
                            val communityId = community.communityId

                            if (communityId != null) {
                                // Check if user is already a member
                                val userCommunityRef = realtimeDatabase.getReference(Constant.MY_COMMUNITIES)
                                    .child(userId)
                                    .child(communityId)

                                userCommunityRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        if (userSnapshot.exists()) {
                                            result.invoke(UiState.Failure("You are already a member of this community"))
                                        } else {
                                            // Add user as member
                                            val userCommunityData = hashMapOf(
                                                "communityId" to communityId,
                                                "communityName" to community.communityName,
                                                "role" to Constant.MEMBER,
                                                "joinedAt" to System.currentTimeMillis()
                                            )

                                            userCommunityRef.setValue(userCommunityData)
                                                .addOnSuccessListener {
                                                    Log.d("JoinCommunity", "User joined community successfully")
                                                    result.invoke(UiState.Success("Successfully joined ${community.communityName}"))
                                                }
                                                .addOnFailureListener { exception ->
                                                    Log.e("JoinCommunity", "Failed to join community", exception)
                                                    result.invoke(UiState.Failure(exception.localizedMessage ?: "Failed to join community"))
                                                }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("JoinCommunity", "Database error", error.toException())
                                        result.invoke(UiState.Failure(error.message))
                                    }
                                })
                            } else {
                                result.invoke(UiState.Failure("Invalid community data"))
                            }
                        } else {
                            result.invoke(UiState.Failure("Community not found"))
                        }
                    } else {
                        result.invoke(UiState.Failure("Invalid community code"))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("JoinCommunity", "Database error", error.toException())
                    result.invoke(UiState.Failure(error.message))
                }
            })
    }
}