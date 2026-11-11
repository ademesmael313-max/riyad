package com.riyadsalihin.amharic.ui.bookmarks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riyadsalihin.amharic.AppContainer
import com.riyadsalihin.amharic.R
import com.riyadsalihin.amharic.data.SearchResult
import com.riyadsalihin.amharic.ui.AppViewModelFactory
import com.riyadsalihin.amharic.ui.components.EmptyState
import com.riyadsalihin.amharic.ui.components.LoadingState
import com.riyadsalihin.amharic.util.ArabicText

@Composable
fun BookmarksScreen(
    container: AppContainer,
    onOpenResult: (bookId: Int, hadithNumber: Int) -> Unit,
) {
    val bookmarksViewModel: BookmarksViewModel = viewModel(
        factory = AppViewModelFactory {
            BookmarksViewModel(container.content, container.bookmarks)
        },
    )
    val state by bookmarksViewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.tab_bookmarks),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp),
        )
        when {
            state.loading -> LoadingState()
            state.items.isEmpty() -> EmptyState(
                icon = Icons.Outlined.BookmarkBorder,
                title = stringResource(R.string.bookmarks_empty_title),
                body = stringResource(R.string.bookmarks_empty_body),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items, key = { "${it.bookId}-${it.hadith.number}" }) { result ->
                    BookmarkRow(result = result, onClick = {
                        onOpenResult(result.bookId, result.hadith.number)
                    })
                }
            }
        }
    }
}

@Composable
private fun BookmarkRow(result: SearchResult, onClick: () -> Unit) {
    val hadith = result.hadith
    val preview = hadith.amharic ?: hadith.english
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = result.bookTitleAm,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.hadith_number, hadith.number),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(
                text = ArabicText.excerpt(preview, 150),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
