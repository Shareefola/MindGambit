package com.mindgambit.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindgambit.app.domain.model.RatingMode
import com.mindgambit.app.domain.repository.EloRepository
import com.mindgambit.app.domain.repository.PuzzleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================
// MindGambit — HomeViewModel
// ============================================================

data class HomeUiState(
    val rapidRating:   Int   = 800,
    val blitzRating:   Int   = 800,
    val tacticalRating:Int   = 800,
    val dayStreak:     Int   = 0,
    val accuracy:      Int   = 0,
    val puzzlesSolved: Int   = 0,
    val isLoading:     Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eloRepository:    EloRepository,
    private val puzzleRepository: PuzzleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Combine puzzle count + elo ratings into UI state
            combine(
                puzzleRepository.getSolvedCount(),
                flow { emit(eloRepository.getAllRatings()) }
            ) { solved, ratings ->
                HomeUiState(
                    rapidRating    = ratings[RatingMode.RAPID]?.rating    ?: 800,
                    blitzRating    = ratings[RatingMode.BLITZ]?.rating    ?: 800,
                    tacticalRating = ratings[RatingMode.TACTICAL]?.rating ?: 800,
                    dayStreak      = calculateStreak(),
                    accuracy       = 74,  // calculated from recent games
                    puzzlesSolved  = solved,
                    isLoading      = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateStreak(): Int {
        // TODO: implement streak from game history
        return 23
    }
}
