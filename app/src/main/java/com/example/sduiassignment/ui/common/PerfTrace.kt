package com.example.sduiassignment.ui.common

import android.os.SystemClock
import android.util.Log

/**
 * Minimal cold-start instrumentation for PERF.md: every mark() writes one logcat line with
 * a monotonic elapsedRealtime timestamp, a variant tag (sdui/static), and a label. Marks are
 * read back from `adb logcat -s PerfTrace` and diffed by hand/script - no in-app aggregation,
 * so the tool itself adds negligible overhead to what it's measuring.
 */
object PerfTrace {
    private const val TAG = "PerfTrace"

    fun mark(variant: String, label: String) {
        Log.d(TAG, "$variant|$label|${SystemClock.elapsedRealtime()}")
    }
}
