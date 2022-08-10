package jp.bucketeer.sdk.events.dto

import bucketeer.event.client.EventOuterClass

internal class EventListDataChangedAction(val events: List<EventOuterClass.Event>)
