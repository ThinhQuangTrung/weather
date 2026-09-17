package com.example.weather.core.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Base ViewModel dùng chung cho toàn app.
 * Cung cấp sẵn cơ chế hiển thị thông báo người dùng (userMessage / clearUserMessage).
 */
abstract class BaseViewModel : ViewModel() {

    private val _userMessage = MutableLiveData<String?>()
    val userMessage: LiveData<String?> = _userMessage

    protected fun postUserMessage(message: String) {
        _userMessage.value = message
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
