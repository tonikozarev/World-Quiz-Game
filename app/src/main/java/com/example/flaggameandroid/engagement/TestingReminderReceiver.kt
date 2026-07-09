package com.example.flaggameandroid.engagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.flaggameandroid.persistence.AppGraph

class TestingReminderReceiver : BroadcastReceiver() {
  override fun onReceive(
    context: Context,
    intent: Intent,
  ) {
    EngagementDebugLogger.initialize(context.applicationContext)
    EngagementDebugLogger.info(
      "TestingReminderReceiver fired at ${EngagementDebugLogger.formatEpoch(System.currentTimeMillis())}.",
    )
    AppGraph.from(context.applicationContext)
      .engagementCoordinator
      .triggerTestingReminderNotification()
  }
}
