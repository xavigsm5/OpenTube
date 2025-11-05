package com.opentube.util

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi

object PictureInPictureHelper {
    
    fun isPictureInPictureAvailable(activity: Activity): Boolean {
        return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }
    
    fun isInPictureInPictureMode(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.isInPictureInPictureMode
        } else {
            false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun enterPictureInPictureMode(activity: Activity): Boolean {
        if (!isPictureInPictureAvailable(activity)) {
            return false
        }
        
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        
        return activity.enterPictureInPictureMode(params)
    }
}
