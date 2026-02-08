package com.deepurls.deepurls_sdk

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
object ReferrerManager {

    private const val TAG = "DeepUrlsSDK"
    private const val PREFS_NAME = "deepurls_prefs"
    private const val KEY_REFERRER_SENT = "referrer_sent"
    private const val KEY_LAST_VERSION = "last_version"

    fun fetch(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        val currentVersion = packageInfo.versionCode
        val lastVersion = prefs.getInt(KEY_LAST_VERSION, -1)
        val alreadySent = prefs.getBoolean(KEY_REFERRER_SENT, false)
        if (lastVersion != currentVersion || !alreadySent) {
            val referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        val response = referrerClient.installReferrer
                        val referrer = response.installReferrer
                        Log.d(TAG, "Referrer = $referrer")
                        if (referrer?.contains("clickId") == true) {
                            ApiClient.sendReferrer(referrer = referrer ?: "", packageName = context.packageName)
                            prefs.edit().putBoolean(KEY_REFERRER_SENT, true).putInt(KEY_LAST_VERSION, currentVersion).apply()
                            Log.d(TAG, "clickId found. Referrer sent.")
                        } else {
                            Log.d(TAG, "No clickId found. Skipping API call.")
                        }
                    }
                    referrerClient.endConnection()
                }
                override fun onInstallReferrerServiceDisconnected() {
                    Log.w(TAG, "Referrer service disconnected")
                }
            })
        } else {
            Log.d(TAG, "Referrer already sent for this version, skipping.")
        }
    }
}
