package io.bucketeer.sdk.android.internal.model

// we can't use codegen here
// see EventAdapterFactory
data class Event(
  val id: String,
  val event: EventData,
  val type: EventType
)
