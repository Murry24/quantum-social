package app.quantumsocial.core.security

import java.util.Base64

/**
 * Zašifrovaný text tak, ako sa ukladá na disk: verzia, IV a šifrovaný obsah v jednom reťazci.
 * Samotné šifrovanie robí Android Keystore (app), tu je len formát – aby sa dal testovať.
 */
data class SealedText(
    val iv: ByteArray,
    val cipher: ByteArray,
) {
    fun encode(): String = "$VERSION:${b64.encodeToString(iv)}:${b64.encodeToString(cipher)}"

    override fun equals(other: Any?): Boolean =
        other is SealedText && iv.contentEquals(other.iv) && cipher.contentEquals(other.cipher)

    override fun hashCode(): Int = 31 * iv.contentHashCode() + cipher.contentHashCode()

    companion object {
        const val VERSION = "v1"
        private val b64 = Base64.getEncoder()
        private val unb64 = Base64.getDecoder()

        /** @return null, ak reťazec nie je v známom formáte (poškodený alebo iná verzia) */
        fun decode(stored: String): SealedText? {
            val parts = stored.split(":")
            if (parts.size != 3 || parts[0] != VERSION) return null
            return try {
                val iv = unb64.decode(parts[1])
                val cipher = unb64.decode(parts[2])
                if (iv.isEmpty() || cipher.isEmpty()) null else SealedText(iv, cipher)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
