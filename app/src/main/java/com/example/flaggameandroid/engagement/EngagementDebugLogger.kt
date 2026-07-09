package com.example.flaggameandroid.engagement

import android.content.Context
import android.util.Log
import com.example.flaggameandroid.feature.app.FlagGameDebugConfig
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object EngagementDebugLogger {
  private const val Tag = "FlagEngagement"
  private const val MaxLogBytes: Long = 256 * 1024
  private val timestampFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault())
  @Volatile private var logFile: File? = null

  fun initialize(context: Context) {
    if (!FlagGameDebugConfig.EnableEngagementLogging) return
    if (logFile != null) return
    val root =
      context.getExternalFilesDir(null)
        ?: context.filesDir
    val diagnosticsDir = File(root, "diagnostics").apply { mkdirs() }
    logFile = File(diagnosticsDir, "engagement_log.txt")
    info("Persistent engagement logging initialized at ${logFile?.absolutePath}.")
  }

  fun debug(message: String) {
    write("DEBUG", message) { Log.d(Tag, message) }
  }

  fun info(message: String) {
    write("INFO", message) { Log.i(Tag, message) }
  }

  fun warn(message: String) {
    write("WARN", message) { Log.w(Tag, message) }
  }

  fun error(
    message: String,
    throwable: Throwable? = null,
  ) {
    write("ERROR", "$message${throwable?.let { " | ${it.message}" }.orEmpty()}") {
      Log.e(Tag, message, throwable)
    }
  }

  fun formatEpoch(epochMillis: Long): String = timestampFormatter.format(Instant.ofEpochMilli(epochMillis))

  fun logFilePath(): String? = logFile?.absolutePath

  private inline fun write(
    level: String,
    message: String,
    block: () -> Unit,
  ) {
    if (!FlagGameDebugConfig.EnableEngagementLogging) return
    runCatching(block).getOrElse {
      println("$Tag: logging unavailable in this runtime")
    }
    appendToFile(level, message)
  }

  @Synchronized
  private fun appendToFile(
    level: String,
    message: String,
  ) {
    val file = logFile ?: return
    runCatching {
      rotateIfNeeded(file)
      file.appendText("${formatEpoch(System.currentTimeMillis())} [$level] $message\n")
    }
  }

  private fun rotateIfNeeded(file: File) {
    if (!file.exists()) return
    if (file.length() < MaxLogBytes) return

    val backup = File(file.parentFile, "engagement_log_previous.txt")
    if (backup.exists()) {
      backup.delete()
    }
    file.copyTo(backup, overwrite = true)
    file.writeText("")
  }
}
