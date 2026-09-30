package com.example.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.config.GameConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdMobManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialInternet())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // State for showing interstitial overlay
    private val _interstitialVisible = MutableStateFlow(false)
    val interstitialVisible: StateFlow<Boolean> = _interstitialVisible.asStateFlow()
    private var interstitialDismissCallback: (() -> Unit)? = null

    // State for showing rewarded overlay
    private val _rewardedVisible = MutableStateFlow(false)
    val rewardedVisible: StateFlow<Boolean> = _rewardedVisible.asStateFlow()
    private var rewardedRewardCallback: (() -> Unit)? = null

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        registerNetworkObserver()
    }

    private fun checkInitialInternet(): Boolean {
        if (connectivityManager == null) return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun registerNetworkObserver() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            }
            connectivityManager?.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            _isOnline.value = checkInitialInternet()
        }
    }

    /**
     * Checks if ads can be shown (Ads enabled in config + internet available)
     */
    fun areAdsAvailable(): Boolean {
        return GameConfig.ADS_ENABLED && _isOnline.value
    }

    /**
     * Triggers an Interstitial ad if available, otherwise seamlessly proceeds.
     */
    fun showInterstitial(onClosed: () -> Unit) {
        if (!areAdsAvailable()) {
            onClosed()
            return
        }
        interstitialDismissCallback = onClosed
        _interstitialVisible.value = true
    }

    fun dismissInterstitial() {
        _interstitialVisible.value = false
        interstitialDismissCallback?.invoke()
        interstitialDismissCallback = null
    }

    /**
     * Triggers a Rewarded ad. If offline or unavailable, calls onAdUnavailable.
     */
    fun showRewardedAd(
        onRewardGranted: () -> Unit,
        onAdUnavailable: (reason: String) -> Unit
    ) {
        if (!GameConfig.ADS_ENABLED) {
            onAdUnavailable("Ads are currently disabled in game config.")
            return
        }
        if (!_isOnline.value) {
            onAdUnavailable("You are offline! Rewarded ads require an active internet connection.")
            return
        }
        rewardedRewardCallback = onRewardGranted
        _rewardedVisible.value = true
    }

    /**
     * Convenience method to show rewarded ad with offline fallback to never block player.
     */
    fun showRewarded(onRewarded: () -> Unit) {
        showRewardedAd(
            onRewardGranted = onRewarded,
            onAdUnavailable = { onRewarded() }
        )
    }

    fun completeRewardedAd() {
        _rewardedVisible.value = false
        rewardedRewardCallback?.invoke()
        rewardedRewardCallback = null
    }

    fun dismissRewardedAdWithoutReward() {
        _rewardedVisible.value = false
        rewardedRewardCallback = null
    }
}
