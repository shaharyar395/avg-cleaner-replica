package com.replica.cleaner

import android.app.Application
import com.replica.cleaner.ads.AdsInitializer
import com.replica.cleaner.data.CleanerRepository
import com.replica.cleaner.work.Notifications

class CleanerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        // Warm the singleton so the first screen does not block on construction.
        CleanerRepository.get(this)
        AdsInitializer.init(this)
    }
}
