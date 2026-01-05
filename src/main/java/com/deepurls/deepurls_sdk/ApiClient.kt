package com.deepurls.deepurls_sdk

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.concurrent.thread
import okhttp3.OkHttpClient
import okhttp3.Request

object ApiClient {

    private const val TAG = "DeepUrlsSDK"
    private val client = OkHttpClient()

    /** Sends install referrer to hardcoded backend */
    fun sendReferrer(referrer: String, packageName: String) {
        val cfg = DeepUrls.getConfig()

        thread {
            try {
                val json = """
                    {
                      "referrer": "$referrer",
                      "package": "$packageName"
                    }
                """.trimIndent()

                val body = json.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("https://us-central1-v3deeplinks.cloudfunctions.net/postReferrer")
                    .addHeader("x-deeurl-key", cfg.apiKey)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                Log.d(TAG, "Referrer sent, response code = ${response.code}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send referrer", e)
            }
        }
    }

    /** Creates a deep link by sending route + params to backend */
    fun createLink(route: String, params: Map<String, Any> = emptyMap(), useShort: Boolean = true, callback: (success: Boolean, url: String?, longUrl: String?) -> Unit) {
        val cfg = DeepUrls.getConfig()

        thread {
            try {
                // Prepare JSON body
                val jsonBody = JSONObject()
                jsonBody.put("appId", cfg.appId)
                jsonBody.put("route", route)
                jsonBody.put("params", JSONObject(params))
                jsonBody.put("useShort", useShort)

                val body = jsonBody.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(cfg.apiUrl)
                    .addHeader("x-deep-key", cfg.apiKey)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val respBody = response.body?.string()
                if (response.isSuccessful && respBody != null) {
                    val jsonResp = JSONObject(respBody)
                    val success = jsonResp.optBoolean("success", false)
                    val url = jsonResp.optString("url", null)
                    val longUrl = jsonResp.optString("longUrl", null)
                    callback(success, url, longUrl)
                } else {
                    callback(false, null, null)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to create link", e)
                callback(false, null, null)
            }
        }
    }
}
