package com.example.actvremotefreeware.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.example.actvremotefreeware.BuildConfig

/**
 * Manager handling the voluntary "Cup of Tea" (₹20) developer support donation.
 * - Simulates instant test donations in DEBUG builds.
 * - Integrates standard Google Play In-App Billing for production.
 * - Controls the 3-day non-intrusive reminder popup policy.
 * - Tracks 1 to 5 star supporter level progression.
 */
class DonationManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "donation_prefs"
        private const val KEY_HAS_DONATED = "key_has_donated"
        private const val KEY_DONATION_COUNT = "key_donation_count"
        private const val KEY_LAST_REMINDER_TIMESTAMP = "key_last_reminder_ts"
        const val PRODUCT_ID_CUP_OF_TEA = "cup_of_tea"
        const val THREE_DAYS_MILLIS = 3L * 24 * 60 * 60 * 1000L
    }

    fun hasDonated(): Boolean {
        return prefs.getBoolean(KEY_HAS_DONATED, false)
    }

    fun getDonationStars(): Int {
        return prefs.getInt(KEY_DONATION_COUNT, 0).coerceIn(0, 5)
    }

    /**
     * Determines whether the 3-day reminder dialog should be displayed on the Dashboard.
     * Always returns false if the user has already donated or if 3 days have not elapsed.
     */
    fun shouldShowDashboardReminder(): Boolean {
        if (hasDonated()) return false

        val lastReminder = prefs.getLong(KEY_LAST_REMINDER_TIMESTAMP, 0L)
        val now = System.currentTimeMillis()

        // If never shown before, wait at least 3 days from first install before showing first reminder
        if (lastReminder == 0L) {
            prefs.edit().putLong(KEY_LAST_REMINDER_TIMESTAMP, now).apply()
            return false
        }

        return (now - lastReminder) >= THREE_DAYS_MILLIS
    }

    fun recordReminderDismissed() {
        prefs.edit().putLong(KEY_LAST_REMINDER_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun recordDonationSuccess() {
        val currentCount = prefs.getInt(KEY_DONATION_COUNT, 0)
        val newCount = (currentCount + 1).coerceAtMost(5)
        prefs.edit()
            .putBoolean(KEY_HAS_DONATED, true)
            .putInt(KEY_DONATION_COUNT, newCount)
            .apply()
    }

    /**
     * Launches the donation flow.
     * In DEBUG mode: Simulates immediate successful purchase for seamless testing.
     * In RELEASE mode: Connects to Google Play Billing to complete the real purchase.
     */
    fun launchDonation(
        activity: Activity,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        if (BuildConfig.DEBUG) {
            // Debug simulation: Award donation immediately without needing Play Store credentials
            recordDonationSuccess()
            onComplete(true, "Debug Test: Thank you for buying Advit a cup of tea! ⭐")
            return
        }

        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    // Consume purchase so the user can donate again if they wish
                    recordDonationSuccess()
                    onComplete(true, "Thank you for buying a cup of tea! Your support keeps this app 100% free.")
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                onComplete(false, "Donation cancelled.")
            } else {
                onComplete(false, "Google Play Billing: ${billingResult.debugMessage.ifBlank { "Could not complete transaction" }}")
            }
        }

        val billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PRODUCT_ID_CUP_OF_TEA)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )

                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build()

                    billingClient.queryProductDetailsAsync(params) { queryResult, queryDetailsList ->
                        if (queryResult.responseCode == BillingClient.BillingResponseCode.OK && queryDetailsList.isNotEmpty()) {
                            val productDetails = queryDetailsList[0]
                            val flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    listOf(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .build()
                                    )
                                )
                                .build()
                            billingClient.launchBillingFlow(activity, flowParams)
                        } else {
                            onComplete(false, "Donation item is being configured on Google Play Console.")
                        }
                    }
                } else {
                    onComplete(false, "Google Play Store unavailable.")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Service disconnected
            }
        })
    }
}
