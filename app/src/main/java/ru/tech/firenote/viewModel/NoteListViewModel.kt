package ru.tech.firenote.viewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.tech.firenote.model.Note
import ru.tech.firenote.repository.NoteRepository
import ru.tech.firenote.ui.composable.provider.showSnackbar
import ru.tech.firenote.ui.state.UIState
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.Empty())
    val uiState: StateFlow<UIState> = _uiState


    init {
        getNotes()
    }

    private fun getNotes() {
        viewModelScope.launch {
            _uiState.value = UIState.Loading

            while (repository.auth.currentUser == null) {
                delay(200)
            }

            repository.getNotes().collect {
                if (it.isSuccess) {
                    if (it.getOrNull().isNullOrEmpty()) _uiState.value = UIState.Empty()
                    else _uiState.value = UIState.Success(it.getOrNull())
                } else {
                    _uiState.value = UIState.Empty(it.exceptionOrNull()?.localizedMessage)
                }
            }
        }
    }

    fun deleteNote(
        note: Note,
        scope: CoroutineScope,
        host: SnackbarHostState,
        message: String,
        action: String
    ) {
        viewModelScope.launch {
            repository.deleteNote(note)
            var messageNew = message.replace("*", note.title.toString()).take(30)
            if (note.title.toString().length > 30) messageNew += "..."
            showSnackbar(
                scope,
                host,
                messageNew,
                action
            ) {
                if (it == SnackbarResult.ActionPerformed) {
                    viewModelScope.launch { repository.insertNote(note) }
                }
            }
        }
    }

}