package com.riyadsalihin.amharic.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riyadsalihin.amharic.AppContainer
import com.riyadsalihin.amharic.R
import com.riyadsalihin.amharic.data.AppSettings
import com.riyadsalihin.amharic.data.BookContent
import com.riyadsalihin.amharic.ui.AppViewModelFactory
import com.riyadsalihin.amharic.ui.components.ErrorState
import com.riyadsalihin.amharic.ui.components.HadithCard
import com.riyadsalihin.amharic.ui.components.LoadingState
import com.riyadsalihin.amharic.ui.components.RiyadTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ReaderScreen(
    container: AppContainer,
    bookId: Int,
    initialHadith: Int?,
    onBack: () -> Unit,
) {
    val readerViewModel: ReaderViewModel = viewModel(
        key = "reader-$bookId",
        factory = AppViewModelFactory {
            ReaderViewModel(bookId, container.content, container.bookmarks, container.progress)
        },
    )
    val state by readerViewModel.state.collectAsStateWithLifecycle()
    val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    val listState = rememberLazyListState()
    val book = state.book
    var didScrollToTarget by remember(bookId) { mutableStateOf(false) }

    LaunchedEffect(book, initialHadith) {
        if (book == null || didScrollToTarget) return@LaunchedEffect
        val targetIndex = if (initialHadith != null) {
            book.hadiths.indexOfFirst { it.number == initialHadith }
        } else {
            0
        }
        if (targetIndex > 0) {
            listState.scrollToItem(targetIndex + 1)
        }
        didScrollToTarget = true
    }

    LaunchedEffect(book) {
        val currentBook = book ?: return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collectLatest { firstVisible ->
                delay(700)
                val hadithIndex = (firstVisible - 1).coerceAtLeast(0)
                currentBook.hadiths.getOrNull(hadithIndex)?.let {
                    readerViewModel.onHadithVisible(it.number)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RiyadTopBar(
            title = book?.titleAm ?: stringResource(R.string.app_name_amharic),
            subtitle = book?.titleEn,
            onBack = onBack,
        )
        when {
            state.loading -> LoadingState(Modifier.weight(1f))
            book == null -> ErrorState(onRetry = readerViewModel::load, modifier = Modifier.weight(1f))
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "header") { BookHeader(book) }
                items(book.hadiths, key = { it.number }) { hadith ->
                    HadithCard(
                        hadith = hadith,
                        isBookmarked = hadith.number in state.bookmarks,
                        showEnglish = settings.showEnglish,
                        arabicScale = settings.arabicScale,
                        amharicScale = settings.amharicScale,
                        onToggleBookmark = { readerViewModel.toggleBookmark(hadith.number) },
                    )
                }
                item(key = "footer") { ReaderFooter(book) }
            }
        }
    }
}

@Composable
private fun BookHeader(book: BookContent) {
    val translated = book.hadiths.count { it.hasAmharic }
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = book.titleAr,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = book.titleAm,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = book.titleEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
            Row(Modifier.padding(top = 10.dp)) {
                Text(
                    text = stringResource(R.string.hadiths_count, book.hadiths.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "  ·  " + stringResource(R.string.translated_count, translated),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun ReaderFooter(book: BookContent) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.translation_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_name_amharic) + " · " +
                stringResource(R.string.hadiths_count, book.hadiths.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
