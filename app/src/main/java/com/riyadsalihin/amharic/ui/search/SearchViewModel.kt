package com.riyadsalihin.amharic.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riyadsalihin.amharic.data.ContentRepository
import com.riyadsalihin.amharic.data.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val searching: Boolean = false,
    val hasSearched: Boolean = false,
)

class SearchViewModel(private val content: ContentRepository) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _state.update { it.copy(results = emptyList(), searching = false, hasSearched = false) }
            return
        }
        searchJob = viewModelScope.launch {
            _state.update { it.copy(searching = true) }
            delay(250)
            val found = runCatching { content.search(trimmed) }.getOrDefault(emptyList())
            _state.update {
                it.copy(searching = false, hasSearched = true, results = found)
            }
        }
    }

    fun clear() = onQueryChange("")
}
