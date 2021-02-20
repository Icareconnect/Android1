package com.consultantvendor

import android.app.Application
import com.consultantvendor.data.models.responses.appdetails.AppVersion
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.di.DaggerAppComponent
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.libraries.places.api.Places
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

var appClientDetails = AppVersion()

class ConsultantApplication : DaggerApplication() {

    @Inject
    lateinit var userRepository: UserRepository


    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this)

        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_places_api_key))

        appClientDetails = userRepository.getAppSetting()
        setsApplication(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder().create(this)

    companion object {

        private var isApplication: Application? = null

        fun setsApplication(sApplication: Application) {
            isApplication = sApplication
        }
    }
}