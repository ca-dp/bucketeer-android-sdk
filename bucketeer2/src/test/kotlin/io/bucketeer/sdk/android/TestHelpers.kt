package io.bucketeer.sdk.android

import android.content.Context
import io.bucketeer.sdk.android.internal.Constants
import io.bucketeer.sdk.android.internal.database.OpenHelperCallback

internal fun deleteDatabase(context: Context) {
  context.deleteDatabase(OpenHelperCallback.FILE_NAME)
}

internal fun deleteSharedPreferences(context: Context) {
  context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
    .edit()
    .clear()
    .commit()
}
