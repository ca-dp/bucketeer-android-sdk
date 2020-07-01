package jp.bucketeer.sdk

interface ScheduledTask {
  var isStarted: Boolean
  fun start()
  fun stop()
}
