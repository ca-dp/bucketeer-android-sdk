package io.bucketeer.sdk.android.internal.di

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.Moshi
import io.bucketeer.sdk.android.internal.model.jsonadapter.EventAdapterFactory
import io.bucketeer.sdk.android.internal.model.jsonadapter.EventTypeAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.MetricsEventAdapterFactory
import io.bucketeer.sdk.android.internal.model.jsonadapter.MetricsEventTypeAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.ReasonTypeAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.SourceIDAdapter
import io.bucketeer.sdk.android.internal.model.jsonadapter.UserEvaluationsStateAdapter

internal class DataModule(
  application: Application,
  apiKey: String,
  endpoint: String,
  featureTag: String
) {

  companion object {
    @VisibleForTesting
    internal fun moshi(): Moshi {
      return Moshi.Builder()
        .add(EventTypeAdapter())
        .add(MetricsEventTypeAdapter())
        .add(ReasonTypeAdapter())
        .add(SourceIDAdapter())
        .add(EventAdapterFactory())
        .add(MetricsEventAdapterFactory())
        .add(UserEvaluationsStateAdapter())
        .build()
    }
  }
}
