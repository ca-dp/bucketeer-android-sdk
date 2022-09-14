package io.bucketeer.sdk.android

import android.content.Context
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.internal.Constants
import io.bucketeer.sdk.android.internal.database.OpenHelperCallback
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

internal fun deleteDatabase(context: Context) {
  context.deleteDatabase(OpenHelperCallback.FILE_NAME)
}

internal fun deleteSharedPreferences(context: Context) {
  context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
    .edit()
    .clear()
    .commit()
}

internal inline fun <reified T> MockWebServer.enqueueResponse(
  moshi: Moshi,
  responseCode: Int,
  response: T,
) {
  enqueue(
    MockResponse()
      .setResponseCode(responseCode)
      .setBody(
        moshi.adapter(T::class.java).toJson(response),
      ),
  )
}
