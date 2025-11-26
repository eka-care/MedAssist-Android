package com.eka.medassist.ui.chat.logger

import android.util.Log

object MedAssistLogger {
    private var enableDebugLogs = false

    fun changeLogsVisibility(debugMode : Boolean) {
        enableDebugLogs = debugMode
    }

    fun d(tag: String, msg: String) {
        if (enableDebugLogs) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String, e: Exception? = null) {
        if (enableDebugLogs) {
            Log.e(tag, msg, e)
        }
    }

    fun w(tag: String, msg: String) {
        if (enableDebugLogs) {
            Log.w(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (enableDebugLogs) {
            Log.i(tag, msg)
        }
    }
}