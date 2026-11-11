package com.riyadsalihin.amharic.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riyadsalihin.amharic.data.BookContent
import com.riyadsalihin.amharic.data.BookmarksRepository
import com.riyadsalihin.amharic.data.ContentRepository
import com.riyadsalihin.amharic.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val book: BookContent? = null,
    val bookmarks: Set<Int> = emptySet(),
)

class ReaderViewModel(
    private val bookId: Int,
    private val content: ContentRepository,
    private val bookmarks: BookmarksRepository,
    private val progress: ProgressRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            bookmarks.bookmarks.collect { saved ->
                _state.update { it.copy(bookmarks = saved) }
            }
        }
    }

    fun load() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            runCatching { content.book(bookId) }
                .onSuccess { book ->
                    _state.update { it.copy(loading = false, error = false, book = book) }
                }
                .onFailure {
                    _state.update { it.copy(loading = false, error = true) }
                }
        }
    }

    fun toggleBookmark(hadithNumber: Int) {
        viewModelScope.launch { bookmarks.toggle(hadithNumber) }
    }

    fun onHadithVisible(hadithNumber: Int) {
        viewModelScope.launch { progress.markRead(bookId, hadithNumber) }
    }
}
