package com.example.lostandfound.utility

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferenceManager @Inject constructor(@ApplicationContext val context: Context) {
    private var mPreferences: SharedPreferences = context.getSharedPreferences(
        Constant.AUTH,
        AppCompatActivity.MODE_PRIVATE
    )
    private var editor: SharedPreferences.Editor = mPreferences.edit()
}