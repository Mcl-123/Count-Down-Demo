package com.saicmotor.pvwav.asyncdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Author: machenglong
 * Date: 2020/7/21 10:53
 */
class MainViewModel : ViewModel() {

    val titleLiveData = MutableLiveData<String>()
    val btnClickableLiveData = MutableLiveData<Boolean>()

    @ExperimentalCoroutinesApi
    fun startCountDown() {
        viewModelScope.launch {
            flow {
                (5 downTo 1).forEach {
                    emit("${it}s")
                    delay(1000)
                }
            }.flowOn(Dispatchers.Default)
                .onStart {
                    btnClickableLiveData.value = false
                }
                .onCompletion {
                    btnClickableLiveData.value = true
                    titleLiveData.value = "Coroutine 完成"
                }
                .collect {
                    titleLiveData.value = it
                }
        }
    }
}