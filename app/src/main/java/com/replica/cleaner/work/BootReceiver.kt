package com.replica.cleaner.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.replica.cleaner.data.CleanerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** WorkManager survives reboots on its own, but re-asserting keeps the cadence honest. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val prefs = CleanerRepository.get(context).prefs
                if (prefs.autoCleanEnabled.first()) {
                    AutoCleanWorker.schedule(context, prefs.autoCleanFrequency.first())
                }
            } finally {
                pending.finish()
            }
        }
    }
}
