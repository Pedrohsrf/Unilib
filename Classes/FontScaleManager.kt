package com.example.unilib.Classes

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Log
import java.util.Locale

object FontScaleManager {

    fun applyFontPreset(context: Context, preset: String): Context {
        val res = context.resources
        val conf = Configuration(res.configuration)

        val scale = when (preset) {
            "font70"  -> 0.7f
            "font80"  -> 0.8f
            "font90"  -> 0.9f
            "font100" -> 1.0f
            "font110" -> 1.1f
            "font120" -> 1.2f
            "font130" -> 1.3f
            "font140" -> 1.4f
            "font150" -> 1.5f
            else      -> 1.0f
        }

        conf.fontScale = scale
        conf.setLocale(Locale.getDefault())

        val newContext = context.createConfigurationContext(conf)

        try {
            val id = newContext.resources.getIdentifier("text_16sp", "dimen", context.packageName)
            if (id != 0) {
                val value = newContext.resources.getDimension(id)
                Log.d("FontScaleManager", "applyFontPreset loaded. preset=$preset, text_16sp(px)=$value")
            } else {
                Log.d("FontScaleManager", "applyFontPreset: resource text_16sp not found (ok if you don't have it)")
            }
        } catch (e: Exception) {
            Log.e("FontScaleManager", "applyFontPreset error", e)
        }

        return newContext
    }

    fun updateActivityWithPreset(activity: Activity, preset: String): Context {
        val newContext = applyFontPreset(activity, preset)
        val res: Resources = newContext.resources
        val displayMetrics: DisplayMetrics = res.displayMetrics


        try {
            activity.resources.updateConfiguration(res.configuration, displayMetrics)
        } catch (e: Exception) {
            Log.w("FontScaleManager", "updateActivityWithPreset: updateConfiguration failed (ignored)", e)
        }

        return newContext
    }
}



