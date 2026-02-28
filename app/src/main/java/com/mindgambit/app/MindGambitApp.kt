package com.mindgambit.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// ============================================================
// MindGambit — Application Class
// @HiltAndroidApp triggers Hilt's code generation and sets up
// the application-level dependency injection component.
// ============================================================
@HiltAndroidApp
class MindGambitApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Future: init crash reporting, logging, etc.
    }
}
