package com.ivanasen.smarttickets

import android.app.Application
import android.content.Context
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraDialog
import org.acra.annotation.AcraMailSender
import org.acra.data.StringFormat
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.DialogConfigurationBuilder


@AcraCore(buildConfigClass = BuildConfig::class)
@AcraDialog
@AcraMailSender(mailTo = BuildConfig.DEVELOPERS_EMAIL, reportAsFile = false)
open class SmartTicketsApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        val builder = CoreConfigurationBuilder(this)
        builder.setBuildConfigClass(BuildConfig::class.java).setReportFormat(StringFormat.JSON)
        builder.getPluginConfigurationBuilder(DialogConfigurationBuilder::class.java)
                .setResText(R.string.acra_dialog_text)
        ACRA.init(this, builder)

        // The following line triggers the initialization of ACRA
        ACRA.init(this)
    }
}