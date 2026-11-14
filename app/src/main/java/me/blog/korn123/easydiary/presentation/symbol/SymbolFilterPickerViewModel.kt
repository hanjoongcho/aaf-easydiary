package me.blog.korn123.easydiary.presentation.symbol

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.domain.model.DiarySymbol
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getUncheckedSymbolItem
import me.blog.korn123.easydiary.helper.SYMBOL_EASTER_EGG
import me.blog.korn123.easydiary.helper.SYMBOL_USER_CUSTOM_START
import javax.inject.Inject

data class SymbolCategory(
    val categoryName: String,
    val symbols: List<DiarySymbol>,
)

data class SymbolFilterPickerUiState(
    val selectedSymbols: List<Int> = emptyList(),
    val categories: List<SymbolCategory> = emptyList(),
    val alertDialogMessage: String? = null,
    val scrollToBottomEvent: Boolean = false,
)

@HiltViewModel
class SymbolFilterPickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SymbolFilterPickerUiState())
    val uiState: StateFlow<SymbolFilterPickerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val selected = parseSelectedSymbols(context.config.selectedSymbols)
        val categories = loadCategories(context)
        _uiState.update {
            it.copy(
                selectedSymbols = selected,
                categories = categories,
            )
        }
    }

    fun removeSymbol(sequence: Int) {
        val currentSelected = _uiState.value.selectedSymbols
        val filteredList = currentSelected.filter { it != sequence }
        if (filteredList.isNotEmpty()) {
            _uiState.update { it.copy(selectedSymbols = filteredList) }
        } else {
            _uiState.update {
                it.copy(
                    alertDialogMessage = context.getString(R.string.symbol_filter_picker_remove_guide_message),
                )
            }
        }
    }

    fun addSymbol(symbolSequence: Int) {
        if (symbolSequence <= 0) return
        val currentSelected = _uiState.value.selectedSymbols
        if (currentSelected.contains(symbolSequence)) {
            _uiState.update {
                it.copy(
                    alertDialogMessage = "The selected symbol already exists in the filter list.",
                )
            }
        } else {
            val updatedList = currentSelected + symbolSequence
            _uiState.update {
                it.copy(
                    selectedSymbols = updatedList,
                    scrollToBottomEvent = true,
                )
            }
        }
    }

    fun saveSelectedSymbols() {
        context.config.selectedSymbols = _uiState.value.selectedSymbols.joinToString(",")
    }

    fun onScrollToBottomHandled() {
        _uiState.update { it.copy(scrollToBottomEvent = false) }
    }

    fun dismissAlertDialog() {
        _uiState.update { it.copy(alertDialogMessage = null) }
    }

    private fun parseSelectedSymbols(selectedSymbolsString: String): List<Int> {
        if (selectedSymbolsString.isBlank()) return emptyList()
        return selectedSymbolsString.split(",")
            .mapNotNull { it.toIntOrNull() }
    }

    private fun loadCategories(ctx: Context): List<SymbolCategory> {
        val categoryList = mutableListOf<SymbolCategory>()

        fun addCategory(resourceName: String, categoryNameRes: Int) {
            val resourceId = ctx.resources.getIdentifier(resourceName, "array", ctx.packageName)
            if (resourceId != 0) {
                val stringArray = ctx.resources.getStringArray(resourceId)
                val items = if (resourceName == "weather_item_array") {
                    stringArray
                } else {
                    arrayOf(ctx.getUncheckedSymbolItem(), *stringArray)
                }
                val diarySymbols = items.map { DiarySymbol(it) }
                categoryList.add(SymbolCategory(ctx.getString(categoryNameRes), diarySymbols))
            }
        }

        addCategory("weather_item_array", R.string.category_weather)
        addCategory("emotion_item_array", R.string.category_emotion)
        addCategory("daily_item_array", R.string.category_daily)
        addCategory("tasks_item_array", R.string.category_tasks)
        addCategory("food_item_array", R.string.category_food)
        addCategory("leisure_item_array", R.string.category_leisure)
        addCategory("landscape_item_array", R.string.category_landscape)
        addCategory("symbol_item_array", R.string.category_symbol)
        addCategory("flag_item_array", R.string.category_flag)

        if (ctx.config.enableDebugMode) {
            val customSymbols = mutableListOf<String>()
            var customSymbolSequence = SYMBOL_USER_CUSTOM_START
            ctx.config.customSymbolPaths.forEach { _ ->
                customSymbols.add("$customSymbolSequence|${customSymbolSequence++}")
            }
            val items = arrayOf(
                ctx.getUncheckedSymbolItem(),
                "$SYMBOL_EASTER_EGG|Easter Egg",
                *customSymbols.toTypedArray(),
            )
            categoryList.add(SymbolCategory("Custom", items.map { DiarySymbol(it) }))
        }

        return categoryList
    }
}
