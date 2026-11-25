package com.example.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "vakilyar_offline_aes_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ENC_PREFIX = "ENC_V1:"

    private val fallbackKey: SecretKey by lazy {
        // Fallback deterministic-per-session key if KeyStore isn't available (e.g., unit tests)
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("VakilYarLocalFallbackKey2026".toByteArray(Charsets.UTF_8))
        SecretKeySpec(bytes, "AES")
    }

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hashSecret(secret: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:$secret".toByteArray(Charsets.UTF_8)
        val digest = md.digest(combined)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    fun verifySecret(secret: String, salt: String, expectedHash: String): Boolean {
        return hashSecret(secret, salt) == expectedHash
    }

    fun generateOtpCode(): String {
        val num = 100000 + SecureRandom().nextInt(900000)
        return num.toString()
    }

    private fun getOrCreateAesKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing

            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        } catch (_: Throwable) {
            fallbackKey
        }
    }

    fun encryptText(plainText: String): String {
        if (plainText.isBlank()) return ""
        if (plainText.startsWith(ENC_PREFIX)) return plainText
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateAesKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val dataB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            "$ENC_PREFIX$ivB64:$dataB64"
        } catch (_: Throwable) {
            plainText
        }
    }

    fun decryptText(storedText: String): String {
        if (storedText.isBlank()) return ""
        if (!storedText.startsWith(ENC_PREFIX)) return storedText
        return try {
            val payload = storedText.removePrefix(ENC_PREFIX)
            val parts = payload.split(":")
            if (parts.size != 2) return storedText
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val data = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateAesKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(data), Charsets.UTF_8)
        } catch (_: Throwable) {
            ""
        }
    }
}
