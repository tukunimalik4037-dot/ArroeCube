package com.example.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.config.GameConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdMobManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isOnline = MutableStateFlow(checkInitialInternet())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Real Google AdMob Ads
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    // State for showing interstitial overlay or fallback dialog
    private val _interstitialVisible = MutableStateFlow(false)
    val interstitialVisible: StateFlow<Boolean> = _interstitialVisible.asStateFlow()
    private var interstitialDismissCallback: (() -> Unit)? = null

    // State for showing rewarded overlay or fallback dialog
    private val _rewardedVisible = MutableStateFlow(false)
    val rewardedVisible: StateFlow<Boolean> = _rewardedVisible.asStateFlow()
    private var rewardedRewardCallback: (() -> Unit)? = null

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        registerNetworkObserver()
        loadInterstitialAd()
        loadRewardedAd()
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
                    loadInterstitialAd()
                    loadRewardedAd()
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

    fun loadInterstitialAd() {
        mainHandler.post {
            if (!GameConfig.ADS_ENABLED || !_isOnline.value) return@post
            val appContext = context.applicationContext
            val adRequest = AdRequest.Builder().build()
            try {
                InterstitialAd.load(
                    appContext,
                    GameConfig.ADMOB_INTERSTITIAL_ID,
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            Log.d("AdMobManager", "Real Interstitial Ad loaded successfully.")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            interstitialAd = null
                            Log.d("AdMobManager", "Interstitial Ad failed to load: ${error.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("AdMobManager", "Error loading interstitial ad", e)
            }
        }
    }

    fun loadRewardedAd() {
        mainHandler.post {
            if (!GameConfig.ADS_ENABLED || !_isOnline.value) return@post
            val appContext = context.applicationContext
            val adRequest = AdRequest.Builder().build()
            try {
                RewardedAd.load(
                    appContext,
                    GameConfig.ADMOB_REWARDED_ID,
                    adRequest,
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            rewardedAd = ad
                            Log.d("AdMobManager", "Real Rewarded Ad loaded successfully.")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            rewardedAd = null
                            Log.d("AdMobManager", "Rewarded Ad failed to load: ${error.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("AdMobManager", "Error loading rewarded ad", e)
            }
        }
    }

    /**
     * Checks if ads can be shown (Ads enabled in config + internet available)
     */
    fun areAdsAvailable(): Boolean {
        return GameConfig.ADS_ENABLED && _isOnline.value
    }

    /**
     * Triggers real Interstitial Ad if loaded, otherwise falls back gracefully.
     */
    fun showInterstitial(onClosed: () -> Unit) {
        if (!areAdsAvailable()) {
            onClosed()
            return
        }
        val activity = context as? Activity
        if (interstitialAd != null && activity != null) {
            interstitialDismissCallback = onClosed
            interstitialAd?.show(activity)
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    interstitialDismissCallback?.invoke()
                    interstitialDismissCallback = null
                }
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    interstitialDismissCallback?.invoke()
                    interstitialDismissCallback = null
                }
            }
        } else {
            interstitialDismissCallback = onClosed
            _interstitialVisible.value = true
        }
    }

    fun dismissInterstitial() {
        _interstitialVisible.value = false
        interstitialDismissCallback?.invoke()
        interstitialDismissCallback = null
    }

    /**
     * Triggers real Rewarded Ad if loaded, otherwise falls back to rewarded dialog.
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

        val activity = context as? Activity
        if (rewardedAd != null && activity != null) {
            rewardedRewardCallback = onRewardGranted
            rewardedAd?.show(activity) { rewardItem ->
                Log.d("AdMobManager", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                rewardedRewardCallback?.invoke()
                rewardedRewardCallback = null
            }
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                }
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                }
            }
        } else {
            rewardedRewardCallback = onRewardGranted
            _rewardedVisible.value = true
            loadRewardedAd()
        }
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
