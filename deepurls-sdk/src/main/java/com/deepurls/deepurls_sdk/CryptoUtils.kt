package com.deepurls.deepurls_sdk

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    fun hmacSha256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(keySpec)
        val rawHmac = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return rawHmac.joinToString("") { "%02x".format(it) }
    }
}
