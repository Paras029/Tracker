package com.parasgarg.tracker.feature.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.domain.FoodOption
import com.parasgarg.tracker.data.model.domain.MacroTotals
import com.parasgarg.tracker.data.model.domain.MealType
import com.parasgarg.tracker.data.model.domain.NutritionEntry
import com.parasgarg.tracker.data.model.domain.UserProfile
import com.parasgarg.tracker.data.repository.NutritionRepository
import com.parasgarg.tracker.data.repository.UserProfileRepository
import com.parasgarg.tracker.data.source.StaticFoodDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddFoodState(
    val query: String = "",
    val results: List<FoodOption> = emptyList(),
    val selected: FoodOption? = null,
    val servingGrams: String = "100",
    val mealType: MealType = MealType.BREAKFAST,
)

data class NutritionUiState(
    val date: LocalDate = LocalDate.now(),
    val entries: List<NutritionEntry> = emptyList(),
    val totals: MacroTotals = MacroTotals(),
    val targets: MacroTotals = MacroTotals(),
    val showAddSheet: Boolean = false,
    val addFood: AddFoodState = AddFoodState(),
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val profileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        observeEntries()
        observeProfile()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeEntries() {
        viewModelScope.launch {
            _uiState
                .map { it.date }
                .distinctUntilChanged()
                .flatMapLatest { date -> nutritionRepository.observeEntriesForDate(date) }
                .collect { entries ->
                    _uiState.update { state ->
                        state.copy(entries = entries, totals = computeTotals(entries))
                    }
                }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.observe().collect { profile ->
                _uiState.update { state ->
                    state.copy(targets = profile.toTargets())
                }
            }
        }
    }

    fun previousDay() = _uiState.update { it.copy(date = it.date.minusDays(1)) }
    fun nextDay() = _uiState.update { it.copy(date = it.date.plusDays(1)) }

    fun openAddSheet(mealType: MealType = MealType.BREAKFAST) {
        _uiState.update { it.copy(showAddSheet = true, addFood = AddFoodState(mealType = mealType)) }
    }

    fun closeAddSheet() = _uiState.update { it.copy(showAddSheet = false, addFood = AddFoodState()) }

    fun updateSearchQuery(query: String) {
        val results = StaticFoodDatabase.search(query)
        _uiState.update { it.copy(addFood = it.addFood.copy(query = query, results = results, selected = null)) }
    }

    fun selectFood(food: FoodOption) {
        _uiState.update { it.copy(addFood = it.addFood.copy(selected = food, query = food.name, results = emptyList())) }
    }

    fun updateServingGrams(value: String) {
        _uiState.update { it.copy(addFood = it.addFood.copy(servingGrams = value)) }
    }

    fun updateMealType(mealType: MealType) {
        _uiState.update { it.copy(addFood = it.addFood.copy(mealType = mealType)) }
    }

    fun logFood() {
        val state = _uiState.value.addFood
        val food = state.selected ?: return
        val grams = state.servingGrams.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        val factor = grams / 100.0
        viewModelScope.launch {
            nutritionRepository.logFood(
                NutritionEntry(
                    id = UUID.randomUUID().toString(),
                    date = _uiState.value.date,
                    mealType = state.mealType,
                    foodName = food.name,
                    servingGrams = grams,
                    caloriesKcal = food.caloriesPer100g * factor,
                    proteinG = food.proteinPer100g * factor,
                    carbsG = food.carbsPer100g * factor,
                    fatG = food.fatPer100g * factor,
                    loggedAt = Instant.now(),
                ),
            )
            closeAddSheet()
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch { nutritionRepository.deleteEntry(id) }
    }

    private fun computeTotals(entries: List<NutritionEntry>) = MacroTotals(
        caloriesKcal = entries.sumOf { it.caloriesKcal },
        proteinG = entries.sumOf { it.proteinG },
        carbsG = entries.sumOf { it.carbsG },
        fatG = entries.sumOf { it.fatG },
    )

    private fun UserProfile?.toTargets() = MacroTotals(
        caloriesKcal = this?.targetCaloriesKcal ?: 0.0,
        proteinG = this?.targetProteinG ?: 0.0,
        carbsG = this?.targetCarbsG ?: 0.0,
        fatG = this?.targetFatG ?: 0.0,
    )
}
