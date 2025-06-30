package com.example.lostandfound.mainModule.di

import com.example.lostandfound.mainModule.interfaces.ImageUploadApi
import com.example.lostandfound.mainModule.repositories.MainRepository
import com.example.lostandfound.mainModule.repositories.MainRepositoryImpl
import com.example.lostandfound.utility.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class MainModule {
    @Provides
    @Singleton
    fun provideMainRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        imageUploadApi:ImageUploadApi,
        realtimeDatabase: FirebaseDatabase
    ):MainRepository{
        return MainRepositoryImpl(firestore,auth, imageUploadApi,realtimeDatabase)
    }
    @Singleton
    @Provides
    @Named("ImageUploadRetrofit") // ✅ Naming the Retrofit instance for image upload
    fun provideRetroFit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.BASE_URL_IMAGE_UPLOAD)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}