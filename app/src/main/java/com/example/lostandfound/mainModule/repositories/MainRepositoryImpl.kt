package com.example.lostandfound.mainModule.repositories

import android.util.Log
import com.example.lostandfound.mainModule.interfaces.ImageUploadApi
import com.example.lostandfound.mainModule.models.CommunityModel
import com.example.lostandfound.mainModule.models.ImageUploadResponse
import com.example.lostandfound.mainModule.models.MissingItemModel
import com.example.lostandfound.mainModule.models.UserCommunityModel
import com.example.lostandfound.utility.Constant
import com.example.lostandfound.utility.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val imageUploadApi: ImageUploadApi,
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

    override fun getUserCommunities(
        userId: String,
        result: (UiState<List<UserCommunityModel>>) -> Unit
    ) {
        result.invoke(UiState.Loading)

        val userCommunitiesRef = realtimeDatabase.getReference(Constant.MY_COMMUNITIES)
            .child(userId)

        userCommunitiesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val communities = mutableListOf<UserCommunityModel>()

                for (communitySnapshot in snapshot.children) {
                    try {
                        val communityData = communitySnapshot.value as? HashMap<*, *>
                        if (communityData != null) {
                            val community = UserCommunityModel(
                                communityId = communityData["communityId"] as? String,
                                communityName = communityData["communityName"] as? String,
                                role = communityData["role"] as? String,
                                joinedAt = (communityData["joinedAt"] as? Number)?.toLong()
                            )
                            communities.add(community)
                        }
                    } catch (e: Exception) {
                        Log.e("GetUserCommunities", "Error parsing community data", e)
                    }
                }

                // Sort communities by joined date (newest first)
                communities.sortByDescending { it.joinedAt }

                result.invoke(UiState.Success(communities))
                Log.d("GetUserCommunities", "Retrieved ${communities.size} communities")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GetUserCommunities", "Database error", error.toException())
                result.invoke(UiState.Failure(error.message))
            }
        })
    }

    // ✅ Fixed: Added missing reportMissingItem method
    override fun reportMissingItem(
        missingItem: MissingItemModel,
        result: (UiState<MissingItemModel>) -> Unit
    ) {
        result.invoke(UiState.Loading)

        val missingItemsRef = realtimeDatabase.getReference(Constant.MISSING_ITEMS)
        val itemId = missingItem.itemId ?: missingItemsRef.push().key

        if (itemId == null) {
            result.invoke(UiState.Failure("Failed to generate item ID"))
            return
        }

        missingItem.itemId = itemId

        missingItemsRef.child(itemId).setValue(missingItem)
            .addOnSuccessListener {
                Log.d("ReportMissingItem", "Missing item reported successfully")
                result.invoke(UiState.Success(missingItem))
            }
            .addOnFailureListener { exception ->
                Log.e("ReportMissingItem", "Failed to report missing item", exception)
                result.invoke(UiState.Failure(exception.localizedMessage ?: "Failed to report missing item"))
            }
    }

    // ✅ Improved: Better implementation for uploadImage
    override fun uploadImage(
        imageFile: File,
        apiKey: String,
        result: (UiState<ImageUploadResponse>) -> Unit
    ) {
        result.invoke(UiState.Loading)

        // Create a RequestBody for the image file
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("source", imageFile.name, requestBody)

        // Make the API call
        imageUploadApi.uploadImage(apiKey, action = "upload", image = imagePart)
            .enqueue(object : Callback<ImageUploadResponse> {
                override fun onResponse(
                    call: Call<ImageUploadResponse>,
                    response: Response<ImageUploadResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.d("ImageUpload", "Image uploaded successfully: ${response.body()?.image?.url}")
                        result.invoke(UiState.Success(response.body()!!))
                    } else {
                        Log.e("ImageUpload", "Upload failed: ${response.message()}")
                        result.invoke(UiState.Failure("Upload failed: ${response.message()}"))
                    }
                }

                override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                    Log.e("ImageUpload", "Failed to upload image: ${t.message}")
                    result.invoke(UiState.Failure("Failed to upload image: ${t.message}"))
                }
            })
    }
    override fun getMissingItems(
        communityId: String,
        result: (UiState<List<MissingItemModel>>) -> Unit
    ) {
        result.invoke(UiState.Loading)

        val missingItemsRef = realtimeDatabase.getReference(Constant.MISSING_ITEMS)
        missingItemsRef.orderByChild("communityId").equalTo(communityId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val missingItems = mutableListOf<MissingItemModel>()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(MissingItemModel::class.java)
                        if (item != null) {
                            missingItems.add(item)
                        }
                    }
                    result.invoke(UiState.Success(missingItems))
                    Log.d("GetMissingItems", "Retrieved ${missingItems.size} missing items")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("GetMissingItems", "Database error", error.toException())
                    result.invoke(UiState.Failure(error.message))
                }
            })
    }

}