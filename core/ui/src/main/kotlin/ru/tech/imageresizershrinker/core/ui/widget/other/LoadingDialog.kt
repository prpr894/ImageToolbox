package ru.tech.imageresizershrinker.core.ui.widget.other

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(canCancel: Boolean = true, onCancelLoading: () -> Unit) {
    var showWantDismissDialog by remember(canCancel) { mutableStateOf(false) }
    BasicAlertDialog(onDismissRequest = { showWantDismissDialog = canCancel }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        showWantDismissDialog = canCancel
                    }
                },
            contentAlignment = Alignment.Center
        ) { Loading(modifier = Modifier.size(108.dp)) }
    }
    WantCancelLoadingDialog(
        visible = showWantDismissDialog,
        onCancelLoading = onCancelLoading,
        onDismissDialog = {
            showWantDismissDialog = false
        }
    )
    KeepScreenOn()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(done: Int, left: Int, canCancel: Boolean = true, onCancelLoading: () -> Unit) {
    var showWantDismissDialog by remember(canCancel) { mutableStateOf(false) }
    BasicAlertDialog(onDismissRequest = { showWantDismissDialog = canCancel }) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        showWantDismissDialog = canCancel
                    }
                }
        ) { Loading(done, left) }
    }
    WantCancelLoadingDialog(
        visible = showWantDismissDialog,
        onCancelLoading = onCancelLoading,
        onDismissDialog = {
            showWantDismissDialog = false
        }
    )
    KeepScreenOn()
}