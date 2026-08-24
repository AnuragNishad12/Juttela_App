package com.example.juttela.ViewModels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.Repository.SubscriptionRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel : ViewModel() {

    private val repository = SubscriptionRepository()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing

    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro

    init {
        checkProStatus()
    }

    fun checkProStatus() {
        repository.checkProStatus { isPro ->
            _isPro.value = isPro
        }
    }

    fun loadOfferings() {
        _error.value = null
        repository.getOfferings(
            onSuccess = { offerings ->
                _offerings.value = offerings
            },
            onError = { message ->
                _error.value = message
            }
        )
    }

    fun purchasePackage(activity: Activity, packageToPurchase: Package) {
        _isPurchasing.value = true
        _error.value = null

        repository.purchasePackage(
            activity = activity,
            packageToPurchase = packageToPurchase,
            onSuccess = { customerInfo: CustomerInfo ->
                _isPurchasing.value = false
                _purchaseSuccess.value = true
                _isPro.value = isProFrom(customerInfo)
            },
            onError = { message ->
                _isPurchasing.value = false
                _error.value = message
            }
        )
    }

    fun resetPurchaseSuccess() {
        _purchaseSuccess.value = false
    }

    private fun isProFrom(customerInfo: CustomerInfo): Boolean {
        val entitlements = customerInfo.entitlements
        return entitlements["pro"]?.isActive == true ||
                entitlements["Juttela Pro"]?.isActive == true ||
                entitlements.active.isNotEmpty()
    }
}