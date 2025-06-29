package com.example.lostandfound.utility

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

open class BaseActivity:AppCompatActivity() {


    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var motionToastUtil: MotionToastUtil
}