package com.mediationmixer.app.ui.library

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbCloudy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditationmixer.core.domain.usecase.PreviewAmbienceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val previewAmbience: PreviewAmbienceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadAmbienceList()
    }

    private fun loadAmbienceList() {
        val ambiences = listOf(
            AmbienceItemData(
                id = "rain_light",
                name = "Light Rain",
                description = "Gentle rainfall on leaves",
                icon = Icons.Default.Water,
                assetPath = "ambience/rain_light.ogg"
            ),
            AmbienceItemData(
                id = "rain_heavy",
                name = "Heavy Rain",
                description = "Intense thunderstorm",
                icon = Icons.Default.WbCloudy,
                assetPath = "ambience/rain_heavy.ogg"
            ),
            AmbienceItemData(
                id = "ocean_waves",
                name = "Ocean Waves",
                description = "Calm beach waves",
                icon = Icons.Default.Waves,
                assetPath = "ambience/ocean_waves.ogg"
            ),
            AmbienceItemData(
                id = "forest_night",
                name = "Forest Night",
                description = "Crickets and night sounds",
                icon = Icons.Default.Forest,
                assetPath = "ambience/forest_night.ogg"
            ),
            AmbienceItemData(
                id = "wind_soft",
                name = "Soft Wind",
                description = "Gentle breeze through trees",
                icon = Icons.Default.Nature,
                assetPath = "ambience/wind_soft.ogg"
            ),
            AmbienceItemData(
                id = "river_stream",
                name = "River Stream",
                description = "Flowing water over rocks",
                icon = Icons.Default.Water,
                assetPath = "ambience/river_stream.ogg"
            )
        )
        _uiState.update { it.copy(ambienceList = ambiences) }
    }

    fun togglePlay(ambienceId: String) {
        viewModelScope.launch {
            val currentPlaying = _uiState.value.playingId
            if (currentPlaying == ambienceId) {
                previewAmbience.stop()
                _uiState.update { it.copy(playingId = null) }
            } else {
                val ambience = _uiState.value.ambienceList.find { it.id == ambienceId }
                ambience?.let {
                    previewAmbience(it.assetPath)
                    _uiState.update { state -> state.copy(playingId = ambienceId) }
                }
            }
        }
    }
}

data class LibraryUiState(
    val ambienceList: List<AmbienceItemData> = emptyList(),
    val playingId: String? = null
)
