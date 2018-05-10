package com.ivanasen.smarttickets

import android.app.Application
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.DialogConfigurationBuilder


open class SmartTicketsApplication : Application(), LifecycleOwner {

    private val mLifeCycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    override fun onCreate() {
        super.onCreate()
        mLifeCycleRegistry.markState(Lifecycle.State.CREATED)
        mLifeCycleRegistry.markState(Lifecycle.State.STARTED)
    }

    override fun onTerminate() {
        super.onTerminate()
        mLifeCycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    override fun getLifecycle(): Lifecycle {
        return mLifeCycleRegistry
    }
}