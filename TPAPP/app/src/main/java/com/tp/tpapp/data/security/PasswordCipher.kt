package com.tp.tpapp.data.security

interface PasswordCipher {
    fun encrypt(plainText: String): String
    fun decrypt(cipherText: String): String
    fun isEncrypted(value: String): Boolean
}
