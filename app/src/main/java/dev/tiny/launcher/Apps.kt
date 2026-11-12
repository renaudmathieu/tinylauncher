package dev.tiny.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.UserHandle
import android.os.UserManager
import java.text.Collator

/**
 * One entry of the drawer. An "app" is a launchable activity, not a package: a package can have
 * zero, one or several of them, and one per user profile.
 */
data class App(
    val label: String,
    val component: ComponentName,
    val user: UserHandle,
    val info: LauncherActivityInfo, // kept for its icon, see cp-4
)

/** Every launchable activity, from every profile (personal, work, private space). */
fun Context.loadApps(): List<App> {
    val launcherApps = getSystemService(LauncherApps::class.java)
    val userManager = getSystemService(UserManager::class.java)
    // Collator sorts the way people read: "Élan" does not end up after "Zoom".
    val collator = Collator.getInstance()
    return userManager.userProfiles
        .flatMap { user ->
            launcherApps.getActivityList(null, user).map {
                App(it.label.toString(), it.componentName, user, it)
            }
        }
        .sortedWith(compareBy(collator) { it.label })
}

/**
 * Opens the app in its own profile. [bounds] is the rectangle of the item on screen: the system
 * grows the app out of it.
 */
fun Context.launch(app: App, bounds: Rect?) {
    getSystemService(LauncherApps::class.java)
        .startMainActivity(app.component, app.user, bounds, null)
}
