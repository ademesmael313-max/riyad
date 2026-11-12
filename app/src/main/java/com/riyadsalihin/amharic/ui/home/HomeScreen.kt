package com.riyadsalihin.amharic.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riyadsalihin.amharic.AppContainer
import com.riyadsalihin.amharic.R
import com.riyadsalihin.amharic.data.BookSummary
import com.riyadsalihin.amharic.data.ContentIndex
import com.riyadsalihin.amharic.data.LastRead
import com.riyadsalihin.amharic.ui.AppViewModelFactory
import com.riyadsalihin.amharic.ui.components.ErrorState
import com.riyadsalihin.amharic.ui.components.LoadingState

@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenSearch: () -> Unit,
    onOpenBook: (bookId: Int, hadithNumber: Int?) -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory {
            HomeViewModel(container.content, container.progress)
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onOpenSearch = onOpenSearch,
        onOpenBook = onOpenBook,
        onRetry = viewModel::refresh,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenSearch: () -> Unit,
    onOpenBook: (bookId: Int, hadithNumber: Int?) -> Unit,
    onRetry: () -> Unit,
) {
    val index = state.index
    val appTitleFallback = stringResource(R.string.app_name_amharic)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroHeader(
                totalHadiths = index?.totalHadiths ?: 0,
                bookCount = index?.books?.size ?: 0,
            )
        }
        item { SearchEntry(onClick = onOpenSearch) }

        if (index != null) {
            item {
                TranslationProgressCard(
                    translated = index.translatedHadiths,
                    total = index.totalHadiths,
                )
            }
        }

        state.lastRead?.let { lastRead ->
            val book = index?.books?.firstOrNull { it.id == lastRead.bookId }
            item {
                ContinueReadingCard(
                    lastRead = lastRead,
                    bookTitle = book?.titleAm ?: appTitleFallback,
                    onClick = { onOpenBook(lastRead.bookId, lastRead.hadithNumber) },
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.tab_books),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
            )
        }

        when {
            state.loading -> item { LoadingState() }
            index == null -> item { ErrorState(onRetry = onRetry) }
            else -> items(index.books, key = { it.id }) { book ->
                BookRow(book = book, onClick = { onOpenBook(book.id, null) })
            }
        }
    }
}

@Composable
private fun HeroHeader(totalHadiths: Int, bookCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0B3B27), Color(0xFF176B49)),
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
        Column {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = "رياض الصالحين",
                    color = Color(0xFFEBD9A9),
                    fontSize = 19.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.app_name_amharic),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodySmall,
            )
            if (totalHadiths > 0) {
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HeroChip(stringResource(R.string.hadiths_count, totalHadiths))
                    HeroChip(stringResource(R.string.books_count, bookCount))
                }
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.14f),
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.search_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TranslationProgressCard(translated: Int, total: Int) {
    val fraction = if (total > 0) (translated.toFloat() / total).coerceIn(0f, 1f) else 0f
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.translation_progress_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.translation_progress, translated, total),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.translation_in_progress),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun ContinueReadingCard(lastRead: LastRead, bookTitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.continue_reading),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
                Text(
                    text = stringResource(
                        R.string.continue_reading_detail,
                        bookTitle,
                        lastRead.hadithNumber,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun BookRow(book: BookSummary, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = book.id.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = book.titleAm,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = book.titleEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(Modifier.padding(top = 3.dp)) {
                    Text(
                        text = stringResource(R.string.hadiths_count, book.hadithCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (book.translatedCount > 0) {
                        Text(
                            text = "  ·  " + stringResource(
                                R.string.translated_count,
                                book.translatedCount,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
