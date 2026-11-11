package com.riyadsalihin.amharic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riyadsalihin.amharic.data.ContentIndex
import com.riyadsalihin.amharic.data.ContentRepository
import com.riyadsalihin.amharic.data.LastRead
import com.riyadsalihin.amharic.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val index: ContentIndex? = null,
    val lastRead: LastRead? = null,
)

class HomeViewModel(
    private val content: ContentRepository,
    private val progress: ProgressRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadIndex()
        viewModelScope.launch {
            progress.lastRead.collect { lastRead ->
                _state.update { it.copy(lastRead = lastRead) }
            }
        }
    }

    fun refresh() = loadIndex()

    private fun loadIndex() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            runCatching { content.index() }
                .onSuccess { index ->
                    _state.update { it.copy(loading = false, error = false, index = index) }
                }
                .onFailure {
                    _state.update { it.copy(loading = false, error = true) }
                }
        }
    }
}
