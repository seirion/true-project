package com.trueedu.project.ui.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.trueedu.project.R
import com.trueedu.project.analytics.TrueAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdmobManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val trueAnalytics: TrueAnalytics,
) {
    companion object {
        private val TAG = AdmobManager::class.java.simpleName
    }
    //private var rewardedAd: RewardedAd? = null
    val nativeAd = mutableStateOf<NativeAd?>(null)

    fun loadNativeAd() {
        val adLoader = AdLoader.Builder(applicationContext, applicationContext.getString(R.string.native_ad_unit_id))
            .forNativeAd { ad: NativeAd ->
                this.nativeAd.value = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {

                }

                override fun onAdClicked() {
                    trueAnalytics.log("native_ad__click")
                    super.onAdClicked()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(
                        NativeAdOptions.ADCHOICES_TOP_RIGHT
                    ).build()
            )
            .build()


        adLoader.loadAd(AdRequest.Builder().build())
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BannerAd(adUnitId: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    this.adUnitId = adUnitId
                    setAdSize(AdSize.BANNER)
                    loadAd(AdRequest.Builder().build())
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    this.adListener = object : AdListener() {
                        override fun onAdClicked() {
                        }

                        override fun onAdClosed() {
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("admob", adError.toString())
                        }

                        override fun onAdImpression() {
                        }

                        override fun onAdLoaded() {
                        }

                        override fun onAdOpened() {
                        }

                        override fun onAdSwipeGestureClicked() {
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun NativeAdView(nativeAd: NativeAd) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(false) {  }

    ) {
        val colorScheme = MaterialTheme.colorScheme
        AndroidView(
            factory = { context ->
                TrueNativeAdView(context).also {
                    it.setNativeAd(nativeAd, colorScheme)
                }
            },
            update = {
                it.setNativeAd(nativeAd, colorScheme)
            }
        )
    }
}