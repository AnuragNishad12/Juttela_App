package com.example.juttela

import android.app.Application
import com.onesignal.OneSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class JutellaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // -------------------------
        // OneSignal
        // -------------------------
        OneSignal.Debug.logLevel = com.onesignal.debug.LogLevel.VERBOSE
        OneSignal.initWithContext(
            this,
            "b93e6dea-3aab-4396-81e8-b77e1559dc65"
        )

        CoroutineScope(Dispatchers.Main).launch {
            OneSignal.Notifications.requestPermission(true)
        }

        // -------------------------
        // RevenueCat
        // -------------------------
        Purchases.logLevel = com.revenuecat.purchases.LogLevel.DEBUG

        Purchases.configure(
            PurchasesConfiguration.Builder(
                this,
                "test_OfmhpOQmVnqZPklDbMnuHztmeeM"
            ).build()
        )
    }
}