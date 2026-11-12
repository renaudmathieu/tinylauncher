package dev.tiny.launcher

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow

private val clockFormat = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun TinyHome(homePresses: Flow<Unit>) {
    val context = LocalContext.current

    // Known bug, on purpose: the time is read once, so the clock is frozen. Fixed in cp-3.
    val now = remember { LocalTime.now() }
    // Known bug, on purpose: the apps are loaded once, so a new app never shows up. Fixed in cp-3.
    val apps = remember { context.loadApps() }

    // Home is the root of the phone: Back has nowhere to go.
    BackHandler {}

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Clock(now, homePresses, Modifier.padding(vertical = 48.dp))
        AppDrawer(apps, Modifier.fillMaxWidth().weight(1f))
    }
}

/** A big clock that bounces each time Home is pressed while we are already home. */
@Composable
fun Clock(time: LocalTime, homePresses: Flow<Unit>, modifier: Modifier = Modifier) {
    val bounce = remember { Animatable(1f) }
    LaunchedEffect(homePresses) {
        homePresses.collect {
            bounce.animateTo(1.2f)
            bounce.animateTo(1f)
        }
    }

    Text(
        text = time.format(clockFormat),
        fontSize = 96.sp,
        color = Color.White,
        modifier = modifier.scale(bounce.value),
    )
}
