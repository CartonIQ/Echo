package com.cartoniq.echo

import android.app.Application
import com.cartoniq.echo.ads.AdManager
import com.cartoniq.echo.audio.EchoService

class EchoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        EchoService.createNotificationChannel(this)
        AdManager.initialize(this)
    }
}
