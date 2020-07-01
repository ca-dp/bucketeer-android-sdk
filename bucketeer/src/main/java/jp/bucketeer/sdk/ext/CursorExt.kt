package jp.bucketeer.sdk.ext

import android.database.Cursor

internal fun Cursor.asSequence(): Sequence<Cursor> {
  return generateSequence(seed = takeIf { it.moveToFirst() }) { takeIf { it.moveToNext() } }
}

// TODO add Cursor.get(Int, Long, Double, ...)
internal fun Cursor.getString(name: String): String {
  return getString(getColumnIndex(name)) ?: throw IllegalStateException()
}

internal fun Cursor.getBlob(name: String): ByteArray {
  return getBlob(getColumnIndex(name)) ?: throw IllegalStateException()
}

internal fun Cursor.getInt(name: String): Int {
  return getInt(getColumnIndex(name))
}

