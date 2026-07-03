package com.parasgarg.tracker.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parasgarg.tracker.data.model.domain.UserProfile
import com.parasgarg.tracker.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileForm(
    val name: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val activityLevel: String = "",
    val fitnessGoal: String = "",
    val targetCaloriesKcal: String = "",
    val targetProteinG: String = "",
    val targetCarbsG: String = "",
    val targetFatG: String = "",
    val geminiApiKey: String = "",
    val isGeminiOfflineMode: Boolean = false,
)

data class ProfileUiState(
    val form: ProfileForm = ProfileForm(),
    val saved: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observe().collect { profile ->
                if (profile != null) {
                    _uiState.update { state ->
                        state.copy(
                            form = ProfileForm(
                                name = profile.name,
                                heightCm = profile.heightCm?.toString() ?: "",
                                weightKg = profile.weightKg?.toString() ?: "",
                                activityLevel = profile.activityLevel ?: "",
                                fitnessGoal = profile.fitnessGoal ?: "",
                                targetCaloriesKcal = profile.targetCaloriesKcal?.toString() ?: "",
                                targetProteinG = profile.targetProteinG?.toString() ?: "",
                                targetCarbsG = profile.targetCarbsG?.toString() ?: "",
                                targetFatG = profile.targetFatG?.toString() ?: "",
                                geminiApiKey = profile.geminiApiKey ?: "",
                                isGeminiOfflineMode = profile.isGeminiOfflineMode,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun updateName(v: String) = _uiState.update { it.copy(form = it.form.copy(name = v)) }
    fun updateHeight(v: String) = _uiState.update { it.copy(form = it.form.copy(heightCm = v)) }
    fun updateWeight(v: String) = _uiState.update { it.copy(form = it.form.copy(weightKg = v)) }
    fun updateActivityLevel(v: String) = _uiState.update { it.copy(form = it.form.copy(activityLevel = v)) }
    fun updateFitnessGoal(v: String) = _uiState.update { it.copy(form = it.form.copy(fitnessGoal = v)) }
    fun updateTargetCalories(v: String) = _uiState.update { it.copy(form = it.form.copy(targetCaloriesKcal = v)) }
    fun updateTargetProtein(v: String) = _uiState.update { it.copy(form = it.form.copy(targetProteinG = v)) }
    fun updateTargetCarbs(v: String) = _uiState.update { it.copy(form = it.form.copy(targetCarbsG = v)) }
    fun updateTargetFat(v: String) = _uiState.update { it.copy(form = it.form.copy(targetFatG = v)) }
    fun updateGeminiApiKey(v: String) = _uiState.update { it.copy(form = it.form.copy(geminiApiKey = v)) }
    fun toggleGeminiOfflineMode(v: Boolean) = _uiState.update { it.copy(form = it.form.copy(isGeminiOfflineMode = v)) }

    fun save() {
        viewModelScope.launch {
            val form = _uiState.value.form
            repository.save(
                UserProfile(
                    name = form.name.trim(),
                    heightCm = form.heightCm.toDoubleOrNull(),
                    weightKg = form.weightKg.toDoubleOrNull(),
                    activityLevel = form.activityLevel.trim().ifBlank { null },
                    fitnessGoal = form.fitnessGoal.trim().ifBlank { null },
                    targetCaloriesKcal = form.targetCaloriesKcal.toDoubleOrNull(),
                    targetProteinG = form.targetProteinG.toDoubleOrNull(),
                    targetCarbsG = form.targetCarbsG.toDoubleOrNull(),
                    targetFatG = form.targetFatG.toDoubleOrNull(),
                    geminiApiKey = form.geminiApiKey.trim().ifBlank { null },
                    isGeminiOfflineMode = form.isGeminiOfflineMode,
                ),
            )
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }
}
