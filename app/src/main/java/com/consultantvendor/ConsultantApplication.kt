package com.consultantvendor

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.di.DaggerAppComponent
import com.consultantvendor.utils.PrefsManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.libraries.places.api.Places
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject


class ConsultantApplication : DaggerApplication() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager


    override fun onCreate() {
        super.onCreate()

        Fresco.initialize(this)

        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_places_api_key))

        setsApplication(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.builder().create(this)

    companion object {

        var currencyCode = ""

        private var isApplication: Application? = null

        fun setsApplication(sApplication: Application) {
            isApplication = sApplication
        }
    }
}