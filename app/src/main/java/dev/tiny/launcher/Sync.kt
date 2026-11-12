package dev.tiny.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import androidx.core.content.ContextCompat
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

// The system tells us when something changes through callbacks from 2008.
// callbackFlow turns each of them into a Flow: register when someone collects,
// unregister in awaitClose when they stop.

/** The current time, emitted again every time the clock on screen must change. */
fun Context.clockTicks(): Flow<LocalTime> = callbackFlow {
    trySend(LocalTime.now())

    val receiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(LocalTime.now())
            }
        }
    val filter =
        IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK) // every minute, never sent to a manifest receiver
            addAction(Intent.ACTION_TIME_CHANGED) // the user or the network changed the clock
            addAction(Intent.ACTION_TIMEZONE_CHANGED) // you just landed in Tokyo
        }
    ContextCompat.registerReceiver(
        this@clockTicks,
        receiver,
        filter,
        ContextCompat.RECEIVER_NOT_EXPORTED,
    )

    awaitClose { unregisterReceiver(receiver) }
}

/** One event each time an app is installed, removed or changed, in any profile. */
fun Context.packageChanges(): Flow<Unit> = callbackFlow {
    val launcherApps = getSystemService(LauncherApps::class.java)

    val callback =
        object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            // Available and unavailable: apps on an SD card, a work profile being paused.
            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                trySend(Unit)
            }
        }
    launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))

    awaitClose { launcherApps.unregisterCallback(callback) }
}

/** The list of apps, loaded once at the start and again after every package change. */
fun Context.installedApps(): Flow<List<App>> =
    packageChanges()
        .onStart { emit(Unit) }
        .map { loadApps() }
        .flowOn(Dispatchers.IO) // loading the list is slow: keep it off the main thread
