package com.example.lostandfound.onBoardingModule.repositories

import android.content.Context
import android.util.Log
import com.example.lostandfound.onBoardingModule.models.UserModel
import com.example.lostandfound.utility.Constant
import com.example.lostandfound.utility.PreferenceManager
import com.example.lostandfound.utility.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class OnBoardingRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val realtimeDatabase: FirebaseDatabase
) : OnBoardingRepository {

    private lateinit var userId: String

    @Inject
    lateinit var preferenceManager: PreferenceManager


    override fun register(
        context: Context,
        email: String,
        password: String,
        userModel: UserModel,
        result: (UiState<String>) -> Unit
    ) {
        preferenceManager = PreferenceManager(context)
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task->
            if(task.isSuccessful){
                val user=auth.currentUser
                if(user!=null){
                    userId=user.uid
                    preferenceManager.userId=userId

                    sendUserData(context, userModel) { state ->
                        when (state) {
                            is UiState.Success -> {
                                result.invoke(UiState.Success("User registered successfully"))
                            }
                            is UiState.Failure -> {
                                result.invoke(UiState.Failure(state.error))
                            }
                            is UiState.Loading -> {
                                result.invoke(UiState.Loading)
                            }
                        }
                    }


                }else {
                    result.invoke(UiState.Failure("User is null after registration"))
                }
            }

        }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.localizedMessage ?: "Registration failed"))
            }

    }

    override fun sendUserData(context: Context, userModel: UserModel, result: (UiState<String>) -> Unit) {
        Log.d("sendUserData", "Function entered")

        preferenceManager = PreferenceManager(context)
        userId = preferenceManager.userId.toString()

        val document = firestore.collection(Constant.USERS).document(userId)
        userModel.uid = userId

        document.set(userModel).addOnSuccessListener {
            Log.d("sendUserData", "User data saved successfully")
            result.invoke(UiState.Success("Registered successfully"))
        }.addOnFailureListener { exception ->
            Log.e("sendUserData", "Failed to save user data: ${exception.localizedMessage}")
            result.invoke(UiState.Failure(exception.localizedMessage))
        }
    }

    override fun login(
        context: Context,
        email: String,
        password: String,
        result: (UiState<String>) -> Unit
    ) {
        preferenceManager=PreferenceManager(context)
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                result.invoke(UiState.Success("Login Successful"))
                userId=auth.currentUser!!.uid
                preferenceManager.userId=userId
            }
            else{
                result.invoke(UiState.Failure("Authentication failed: ${task.exception?.message}"))
            }
        }

    }
}