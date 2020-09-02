package com.consultantvendor.di

import android.content.Context
import android.content.res.Resources
import com.consultantvendor.utils.LocaleHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceProvider @Inject constructor(private val context: Context) {

    fun getStringResource(stringId: Int): String {
        return LocaleHelper.onCreate(context).getString(stringId)
    }

    fun getResources(): Resources {
        return context.resources
    }
}