package com.opentube.util

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi

object PictureInPictureUtil {
    
    /**
     * Check if device supports Picture-in-Picture
     */
    fun isPictureInPictureSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else {
            false
        }
    }
    
    /**
     * Enter Picture-in-Picture mode
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun enterPictureInPicture(context: Context, aspectRatio: Rational = Rational(16, 9)): Boolean {
        val activity = context.findActivity() ?: return false
        
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .build()
        
        return activity.enterPictureInPictureMode(params)
    }
    
    /**
     * Update Picture-in-Picture parameters (for aspect ratio changes)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePictureInPicture(context: Context, aspectRatio: Rational) {
        val activity = context.findActivity() ?: return
        
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .build()
        
        activity.setPictureInPictureParams(params)
    }
    
    /**
     * Check if currently in Picture-in-Picture mode
     */
    fun isInPictureInPictureMode(context: Context): Boolean {
        val activity = context.findActivity() ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.isInPictureInPictureMode
        } else {
            false
        }
    }
    
    /**
     * Find the Activity from a Context
     */
    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}
