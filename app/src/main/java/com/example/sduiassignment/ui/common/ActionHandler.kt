package com.example.sduiassignment.ui.common

import android.content.Context
import android.util.Log
import com.example.sduiassignment.data.model.ActionRef

/**
 * Stub navigation/operation dispatcher. This assignment focuses on rendering the SDUI
 * contract; a real app would route [ActionRef] to a Navigation graph / deep-link handler
 * instead of logging it.
 */
object ActionHandler {

    private const val TAG = "ActionHandler"

    fun handle(context: Context, action: ActionRef?, fallbackLabel: String? = null) {
        if (action == null) return
        val message = when (action.type) {
            "ACTION_TYPE_DISCOVER_PAGE" ->
                "Navigate: ${action.discoverPage?.page ?: action.discoverPage?.navTarget ?: fallbackLabel}"
            "ACTION_TYPE_DISCOVER_WIDGETS" ->
                "Refresh widget: ${action.discoverWidget?.widgetId ?: fallbackLabel}"
            "ACTION_TYPE_OPERATION" ->
                "Operation: ${action.operation?.type ?: fallbackLabel}"
            else -> fallbackLabel ?: "Tapped"
        }
        Log.d(TAG, message)
    }
}
