package com.trueedu.project.utils


import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException

/**
 * AES 복호화를 수행해 반환합니다.
 *
 * @param encryptedText 암호화된 텍스트
 * @param key 키
 * @param iv 초기화 벡터
 */
fun decryptAes(encryptedText: String, key: String, iv: String): String {
    try {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

        val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)

        return String(decryptedBytes)
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: InvalidKeyException) {
        e.printStackTrace()
    }
    return "" // 또는 예외 처리
}
