package com.example.juttela.Repository

import android.app.Activity
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith

class SubscriptionRepository {

    fun getOfferings(
        onSuccess: (Offerings) -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                onError(error.message)
            },
            onSuccess = { offerings ->
                onSuccess(offerings)
            }
        )
    }

    fun purchasePackage(
        activity: Activity,
        packageToPurchase: Package,
        onSuccess: (CustomerInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.purchaseWith(
            PurchaseParams.Builder(activity, packageToPurchase).build(),
            onError = { error, userCancelled ->
                if (!userCancelled) {
                    onError(error.message)
                }
            },
            onSuccess = { _, customerInfo ->
                onSuccess(customerInfo)
            }
        )
    }

    fun checkProStatus(
        onResult: (isPro: Boolean) -> Unit
    ) {
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { _ ->
                onResult(false)
            },
            onSuccess = { customerInfo ->
                val isPro = customerInfo.entitlements["Juttela Pro"]?.isActive == true
                onResult(isPro)
            }
        )
    }
}