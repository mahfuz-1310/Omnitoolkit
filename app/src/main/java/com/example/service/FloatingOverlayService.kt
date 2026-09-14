package com.example.service

import android.content.Context

class FloatingOverlayService : FloatingWindowService() {
    companion object {
        val isRunning: Boolean
            get() = FloatingWindowService.isRunning

        fun stop(context: Context) {
            FloatingWindowService.stop(context)
        }
    }
}
