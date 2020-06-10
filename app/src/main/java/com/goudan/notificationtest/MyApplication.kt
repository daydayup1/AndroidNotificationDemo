package com.goudan.notificationtest

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        myApplicationContext = applicationContext
    }

    companion object  {
        lateinit var myApplicationContext: Context

        fun getApplication(): Context {
            return myApplicationContext
        }
    }
}