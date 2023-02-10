package com.ramitsuri.expensereports.android.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlin.system.exitProcess

fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun Context.shutdown() {
    val activity = getActivity()
    val intent =
        activity?.packageManager?.getLaunchIntentForPackage(activity.packageName)
    activity?.finishAffinity()
    activity?.startActivity(intent)
    exitProcess(0)
}