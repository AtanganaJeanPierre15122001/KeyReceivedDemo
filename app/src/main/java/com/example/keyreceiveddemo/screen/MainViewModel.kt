package com.example.keyreceiveddemo.screen

import androidx.lifecycle.ViewModel
import com.example.keyreceiveddemo.domain.EncryptedPreferences
import com.example.keyreceiveddemo.domain.KeyProviderService
import com.example.keyreceiveddemo.util.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.keyreceiveddemo.model.Keys
import com.example.keyreceiveddemo.util.KeyPairHandler

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferences: EncryptedPreferences,
    private val keyProviderService: KeyProviderService
) : ViewModel() {

    private var _apiKeysReady: MutableState<RequestState<Boolean>> =
        mutableStateOf(RequestState.Idle)
    val apiKeysReady: State<RequestState<Boolean>> = _apiKeysReady

    private var _apiKeys: MutableState<Keys?> = mutableStateOf(null)
    val apiKeys: State<Keys?> = _apiKeys

    init {
        fetchData()
    }

    private suspend fun fetchApiKeysAndStoreThemSecurely(): RequestState<Boolean> {
        return try {
            KeyPairHandler.generateKeyPair()
            val publicKey = KeyPairHandler.getPublicKeyString()
            val fetchedData = fetchEncryptedApiKeys(publicKey = publicKey)
            if (fetchedData != null) {
                val decryptedData = KeyPairHandler.decryptTheData(encryptedData = fetchedData)
                val keys = Json.decodeFromString<Keys>(decryptedData)
                val result = preferences.saveEncryptedData(keys = keys)
                _apiKeys.value = preferences.readEncryptedData()
                RequestState.Success(data = result)
            } else {
                throw ApiKeysException(message = "Failed to Fetch API Keys.")
            }
        } catch (e: Exception) {
            RequestState.Error(message = "${e.message}")
        }
    }

    private suspend fun fetchEncryptedApiKeys(publicKey: String): String? {
        val response = keyProviderService.getEncryptedApiKeys(publicKey = publicKey)
        return if (response.isSuccessful) response.body()
        else throw ApiKeysException(message = response.message())
    }

    fun fetchData() {
        viewModelScope.launch(Dispatchers.Main) {
            _apiKeysReady.value = RequestState.Loading
            delay(1000)
            _apiKeysReady.value = fetchApiKeysAndStoreThemSecurely()
        }
    }
}

class ApiKeysException(message: String) : Exception(message)