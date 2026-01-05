package com.deepurls.deepurls_sdk

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener

object ReferrerManager {

    fun fetch(context: Context) {

        val referrerClient = InstallReferrerClient.newBuilder(context).build()

        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    val response = referrerClient.installReferrer
                    val referrer = response.installReferrer

                    Log.d("DeepUrlsSDK", "Referrer = $referrer")

                    ApiClient.sendReferrer(
                        referrer = referrer ?: "",
                        packageName = context.packageName
                    )
                }
                referrerClient.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {
                Log.w("DeepUrlsSDK", "Referrer service disconnected")
            }
        })
    }
}
