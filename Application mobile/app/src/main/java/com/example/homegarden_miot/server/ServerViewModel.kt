package com.example.homegarden_miot.server

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ServerViewModel : ViewModel() {

    private val apiClient = ApiClient

    fun getApiService(): ApiService {
        return apiClient.apiService
    }

}