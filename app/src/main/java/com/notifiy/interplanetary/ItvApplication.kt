package com.notifiy.interplanetary

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ItvApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Initialize Stripe
        try {
            val publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY
            if (publishableKey.isNotBlank()) {
                com.stripe.android.PaymentConfiguration.init(
                    applicationContext,
                    publishableKey
                )
            } else {
                android.util.Log.w("ItvApplication", "Stripe publishable key is not set")
            }
        } catch (e: Exception) {
            android.util.Log.e("ItvApplication", "Failed to initialize Stripe: ${e.message}", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}
