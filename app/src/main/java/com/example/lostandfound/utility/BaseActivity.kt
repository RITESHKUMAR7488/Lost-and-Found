package com.example.lostandfound.utility

import javax.inject.Inject

open class BaseActivity {


    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var motionToastUtil: MotionToastUtil
}