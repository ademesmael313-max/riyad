package com.riyadsalihin.amharic.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riyadsalihin.amharic.AppContainer
import com.riyadsalihin.amharic.R
import com.riyadsalihin.amharic.data.LocaleSettings
import com.riyadsalihin.amharic.data.ThemeMode
import com.riyadsalihin.amharic.ui.AppViewModelFactory
import com.riyadsalihin.amharic.ui.components.LinkRow
import com.riyadsalihin.amharic.ui.components.SectionTitle
import com.riyadsalihin.amharic.util.findActivity
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    container: AppContainer,
    onOpenAbout: () -> Unit,
) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = AppViewModelFactory { SettingsViewModel(container.settings) },
    )
    val settings by settingsViewModel.state.collectAsStateWithLifecycle()
    val language by container.locale.language.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.tab_settings),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        item { SectionTitle(stringResource(R.string.appearance)) }
        item {
            SettingsCard {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        OptionRow(
                            label = stringResource(
                                when (mode) {
                                    ThemeMode.SYSTEM -> R.string.theme_system
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                }
                            ),
                            selected = settings.theme == mode,
                            onClick = { settingsViewModel.setTheme(mode) },
                        )
                        if (mode != ThemeMode.entries.last()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.app_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp, start = 4.dp),
            )
        }
        item {
            SettingsCard {
                Column {
                    val languages = listOf(
                        LocaleSettings.SYSTEM to stringResource(R.string.language_system),
                        LocaleSettings.ENGLISH to stringResource(R.string.language_english),
                        LocaleSettings.AMHARIC to stringResource(R.string.language_amharic),
                    )
                    languages.forEachIndexed { index, (tag, label) ->
                        OptionRow(
                            label = label,
                            selected = language == tag,
                            onClick = {
                                if (language != tag) {
                                    container.locale.setLanguage(tag)
                                    context.findActivity()?.recreate()
                                }
                            },
                        )
                        if (index != languages.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            Text(
                text = stringResource(R.string.language_restart_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
            )
        }

        item { SectionTitle(stringResource(R.string.reading)) }
        item {
            SettingsCard {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.show_english),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = settings.showEnglish,
                            onCheckedChange = settingsViewModel::setShowEnglish,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ArabicSizeSlider(
                        value = settings.arabicScale,
                        onValueChangeFinished = settingsViewModel::setArabicScale,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    AmharicSizeSlider(
                        value = settings.amharicScale,
                        onValueChangeFinished = settingsViewModel::setAmharicScale,
                    )
                }
            }
        }

        item { SectionTitle(stringResource(R.string.content)) }
        item {
            SettingsCard {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.translation_progress_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.contribute_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.contribute_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item { SectionTitle(stringResource(R.string.about)) }
        item {
            LinkRow(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.about),
                subtitle = stringResource(R.string.sources),
                onClick = onOpenAbout,
            )
        }
        item {
            Text(
                text = stringResource(R.string.translation_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        content()
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ArabicSizeSlider(value: Float, onValueChangeFinished: (Float) -> Unit) {
    var local by remember(value) { mutableFloatStateOf(value) }
    SliderRow(
        label = stringResource(R.string.arabic_text_size),
        value = local,
        onValueChange = { local = it },
        onValueChangeFinished = { onValueChangeFinished(local) },
    )
}

@Composable
private fun AmharicSizeSlider(value: Float, onValueChangeFinished: (Float) -> Unit) {
    var local by remember(value) { mutableFloatStateOf(value) }
    SliderRow(
        label = stringResource(R.string.amharic_text_size),
        value = local,
        onValueChange = { local = it },
        onValueChangeFinished = { onValueChangeFinished(local) },
    )
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(value * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0.8f..1.8f,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
