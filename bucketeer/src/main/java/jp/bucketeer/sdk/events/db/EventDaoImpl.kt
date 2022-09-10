package jp.bucketeer.sdk.events.db

import android.content.ContentValues
import android.database.sqlite.SQLiteOpenHelper
import bucketeer.event.client.EventOuterClass
import jp.bucketeer.sdk.events.EventEntity.Companion.COLUMN_EVENT
import jp.bucketeer.sdk.events.EventEntity.Companion.COLUMN_ID
import jp.bucketeer.sdk.events.EventEntity.Companion.TABLE_NAME
import jp.bucketeer.sdk.events.toEvent
import jp.bucketeer.sdk.ext.asSequence
import jp.bucketeer.sdk.ext.getBlob
import jp.bucketeer.sdk.ext.select

internal class EventDaoImpl(
  private val sqLiteOpenHelper: SQLiteOpenHelper,
) : EventDao {

  override fun addEvent(goalEvent: EventOuterClass.GoalEvent) {
    addEvent(goalEvent.toEvent())
  }

  override fun addEvent(evaluationEvent: EventOuterClass.EvaluationEvent) {
    addEvent(evaluationEvent.toEvent())
  }

  override fun addEvent(metricsEvent: EventOuterClass.MetricsEvent) {
    addEvent(metricsEvent.toEvent())
  }

  private fun addEvent(event: EventOuterClass.Event) {
    val contentValues = ContentValues().apply {
      put(COLUMN_ID, event.id)
      put(COLUMN_EVENT, event.toByteArray())
    }

    sqLiteOpenHelper.writableDatabase.insert(TABLE_NAME, null, contentValues)
  }

  override fun getEvents(): List<EventOuterClass.Event> {
    val c = sqLiteOpenHelper.readableDatabase.select(
      table = TABLE_NAME,
    )

    return c.use {
      c.asSequence()
        .map { EventOuterClass.Event.parseFrom(it.getBlob(COLUMN_EVENT)) }
        .toList()
    }
  }

  override fun delete(ids: List<String>) {
    val valuesIn = List(ids.count(), { "?" }).joinToString(separator = ",")
    val whereArgs = ids.toTypedArray()
    sqLiteOpenHelper.writableDatabase.delete(
      TABLE_NAME,
      "$COLUMN_ID IN ($valuesIn)",
      whereArgs,
    )
  }
}
