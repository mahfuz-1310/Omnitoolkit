package com.example.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.NameGenApplication
import com.example.data.db.AppHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppHistoryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val uri = intent.data ?: return
        val packageName = uri.schemeSpecificPart ?: return

        val app = context.applicationContext as? NameGenApplication ?: return
        val dao = app.database.appHistoryDao()

        val pm = context.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
        val timestampStr = sdf.format(Date(now))

        CoroutineScope(Dispatchers.IO).launch {
            val existing = dao.getByPackage(packageName)
            when (action) {
                Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_REPLACED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    if (existing != null) {
                        val newReinstallCount = existing.reinstallCount + 1
                        dao.insertOrUpdate(
                            AppHistoryEntity(
                                packageName = packageName,
                                appName = appName,
                                actionType = if (isReplacing) "Reinstalled" else "Installed",
                                actionTimestamp = timestampStr,
                                installTimeMillis = now,
                                uninstallTimeMillis = 0L,
                                reinstallCount = newReinstallCount
                            )
                        )
                    } else {
                        dao.insertOrUpdate(
                            AppHistoryEntity(
                                packageName = packageName,
                                appName = appName,
                                actionType = "Installed",
                                actionTimestamp = timestampStr,
                                installTimeMillis = now,
                                uninstallTimeMillis = 0L,
                                reinstallCount = 0
                            )
                        )
                    }
                }
                Intent.ACTION_PACKAGE_REMOVED -> {
                    val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                    if (!isReplacing) {
                        val installTime = existing?.installTimeMillis ?: now
                        dao.insertOrUpdate(
                            AppHistoryEntity(
                                packageName = packageName,
                                appName = existing?.appName ?: appName,
                                actionType = "Uninstalled",
                                actionTimestamp = timestampStr,
                                installTimeMillis = installTime,
                                uninstallTimeMillis = now,
                                reinstallCount = existing?.reinstallCount ?: 0
                            )
                        )
                    }
                }
            }
        }
    }
}
