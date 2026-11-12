package dev.tiny.launcher

import android.graphics.Rect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(apps: List<App>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = modifier) {
        // The same app in two profiles is two items: the key needs the user too.
        items(apps, key = { "${it.component}/${it.user}" }) { app ->
            AppItem(app)
        }
    }
}

@Composable
private fun AppItem(app: App) {
    val context = LocalContext.current
    // Where this item is on screen, for the launch animation.
    var bounds by remember { mutableStateOf<Rect?>(null) }

    Text(
        text = app.label,
        maxLines = 1,
        color = Color.White,
        modifier =
            Modifier.onGloballyPositioned { bounds = it.boundsInWindow().toAndroidRect() }
                .clickable { context.launch(app, bounds) }
                .padding(12.dp),
    )
}
