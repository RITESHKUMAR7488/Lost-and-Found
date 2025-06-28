package com.example.lostandfound.onBoardingModule.di

import com.example.lostandfound.onBoardingModule.repositories.OnBoardingRepository
import com.example.lostandfound.onBoardingModule.repositories.OnBoardingRepositoryImpl
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
class OnBoardingModule {
    @Provides
    @Singleton
    fun provideOnBoardingRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        realtimeDatabase: FirebaseDatabase
    ):OnBoardingRepository{
        return OnBoardingRepositoryImpl(firestore,auth,realtimeDatabase)
    }
}