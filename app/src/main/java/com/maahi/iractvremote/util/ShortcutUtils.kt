package com.maahi.iractvremote.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.maahi.iractvremote.MainActivity
import com.maahi.iractvremote.R
import com.maahi.iractvremote.data.SavedRemote
import com.maahi.iractvremote.model.ApplianceType

/**
 * Helper utility to pin Android launcher shortcuts directly for specific AC or TV remotes.
 */
object ShortcutUtils {

    fun pinRemoteShortcut(context: Context, remote: SavedRemote) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, "Pinned shortcuts are not supported on your launcher", Toast.LENGTH_SHORT).show()
            return
        }

        val isTv = remote.applianceType == ApplianceType.TV
        val iconRes = if (isTv) R.drawable.ic_tv_shortcut else R.drawable.ic_ac_shortcut

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("shortcut_remote_id", remote.id)
            putExtra("shortcut_remote_type", remote.applianceType.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcut = ShortcutInfoCompat.Builder(context, "shortcut_remote_${remote.id}")
            .setShortLabel(remote.name)
            .setLongLabel("${remote.name} (${remote.room})")
            .setIcon(IconCompat.createWithResource(context, iconRes))
            .setIntent(launchIntent)
            .build()

        val success = ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        if (success) {
            Toast.makeText(context, "Added '${remote.name}' to Home Screen!", Toast.LENGTH_SHORT).show()
        }
    }
}
