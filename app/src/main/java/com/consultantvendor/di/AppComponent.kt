package com.consultantvendor.di

import com.consultantvendor.ConsultantApplication
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    BindingsModule::class,
    ServiceBuilderModule::class,
    NetworkModule::class,
    ViewModelsModule::class
])
@Singleton
interface AppComponent : AndroidInjector<ConsultantApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<ConsultantApplication>()
}