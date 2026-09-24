package com.example.weather.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager {

    private var appOpenAd: AppOpenAd? = null

    private var isLoadingAd = false

    private var isShowingAd = false

    fun isAdLoaded(): Boolean = appOpenAd != null

    fun loadAd(
        context: Context,
        onAdLoadFinished: () -> Unit
    ) {

        if (isLoadingAd) {
            return
        }

        if (appOpenAd != null) {
            onAdLoadFinished()
            return
        }

        isLoadingAd = true

        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            "ca-app-pub-3940256099942544/9257395921",
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false

                    onAdLoadFinished()
                }

                override fun onAdFailedToLoad(
                    error: LoadAdError
                ) {
                    appOpenAd = null
                    isLoadingAd = false

                    onAdLoadFinished()
                }
            }
        )
    }

    fun showAdIfAvailable(
        activity: Activity,
        onAdShowed: (() -> Unit)? = null,
        onAdDismissed: () -> Unit
    ) {
        if (isShowingAd) {
            return
        }

        val ad = appOpenAd

        if (ad == null) {
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                    onAdShowed?.invoke()
                }

                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false

                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(
                    adError: com.google.android.gms.ads.AdError
                ) {
                    appOpenAd = null
                    isShowingAd = false

                    onAdDismissed()
                }
            }

        ad.show(activity)
    }
}