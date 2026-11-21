package app.quantumsocial.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import app.quantumsocial.core.security.SealedText
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Šifrovanie citlivých textov (bod 9) kľúčom v Android Keystore.
 * Kľúč nikdy neopustí zariadenie – ani aplikácia ho nevie prečítať, len ním šifrovať.
 */
object KeyVault {
    private const val STORE = "AndroidKeyStore"
    private const val ALIAS = "qs_vault_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_BITS = 128

    fun seal(plain: String): SealedText {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return SealedText(iv = cipher.iv, cipher = cipher.doFinal(plain.toByteArray(Charsets.UTF_8)))
    }

    /** @return null, ak sa nedá rozšifrovať (napr. dáta z iného zariadenia alebo zmazaný kľúč) */
    fun open(sealed: SealedText): String? =
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, sealed.iv))
            String(cipher.doFinal(sealed.cipher), Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            null
        }

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance(STORE).apply { load(null) }
        (ks.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, STORE)
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return gen.generateKey()
    }
}
