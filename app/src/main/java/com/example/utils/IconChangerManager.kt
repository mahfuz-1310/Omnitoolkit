package com.example.utils

import android.content.Context

/**
 * Backward-compatibility wrapper for IconManager.
 */
object IconChangerManager {
    val variants = IconManager.variants

    fun applyIcon(context: Context, targetAlias: String) {
        IconManager.applyIcon(context, targetAlias)
    }
}
