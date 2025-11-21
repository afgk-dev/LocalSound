package dev.afgk.localsound

import android.app.Application
import dev.afgk.localsound.di.AppModule
import dev.afgk.localsound.di.AppModuleImpl

class MyApplication : Application() {
    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }
}