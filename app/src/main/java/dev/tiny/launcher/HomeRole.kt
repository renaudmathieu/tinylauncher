package dev.tiny.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

/** Are we the app that opens when the user presses Home? */
fun Context.isDefaultHome(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Since Android 10, "home" is a role, like default SMS or default dialer.
        getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_HOME) == true
    } else {
        // Before Android 10: resolve the Home intent ourselves and look at who answers.
        val resolved =
            packageManager.resolveActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        resolved?.activityInfo?.packageName == packageName
    }

/** The system sheet that offers the home role. It only exists since Android 10. */
fun Context.createHomeRoleRequestIntent(): Intent? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        getSystemService(RoleManager::class.java)?.createRequestRoleIntent(RoleManager.ROLE_HOME)
    } else {
        null
    }

/** Asks the user to make Tiny Launcher the home app, if it is not already. */
@Composable
fun AskToBeHome() {
    val context = LocalContext.current
    // The role sheet must be started for a result, even though we ignore the result:
    // there is no listener for the role, isDefaultHome() is the only source of truth.
    val askUser = rememberLauncherForActivityResult(StartActivityForResult()) {}

    LaunchedEffect(Unit) {
        if (!context.isDefaultHome()) {
            // No sheet before Android 10: send the user to the home app settings.
            val request =
                context.createHomeRoleRequestIntent() ?: Intent(Settings.ACTION_HOME_SETTINGS)
            askUser.launch(request)
        }
    }
}
