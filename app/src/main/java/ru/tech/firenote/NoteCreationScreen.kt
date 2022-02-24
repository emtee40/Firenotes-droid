package ru.tech.firenote

import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.insets.navigationBarsPadding
import kotlinx.coroutines.launch
import ru.tech.firenote.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCreationScreen(
    state: MutableTransitionState<Boolean>,
    noteColor: Int = -1,
    appBarColor: Int = -1,
    viewModel: NoteCreationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val noteBackgroundAnimatable = remember {
        Animatable(
            Color(if (noteColor != -1) noteColor else viewModel.noteColor.value)
        )
    }
    val appBarAnimatable = remember {
        Animatable(
            Color(if (appBarColor != -1) appBarColor else viewModel.appBarColor.value)
        )
    }

    val gradientColor = rememberSaveable { mutableStateOf(noteBackgroundAnimatable.value.toArgb()) }

    if (noteColor != -1) viewModel.setColors()

    Scaffold(
        topBar = {
            EditableAppBar(
                modifier = Modifier.background(appBarAnimatable.value),
                navigationIcon = {
                    IconButton(onClick = {
                        state.targetState = false
                    }) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = Color.Black)
                    }
                },
                hint = stringResource(R.string.enterNoteLabel),
                errorColor = viewModel.errorColor,
                color = Color.Black
            ) {
                viewModel.noteLabel = it
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.navigationBarsPadding(),
                text = { Text(stringResource(R.string.save)) },
                icon = { Icon(Icons.Outlined.Save, null) },
                onClick = {
                    if (viewModel.noteDescription.isNotBlank() && viewModel.noteLabel.isNotEmpty()) {
                        viewModel.saveNote()
                        state.targetState = false
                    } else Toast.makeText(context, R.string.fillAll, Toast.LENGTH_SHORT).show()
                })
        }
    ) { contentPadding ->
        Column(
            Modifier
                .background(noteBackgroundAnimatable.value)
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(contentPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
            ) {
                viewModel.colors.forEachIndexed { index, color ->
                    val colorInt = color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .padding(horizontal = 10.dp)
                            .shadow(15.dp, CircleShape)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = 3.dp,
                                color = if (viewModel.noteColor.value == colorInt) {
                                    Color.Black
                                } else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                gradientColor.value = Color.Transparent.toArgb()
                                scope.launch {
                                    noteBackgroundAnimatable.animateTo(
                                        targetValue = Color(colorInt),
                                        animationSpec = tween(
                                            durationMillis = 500
                                        )
                                    )
                                    gradientColor.value = colorInt
                                }
                                scope.launch {
                                    appBarAnimatable.animateTo(
                                        targetValue = viewModel.darkColors[index],
                                        animationSpec = tween(
                                            durationMillis = 500
                                        )
                                    )
                                }
                                viewModel.noteColor.value = colorInt
                                viewModel.appBarColor.value =
                                    viewModel.darkColors[index].toArgb()
                                viewModel.setColors()
                            }
                    )
                }
            }
            Box(modifier = Modifier.wrapContentHeight()) {
                MaterialTextField(
                    topPadding = 20.dp,
                    endPaddingIcon = 20.dp,
                    hintText = stringResource(R.string.noteText),
                    errorColor = viewModel.errorColor,
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .padding(
                            start = 30.dp,
                            end = 30.dp,
                        ),
                    color = Color.Black
                ) {
                    viewModel.noteDescription = it
                }
                Gradient(
                    Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    Color(gradientColor.value),
                    Color.Transparent
                )
                Gradient(
                    Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .align(Alignment.BottomCenter),
                    Color.Transparent,
                    Color(gradientColor.value)
                )
            }
        }
    }
}

fun NoteCreationViewModel.setColors() {
    errorColor.value = when (noteColor.value) {
        NoteYellow.toArgb() -> YellowError
        NoteGreen.toArgb() -> GreenError
        NoteBlue.toArgb() -> BlueError
        NoteViolet.toArgb() -> VioletError
        NotePink.toArgb() -> PinkError
        else -> Color(0)
    }
}