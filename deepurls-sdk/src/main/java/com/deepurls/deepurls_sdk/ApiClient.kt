package com.deepurls.deepurls_sdk

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.thread

object ApiClient {

    private const val TAG = "DeepUrlsSDK"
    private val client = OkHttpClient()
    private fun generateNonce(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
    fun sendReferrer(referrer: String, packageName: String) {
        val cfg = DeepUrls.getConfig()
        thread {
            try {
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val nonce = generateNonce()
                val bodyJson = JSONObject().apply {
                    put("appId", cfg.appId)
                    put("referrer", referrer)
                }
                val signaturePayload = """{"appId":"${cfg.appId}","referrer":"$referrer","timestamp":${timestamp.toInt()},"nonce":"$nonce"}"""
                val signature = CryptoUtils.hmacSha256(signaturePayload, cfg.deepKey)
                val request = Request.Builder()
                    .url("https://us-central1-v3deeplinks.cloudfunctions.net/postReferrer")
                    .addHeader("x-timestamp", timestamp)
                    .addHeader("x-nonce", nonce)
                    .addHeader("x-signature", signature)
                    .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                val response = client.newCall(request).execute()
                val respBody = response.body?.string()
                Log.d("DeepUrlsSDK", "Referre respons = $respBody")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send referrer", e)
            }
        }
    }

    fun createLink(
        route: String,
        params: Map<String, Any> = emptyMap(),
        useShort: Boolean = true,
        callback: (success: Boolean, url: String?, longUrl: String?) -> Unit
    ) {
        val cfg = DeepUrls.getConfig()
        thread {
            try {
                val timestamp = (System.currentTimeMillis() / 1000).toString()
                val nonce = generateNonce()
                val bodyJson = JSONObject().apply {
                    put("appId", cfg.appId)
                    put("route", route)
                    put("params", JSONObject(params))
                    put("useShort", useShort)
                    put("timestamp", timestamp.toInt())
                    put("nonce", nonce)
                }
                val paramsJson = JSONObject(params).toString()
                val signaturePayload = """{"appId":"${cfg.appId}","route":"$route","params":$paramsJson,"timestamp":${timestamp.toInt()},"nonce":"$nonce"}"""
                val signature = CryptoUtils.hmacSha256(signaturePayload.toString(), cfg.deepKey)
                val body = bodyJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://us-central1-v3deeplinks.cloudfunctions.net/postGenerateLink")
                    .addHeader("x-timestamp", timestamp)
                    .addHeader("x-nonce", nonce)
                    .addHeader("x-signature", signature)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val respBody = response.body?.string()
                Log.d("DeepUrlsSDK", "CreateLink raw response = $respBody")
                if (!response.isSuccessful || respBody.isNullOrBlank()) {
                    callback(false, null, null)
                    return@thread
                }
                val json = JSONObject(respBody)
                callback(true, json.optString("url"), json.optString("longUrl"))
            } catch (e: Exception) {
                callback(false, null, null)
            }
        }
    }
}
