package dev.tiny.launcher

import android.appwidget.AppWidgetHost
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {

    // One event each time Home is pressed while we are already on the home screen.
    private val homePresses = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private lateinit var appWidgetHost: AppWidgetHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        appWidgetHost = AppWidgetHost(this, HOST_ID)
        setContent {
            AskToBeHome()
            TinyHome(homePresses, appWidgetHost)
        }
    }

    override fun onStart() {
        super.onStart()
        // Widget updates only arrive while we listen: listen while the launcher is visible.
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    // singleTask: there is only one instance of this activity, so a Home press
    // never creates a new one. The Home intent is delivered here instead.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Trap: this also runs when Home brings us back from another app.
        val alreadyHome =
            hasWindowFocus() && intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT == 0
        if (alreadyHome && intent.hasCategory(Intent.CATEGORY_HOME)) {
            homePresses.tryEmit(Unit)
        }
    }
}
