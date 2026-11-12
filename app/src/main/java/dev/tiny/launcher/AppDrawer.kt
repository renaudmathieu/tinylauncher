package dev.tiny.launcher

import android.graphics.Rect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppDrawer(apps: List<App>, iconStyle: IconStyle, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Themed icons are painted with the main color of the wallpaper.
    val wallpaperColor = remember { context.wallpaperColor() } ?: MaterialTheme.colorScheme.primary
    val themeColor = if (iconStyle.themed) wallpaperColor else null

    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = modifier) {
        // The same app in two profiles is two items: the key needs the user too.
        items(apps, key = { "${it.component}/${it.user}" }) { app ->
            AppItem(app, iconStyle.shape, themeColor)
        }
    }
}

@Composable
private fun AppItem(app: App, iconShape: Shape, themeColor: Color?) {
    val context = LocalContext.current
    // Where this item is on screen, for the launch animation.
    var bounds by remember { mutableStateOf<Rect?>(null) }
    // 0 means "the density of this screen".
    val icon = remember(app) { app.info.getIcon(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.onGloballyPositioned { bounds = it.boundsInWindow().toAndroidRect() }
                .clickable { context.launch(app, bounds) }
                .padding(8.dp),
    ) {
        AppIcon(icon, iconShape, themeColor)
        Text(
            text = app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
