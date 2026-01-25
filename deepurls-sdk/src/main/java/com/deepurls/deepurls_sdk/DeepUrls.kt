package com.deepurls.deepurls_sdk

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.InputStream

object DeepUrls {

    private var config: DeepUrlsConfig? = null
    private const val TAG = "DeepUrlsSDK"

    /** Initialize SDK: loads config and fetches referrer */
    fun init(context: Context) {
        loadConfig(context)        // load JSON config
        ReferrerManager.fetch(context) // fetch install referrer automatically
    }

    /** Public function for SDK users to create deep links */
    fun createLink(route: String, params: Map<String, Any> = emptyMap(), useShort: Boolean = true, callback: (success: Boolean, url: String?, longUrl: String?) -> Unit) {
        ApiClient.createLink(route, params, useShort, callback)
    }

    /** Load JSON config from assets */
    private fun loadConfig(context: Context) {
        try {
            val inputStream: InputStream = context.assets.open("deepUrlsConfig.json")
            val jsonStr = inputStream.bufferedReader().use { it.readText() }
            val jsonObj = JSONObject(jsonStr)
            config = DeepUrlsConfig(
                appId = jsonObj.getString("appId"),
                deepKey = jsonObj.getString("deepKey"),
            )
            Log.d(TAG, "Config loaded: $config")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load config", e)
        }
    }

    /** Internal getter for SDK usage */
    internal fun getConfig(): DeepUrlsConfig {
        return config ?: throw IllegalStateException("SDK not initialized or config missing")
    }
}
