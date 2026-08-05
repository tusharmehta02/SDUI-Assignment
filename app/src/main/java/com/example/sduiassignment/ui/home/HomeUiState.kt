package com.example.sduiassignment.ui.home

import com.example.sduiassignment.data.repository.HomeWidgets

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val widgets: HomeWidgets) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
