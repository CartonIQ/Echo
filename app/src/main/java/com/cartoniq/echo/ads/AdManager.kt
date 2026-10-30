package com.cartoniq.echo.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdManager {

    private const val TAG = "EchoAdManager"

    const val APP_ID = "ca-app-pub-9771662959477951~8516632228"
    const val BANNER_ID = "ca-app-pub-9771662959477951/4688021674"
    const val INTERSTITIAL_ID = "ca-app-pub-9771662959477951/1951223873"
    const val REWARDED_ID = "ca-app-pub-9771662959477951/7575532755"

    private const val PREFS_NAME = "echo_ad_prefs"
    private const val KEY_STUDIO_EXPIRE = "studio_unlocked_until"
    private const val KEY_LAST_INTERSTITIAL = "last_interstitial_time"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var activeBannerView: AdView? = null

    var canRequestAds: Boolean = false
        private set

    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "MobileAds initialized: $initializationStatus")
        }
    }

    fun initConsent(activity: Activity, onConsentReady: (Boolean) -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    }
                    canRequestAds = consentInformation.canRequestAds()
                    onConsentReady(canRequestAds)
                }
            },
            { requestConsentError ->
                Log.w(TAG, "Consent request error: ${requestConsentError.message}")
                canRequestAds = consentInformation.canRequestAds()
                onConsentReady(canRequestAds)
            }
        )
    }

    fun resetConsent(context: Context) {
        UserMessagingPlatform.getConsentInformation(context).reset()
    }

    fun preloadAds(context: Context) {
        if (!canRequestAds) return
        loadInterstitial(context)
        loadRewarded(context)
    }

    fun setBannerView(view: AdView) {
        activeBannerView = view
    }

    fun destroyBanner() {
        activeBannerView?.destroy()
        activeBannerView = null
    }

    private fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun maybeShowInterstitialAfterSession(
        activity: Activity,
        sessionDurationMs: Long
    ) {
        if (sessionDurationMs < 30000L) return

        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastTime = prefs.getLong(KEY_LAST_INTERSTITIAL, 0L)
        val now = System.currentTimeMillis()
        if (now - lastTime < 180000L) return

        val ad = interstitialAd ?: run {
            loadInterstitial(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial(activity)
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                loadInterstitial(activity)
            }
        }

        prefs.edit().putLong(KEY_LAST_INTERSTITIAL, now).apply()
        ad.show(activity)
    }

    fun loadRewarded(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (Long) -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewarded(activity)
            onAdFailed("Rewarded ad is loading. Please try again in a moment.")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded(activity)
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                loadRewarded(activity)
                onAdFailed(adError.message)
            }
        }

        ad.show(activity) { rewardItem ->
            val expiry = System.currentTimeMillis() + 3600000L
            val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(KEY_STUDIO_EXPIRE, expiry).apply()
            onRewardEarned(expiry)
        }
    }

    fun getStudioModeExpiry(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_STUDIO_EXPIRE, 0L)
    }
}
