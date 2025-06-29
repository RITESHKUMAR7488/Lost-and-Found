package com.example.lostandfound.mainModule.di

import com.example.lostandfound.mainModule.repositories.MainRepository
import com.example.lostandfound.mainModule.repositories.MainRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class MainModule {
    @Provides
    @Singleton
    fun provideMainRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        realtimeDatabase: FirebaseDatabase
    ):MainRepository{
        return MainRepositoryImpl(firestore,auth,realtimeDatabase)
    }
}