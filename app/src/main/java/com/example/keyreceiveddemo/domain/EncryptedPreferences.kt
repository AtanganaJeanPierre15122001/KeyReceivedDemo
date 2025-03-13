package com.example.keyreceiveddemo.domain

import com.example.keyreceiveddemo.model.Keys

interface EncryptedPreferences {
    suspend fun saveEncryptedData(keys: Keys): Boolean
    suspend fun readEncryptedData(): Keys?
    suspend fun areApiKeysReady(): Boolean
}