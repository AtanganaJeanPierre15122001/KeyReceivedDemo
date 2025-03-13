package com.example.keyreceiveddemo.data



import android.annotation.SuppressLint
import androidx.security.crypto.MasterKey
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.keyreceiveddemo.domain.EncryptedPreferences
import com.example.keyreceiveddemo.model.Keys
import android.content.Context
import androidx.datastore.dataStore


const val PREF_NAME = "apiKeys"
const val PREF_FIRST_KEY = "firstKey"
const val PREF_SECOND_KEY = "secondKey"


//private val Context.dataStore by dataStore(
//    fileName = PREF_NAME,
//    serializer = KeysSerializer
//)

class EncryptedPreferencesImpl(context: Context) : EncryptedPreferences {


    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build() as? MasterKey

    private val preferences = masterKey?.let {
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            it,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @SuppressLint("CommitPrefEdits")
    override suspend fun saveEncryptedData(keys: Keys): Boolean {
        return if (preferences != null) {
            preferences.edit().apply() {
                putString(PREF_FIRST_KEY, keys.firstKey)
                putString(PREF_SECOND_KEY, keys.secondKey)
            }
            true
        } else false
    }

    override suspend fun readEncryptedData(): Keys? {
        val firstKey = preferences?.getString(PREF_FIRST_KEY, null)
        val secondKey = preferences?.getString(PREF_SECOND_KEY, null)

        return if (firstKey != null && secondKey != null)
            Keys(firstKey = firstKey, secondKey = secondKey)
        else null
    }

    override suspend fun areApiKeysReady(): Boolean {
        val firstCondition = preferences != null
                && preferences.contains(PREF_FIRST_KEY)
                && preferences.contains(PREF_SECOND_KEY)
        val secondCondition = readEncryptedData() != null
        return firstCondition && secondCondition
    }
}