package com.example.flaggameandroid.engagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.flaggameandroid.persistence.AppGraph

class DailyReminderReceiver : BroadcastReceiver() {
  override fun onReceive(
    context: Context,
    intent: Intent,
  ) {
    EngagementDebugLogger.info(
      "DailyReminderReceiver fired at ${EngagementDebugLogger.formatEpoch(System.currentTimeMillis())} " +
        "with action=${intent.action}",
    )
    AppGraph.from(context.applicationContext).engagementCoordinator.onDailyCheckTriggered()
  }
}
