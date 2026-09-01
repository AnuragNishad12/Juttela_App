package com.example.juttela.ViewModels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.juttela.Repository.SubscriptionRepository
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val repository: SubscriptionRepository = SubscriptionRepository()
) : ViewModel() {

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _purchaseSuccess = MutableStateFlow(false)
    val purchaseSuccess: StateFlow<Boolean> = _purchaseSuccess.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    fun loadOfferings() {
        _error.value = null
        repository.getOfferings(
            onSuccess = { fetchedOfferings ->
                _offerings.value = fetchedOfferings
            },
            onError = { message ->
                _error.value = message
            }
        )
    }

    fun purchasePackage(activity: Activity, packageToPurchase: Package) {
        if (_isPurchasing.value) return

        _isPurchasing.value = true
        _error.value = null

        repository.purchasePackage(
            activity = activity,
            packageToPurchase = packageToPurchase,
            onSuccess = { customerInfo ->
                _isPurchasing.value = false
                _purchaseSuccess.value = true
                _isPro.value = customerInfo.entitlements["Juttela Pro"]?.isActive == true
            },
            onError = { message ->
                _isPurchasing.value = false
                _error.value = message
            }
        )
    }

    fun checkProStatus() {
        repository.checkProStatus { isPro ->
            _isPro.value = isPro
        }
    }

    fun resetPurchaseState() {
        _purchaseSuccess.value = false
        _error.value = null
    }
}