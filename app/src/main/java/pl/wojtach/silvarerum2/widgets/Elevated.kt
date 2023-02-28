@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package pl.wojtach.silvarerum2.widgets

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Elevated(modifier: Modifier = Modifier, elevatedBy: Dp = 50.dp, content: @Composable () -> Unit) {
    Surface(tonalElevation = elevatedBy, modifier = modifier) {
        content()
    }
}

@Composable
fun ElevatedTopBar(modifier: Modifier = Modifier, elevatedBy: Dp = 50.dp, content: @Composable () -> Unit) {
    Elevated(modifier, elevatedBy) {
        TopAppBar(
            modifier = modifier,
            title = {
                content()
            })
    }
}