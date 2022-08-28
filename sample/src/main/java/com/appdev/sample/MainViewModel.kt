package com.appdev.sample

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _mainUiState = MutableLiveData<MainUiState>()
    val mainUiState: LiveData<MainUiState>
        get() = _mainUiState


    private val topics = listOf(
        TopicModel("Arts & Crafts", RandomIcon.icon()),
        TopicModel("Beauty", RandomIcon.icon()),
        TopicModel("Books", RandomIcon.icon()),
        TopicModel("Business", RandomIcon.icon()),
        TopicModel("Comics", RandomIcon.icon()),
        TopicModel("Culinary", RandomIcon.icon()),
        TopicModel("Design", RandomIcon.icon()),
        TopicModel("Writing", RandomIcon.icon()),
        TopicModel("Religion", RandomIcon.icon()),
        TopicModel("Technology", RandomIcon.icon()),
        TopicModel("Social sciences", RandomIcon.icon()),
        TopicModel("Arts & Crafts", RandomIcon.icon()),
        TopicModel("Beauty", RandomIcon.icon()),
        TopicModel("Books", RandomIcon.icon()),
        TopicModel("Business", RandomIcon.icon()),
        TopicModel("Comics", RandomIcon.icon()),
        TopicModel("Culinary", RandomIcon.icon()),
        TopicModel("Design", RandomIcon.icon()),
        TopicModel("Writing", RandomIcon.icon()),
        TopicModel("Religion", RandomIcon.icon()),
        TopicModel("Technology", RandomIcon.icon()),
        TopicModel("Social sciences", RandomIcon.icon())
    )

    init {
        _mainUiState.value = MainUiState(data = topics)
    }

    private var flag = true // 模拟成功失败
    fun fillData(isRefresh: Boolean) {
        viewModelScope.launch {
            runCatching {
                _mainUiState.value = _mainUiState.value?.copy(isLoading = true)
                delay(1000)
                if (isRefresh) {
                    if (flag) {
                        _mainUiState.value = _mainUiState.value?.copy(
                            refreshSuccess = true,
                            data = topics.toMutableList().apply {
                                this[0] =
                                    this[0].copy(title = System.currentTimeMillis().toString())
                            },
                            isLoading = false
                        )
                    } else {
                        _mainUiState.value =
                            _mainUiState.value?.copy(refreshSuccess = false, isLoading = false)
                    }

                } else {
                    if (flag) {
                        _mainUiState.value =
                            _mainUiState.value?.copy(
                                loadMoreSuccess = true,
                                data = _mainUiState.value?.data?.toMutableList()?.apply {
                                    addAll(topics)
                                } ?: emptyList(),
                                isLoading = false)
                    } else {
                        _mainUiState.value =
                            _mainUiState.value?.copy(loadMoreSuccess = false, isLoading = false)
                    }
                }
                flag = !flag
            }.onSuccess {
                Log.v("Loren", "fillData success")
            }.onFailure {
                Log.v("Loren", "fillData error = ${it.message}")
                if (isRefresh) {
                    _mainUiState.value =
                        _mainUiState.value?.copy(refreshSuccess = false, isLoading = false)
                } else {
                    _mainUiState.value =
                        _mainUiState.value?.copy(loadMoreSuccess = false, isLoading = false)
                }
            }

        }
    }
}

data class TopicModel(
    val title: String,
    @DrawableRes
    val icon: Int
)

data class MainUiState(
    val isLoading: Boolean = false,
    val refreshSuccess: Boolean? = null,
    val loadMoreSuccess: Boolean? = null,
    val data: List<TopicModel>
)