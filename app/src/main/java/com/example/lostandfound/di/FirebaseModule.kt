package com.example.lostandfound.di

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
class FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseFireStoreInstance():FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideRealTimeDataBaseInstance():FirebaseDatabase{
        return FirebaseDatabase.getInstance()
    }
    @Provides
    @Singleton
    fun provideFirebaseFireAuthInstance():FirebaseAuth{
        return FirebaseAuth.getInstance()
    }
}