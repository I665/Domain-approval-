package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Domain
import com.example.data.model.Appraisal
import com.example.data.repository.DomainRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ValuationUiState {
    object Idle : ValuationUiState
    object Loading : ValuationUiState
    data class Success(val appraisal: Appraisal) : ValuationUiState
    data class Error(val message: String) : ValuationUiState
}

class DomainViewModel(private val repository: DomainRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _valuationState = MutableStateFlow<ValuationUiState>(ValuationUiState.Idle)
    val valuationState: StateFlow<ValuationUiState> = _valuationState.asStateFlow()

    // Combined catalog flow of domains based on Category, Search Query, and Favorites filters
    val filteredDomains: StateFlow<List<Domain>> = combine(
        repository.allDomains,
        _selectedCategory,
        _searchQuery,
        _showFavoritesOnly
    ) { domains, category, query, favsOnly ->
        domains.filter { domain ->
            val matchesCategory = category == "الكل" || domain.category == category
            val matchesQuery = domain.name.contains(query, ignoreCase = true) || 
                               domain.description.contains(query, ignoreCase = true)
            val matchesFavs = !favsOnly || domain.isFavorite
            matchesCategory && matchesQuery && matchesFavs
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appraisalHistory: StateFlow<List<Appraisal>> = repository.appraisalHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun toggleFavorite(domain: Domain) {
        viewModelScope.launch {
            repository.toggleFavorite(domain)
        }
    }

    fun listNewDomain(name: String, price: Double, category: String, description: String) {
        viewModelScope.launch {
            val newDomain = Domain(
                name = name.trim().lowercase(),
                price = price,
                category = category,
                description = description.trim(),
                isPremium = price >= 5000.0,
                isUserAdded = true
            )
            repository.insertDomain(newDomain)
        }
    }

    fun deleteDomain(domain: Domain) {
        viewModelScope.launch {
            repository.deleteDomain(domain)
        }
    }

    fun evaluateDomain(domainName: String) {
        if (domainName.trim().isEmpty()) {
            _valuationState.value = ValuationUiState.Error("يرجى إدخال اسم دومين صالح للتقييم")
            return
        }

        viewModelScope.launch {
            _valuationState.value = ValuationUiState.Loading
            repository.evaluateDomainWithGemini(domainName)
                .onSuccess { appraisal ->
                    _valuationState.value = ValuationUiState.Success(appraisal)
                }
                .onFailure { exception ->
                    _valuationState.value = ValuationUiState.Error(
                        exception.message ?: "حدث خطأ غير معروف أثناء تقييم الدومين"
                    )
                }
        }
    }

    fun deleteAppraisalHistory(appraisal: Appraisal) {
        viewModelScope.launch {
            repository.deleteAppraisal(appraisal)
        }
    }

    fun clearAppraisalHistory() {
        viewModelScope.launch {
            repository.clearAppraisalHistory()
        }
    }

    fun clearValuationState() {
        _valuationState.value = ValuationUiState.Idle
    }

    class Factory(private val repository: DomainRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DomainViewModel::class.java)) {
                return DomainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
