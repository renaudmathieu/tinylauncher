package dev.tiny.launcher

import android.appwidget.AppWidgetHost
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow

private val clockFormat = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun TinyHome(homePresses: Flow<Unit>, widgetHost: AppWidgetHost) {
    val context = LocalContext.current

    // Collected with the lifecycle: nothing stays registered while the launcher is hidden.
    val now by remember { context.clockTicks() }.collectAsStateWithLifecycle(LocalTime.now())
    val apps by remember { context.installedApps() }.collectAsStateWithLifecycle(emptyList())

    var iconStyle by rememberSaveable { mutableStateOf(IconStyle.Circle) }

    // Home is the root of the phone: Back has nowhere to go.
    BackHandler {}

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Clock(now, homePresses, Modifier.padding(vertical = 32.dp))
        WidgetSlot(widgetHost)
        TextButton(onClick = { iconStyle = iconStyle.next() }) {
            Text("Icons: ${iconStyle.name}", color = Color.White)
        }
        AppDrawer(apps, iconStyle, Modifier.fillMaxWidth().weight(1f))
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
