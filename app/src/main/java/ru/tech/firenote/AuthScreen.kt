package ru.tech.firenote

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(visible: MutableState<Boolean>, viewModel: AuthViewModel = viewModel()) {

    visible.value = viewModel.visibleState.targetState
    AnimatedVisibility(visibleState = viewModel.visibleState, enter = fadeIn(), exit = fadeOut()) {
        when (viewModel.currentScreen.value) {
            Screen.LoginScreen.route -> LoginScreen(viewModel)
            Screen.RegistrationScreen.route -> RegistrationScreen(viewModel)
            Screen.ForgotPasswordScreen.route -> ForgotPasswordScreen(viewModel)
        }
    }


}