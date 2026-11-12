package dev.tiny.launcher

import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

/** The app ships layers, the launcher (so, the user) picks the mask. */
enum class IconStyle(val shape: Shape, val themed: Boolean = false) {
    Circle(CircleShape),
    Squircle(RoundedCornerShape(30)),
    Rounded(RoundedCornerShape(12)),
    Themed(CircleShape, themed = true);

    /** The style after this one, back to the first one after the last. */
    fun next(): IconStyle = entries[(ordinal + 1) % entries.size]
}

// An adaptive icon layer is 108 dp wide, but only the middle 72 dp is meant to be seen.
// Drawn 1.5 times bigger than the icon, a layer shows exactly those 72 dp through the mask.
private const val LAYER_SCALE = 108f / 72f

private val DarkBackground = Color(0xFF2A1D60)

/**
 * Draws [icon] behind the mask [shape]. With a [themeColor], the icon is themed when the app ships
 * a monochrome layer.
 */
@Composable
fun AppIcon(icon: Drawable, shape: Shape, themeColor: Color? = null, size: Dp = 56.dp) {
    val monochrome = icon.monochromeLayer()

    Box(Modifier.size(size).clip(shape)) {
        if (themeColor != null && monochrome != null) {
            // Themed: a flat background, and the monochrome layer painted in one color.
            // The background is dark or light, whichever the color stands out on.
            val background = if (themeColor.luminance() > 0.5f) DarkBackground else Color.White
            Box(Modifier.fillMaxSize().background(background))
            IconLayer(monochrome, size, tint = themeColor)
        } else if (icon is AdaptiveIconDrawable) {
            // Adaptive: the background layer, then the foreground layer on top.
            listOfNotNull(icon.background, icon.foreground).forEach { layer ->
                IconLayer(layer, size)
            }
        } else {
            // Legacy: one picture, no layers. Drawn as it is, slightly smaller.
            val px = with(LocalDensity.current) { size.roundToPx() }
            val bitmap = remember(icon, px) { icon.toBitmap(px, px).asImageBitmap() }
            Image(bitmap, contentDescription = null, Modifier.fillMaxSize().padding(size / 8))
        }
    }
}

@Composable
private fun IconLayer(layer: Drawable, iconSize: Dp, tint: Color? = null) {
    // Rasterized at the size it is drawn, so the icon stays sharp once scaled.
    val px = with(LocalDensity.current) { (iconSize * LAYER_SCALE).roundToPx() }
    // Remembered: without it, every recomposition would rasterize every icon again.
    val bitmap = remember(layer, px) { layer.toBitmap(px, px).asImageBitmap() }

    Image(
        bitmap = bitmap,
        contentDescription = null,
        colorFilter = tint?.let { ColorFilter.tint(it) },
        modifier = Modifier.fillMaxSize().scale(LAYER_SCALE),
    )
}

/** The single-color layer made for themed icons. Many apps still ship none. */
private fun Drawable.monochromeLayer(): Drawable? =
    if (Build.VERSION.SDK_INT >= 33 && this is AdaptiveIconDrawable) monochrome else null

/** We cannot read the wallpaper picture, but we can read its colors. */
fun Context.wallpaperColor(): Color? =
    if (Build.VERSION.SDK_INT >= 27) {
        WallpaperManager.getInstance(this)
            .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            ?.primaryColor
            ?.toArgb()
            ?.let(::Color)
    } else {
        null
    }
