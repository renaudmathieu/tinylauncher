package dev.tiny.launcher

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog

// Hosting a widget is a protocol in five steps. The widget is not our code:
// another app sends us its views, and we show them.

/** Never change it: every widget id the system gives us is keyed to it. */
const val HOST_ID = 0x71AE

/** One place on the home screen for one widget, or a button to add it. */
@Composable
fun WidgetSlot(host: AppWidgetHost, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val manager = remember { AppWidgetManager.getInstance(context) }

    // The widget on screen, if any. Tiny Launcher does not store it: a real launcher would.
    var widgetId by rememberSaveable { mutableStateOf<Int?>(null) }
    // The id waiting for the user's answer to the bind dialog.
    var pendingId by rememberSaveable { mutableIntStateOf(0) }
    var showPicker by remember { mutableStateOf(false) }

    // The answer to the bind dialog of step 2.
    val bind =
        rememberLauncherForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                widgetId = pendingId
            } else {
                host.deleteAppWidgetId(pendingId)
            }
        }

    fun addWidget(info: AppWidgetProviderInfo) {
        // 1. An id.
        val id = host.allocateAppWidgetId()

        // 2. Binding: attach the id to the widget the user picked.
        if (manager.bindAppWidgetIdIfAllowed(id, info.profile, info.provider, null)) {
            widgetId = id
        } else {
            // Not allowed yet: the system asks the user. It is a permission dialog in disguise.
            pendingId = id
            bind.launch(
                Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, info.profile)
            )
        }

        // 3. Configuration: skipped. The picker only offers widgets that need none.
    }

    val id = widgetId
    if (id == null) {
        TextButton(onClick = { showPicker = true }, modifier = modifier) {
            Text("+ Widget", color = Color.White)
        }
    } else {
        // 4. The view: a plain Android View, so it goes in an AndroidView.
        AndroidView(
            factory = { viewContext ->
                host.createView(viewContext, id, manager.getAppWidgetInfo(id))
            },
            modifier = modifier.fillMaxWidth().height(180.dp),
        )
    }

    // 5. Listening: see onStart and onStop in MainActivity.

    if (showPicker) {
        WidgetPicker(
            onPick = { info ->
                showPicker = false
                addWidget(info)
            },
            onDismiss = { showPicker = false },
        )
    }
}

private val PickerBackground = Color(0xFF2A1D60)

/** A plain list of the widgets installed on the phone. */
@Composable
private fun WidgetPicker(onPick: (AppWidgetProviderInfo) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val widgets = remember {
        AppWidgetManager.getInstance(context)
            .getInstalledProvidersForProfile(Process.myUserHandle())
            .filter { it.configure == null } // no configuration screen to show
            .sortedBy { it.loadLabel(context.packageManager) }
    }

    Dialog(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier =
                Modifier.heightIn(max = 480.dp)
                    .background(PickerBackground, RoundedCornerShape(24.dp))
                    .padding(vertical = 12.dp)
        ) {
            items(widgets, key = { "${it.provider}/${it.profile}" }) { info ->
                Text(
                    text = info.loadLabel(context.packageManager),
                    color = Color.White,
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable { onPick(info) }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}
