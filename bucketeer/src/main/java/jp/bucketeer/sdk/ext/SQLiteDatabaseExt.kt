package jp.bucketeer.sdk.ext

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

inline fun <T> SQLiteDatabase.transaction(
    exclusive: Boolean = true,
    block: SQLiteDatabase.() -> T
): T {
  if (exclusive) {
    beginTransaction()
  } else {
    beginTransactionNonExclusive()
  }
  try {
    val result = block()
    setTransactionSuccessful()
    return result
  } finally {
    endTransaction()
  }
}

fun SQLiteDatabase.select(
    table: String,
    columns: Array<String>? = null,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    groupBy: String? = null,
    having: String? = null,
    orderBy: String? = null,
    limit: String? = null
): Cursor {
  return query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
}
