package com.riyadsalihin.amharic.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riyadsalihin.amharic.data.BookmarksRepository
import com.riyadsalihin.amharic.data.ContentRepository
import com.riyadsalihin.amharic.data.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookmarksUiState(
    val loading: Boolean = true,
    val items: List<SearchResult> = emptyList(),
)

class BookmarksViewModel(
    private val content: ContentRepository,
    private val bookmarks: BookmarksRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookmarksUiState())
    val state: StateFlow<BookmarksUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            bookmarks.bookmarks.collect { saved ->
                if (saved.isEmpty()) {
                    _state.update { BookmarksUiState(loading = false, items = emptyList()) }
                } else {
                    val items = runCatching { content.hadiths(saved) }.getOrDefault(emptyList())
                    _state.update { BookmarksUiState(loading = false, items = items) }
                }
            }
        }
    }
}
