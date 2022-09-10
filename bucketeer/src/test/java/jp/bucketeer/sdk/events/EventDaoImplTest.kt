package jp.bucketeer.sdk.events

import bucketeer.event.client.EventOuterClass
import jp.bucketeer.sdk.database.DatabaseOpenHelper
import jp.bucketeer.sdk.evaluationEvent1
import jp.bucketeer.sdk.evaluationEvent2
import jp.bucketeer.sdk.events.db.EventDaoImpl
import jp.bucketeer.sdk.goalEvent1
import jp.bucketeer.sdk.goalEvent2
import jp.bucketeer.sdk.metricsEvent1
import jp.bucketeer.sdk.metricsEvent2
import jp.bucketeer.sdk.metricsEvent3
import jp.bucketeer.sdk.metricsEvent4
import jp.bucketeer.sdk.metricsEvent5
import jp.bucketeer.sdk.metricsEvent6
import jp.bucketeer.sdk.metricsEvent7
import jp.bucketeer.sdk.metricsEvent8
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeIn
import org.amshove.kluent.shouldNotBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class EventDaoImplTest {

  private lateinit var eventDao: EventDaoImpl
  private lateinit var openHelper: DatabaseOpenHelper

  @Before
  fun setUp() {
    openHelper = DatabaseOpenHelper(RuntimeEnvironment.application, null)
    eventDao = EventDaoImpl(openHelper)
  }

  @Test
  fun addEvent_goal() {
    eventDao.addEvent(goalEvent1)

    val expected = goalEvent1.pack()
    val actual = eventDao.getEvents()[0].event
    actual shouldBeEqualTo expected
    eventDao.getEvents().assertNotEmptyIds()
  }

  @Test
  fun addEvent_evaluation() {
    eventDao.addEvent(evaluationEvent1)

    val expected = evaluationEvent1.pack()
    val actual = eventDao.getEvents()[0]
    actual.event shouldBeEqualTo expected
    eventDao.getEvents().assertNotEmptyIds()
  }

  @Test
  fun addEvent_metrics() {
    eventDao.addEvent(metricsEvent1)

    val expected = metricsEvent1.pack()
    val actual = eventDao.getEvents()[0]
    actual.event shouldBeEqualTo expected
    eventDao.getEvents().assertNotEmptyIds()
  }

  @Test
  fun addEvent_multipleEvent() {
    eventDao.addEvent(evaluationEvent1)
    eventDao.addEvent(goalEvent1)
    eventDao.addEvent(metricsEvent1)
    eventDao.addEvent(evaluationEvent2)
    eventDao.addEvent(goalEvent2)
    eventDao.addEvent(metricsEvent2)
    eventDao.addEvent(metricsEvent3)
    eventDao.addEvent(metricsEvent4)
    eventDao.addEvent(metricsEvent5)
    eventDao.addEvent(metricsEvent6)
    eventDao.addEvent(metricsEvent7)
    eventDao.addEvent(metricsEvent8)

    val expected = listOf(
      evaluationEvent1.pack(),
      goalEvent1.pack(),
      metricsEvent1.pack(),
      evaluationEvent2.pack(),
      goalEvent2.pack(),
      metricsEvent2.pack(),
      metricsEvent3.pack(),
      metricsEvent4.pack(),
      metricsEvent5.pack(),
      metricsEvent6.pack(),
      metricsEvent7.pack(),
      metricsEvent8.pack(),
    )
    val actual = eventDao.getEvents().map { it.event }
    expected.forEach {
      it shouldBeIn actual
    }
    eventDao.getEvents().assertNotEmptyIds()
  }

  @Test
  fun deleteAll() {
    eventDao.addEvent(evaluationEvent1)
    eventDao.addEvent(goalEvent1)
    eventDao.addEvent(metricsEvent1)
    eventDao.addEvent(evaluationEvent2)
    eventDao.addEvent(goalEvent2)
    eventDao.addEvent(metricsEvent2)
    eventDao.addEvent(metricsEvent3)
    eventDao.addEvent(metricsEvent4)
    eventDao.addEvent(metricsEvent5)
    eventDao.addEvent(metricsEvent6)
    eventDao.addEvent(metricsEvent7)
    eventDao.addEvent(metricsEvent8)
    val ids = eventDao.getEvents().map { it.id }

    eventDao.delete(ids)

    eventDao.getEvents().assertNotEmptyIds()
    eventDao.getEvents() shouldBe emptyList()
  }

  @Test
  fun deleteAnyItems() {
    eventDao.addEvent(evaluationEvent1)
    eventDao.addEvent(goalEvent1)
    eventDao.addEvent(metricsEvent1)
    eventDao.addEvent(evaluationEvent2)
    eventDao.addEvent(goalEvent2)
    eventDao.addEvent(metricsEvent2)
    eventDao.addEvent(metricsEvent3)
    eventDao.addEvent(metricsEvent4)
    eventDao.addEvent(metricsEvent5)
    eventDao.addEvent(metricsEvent6)
    eventDao.addEvent(metricsEvent7)
    eventDao.addEvent(metricsEvent8)
    val ids = eventDao.getEvents()
      .filter {
        it.event.equals(evaluationEvent2.pack()) ||
          it.event.equals(goalEvent1.pack()) ||
          it.event.equals(metricsEvent1.pack()) ||
          it.event.equals(metricsEvent3.pack()) ||
          it.event.equals(metricsEvent5.pack()) ||
          it.event.equals(metricsEvent7.pack())
      }
      .map { it.id }

    eventDao.delete(ids)

    eventDao.getEvents().assertNotEmptyIds()
    eventDao.getEvents().size shouldBe 6
    eventDao.getEvents()[0].event shouldBeEqualTo evaluationEvent1.pack()
    eventDao.getEvents()[1].event shouldBeEqualTo goalEvent2.pack()
    eventDao.getEvents()[2].event shouldBeEqualTo metricsEvent2.pack()
    eventDao.getEvents()[3].event shouldBeEqualTo metricsEvent4.pack()
    eventDao.getEvents()[4].event shouldBeEqualTo metricsEvent6.pack()
    eventDao.getEvents()[5].event shouldBeEqualTo metricsEvent8.pack()
  }

  private fun List<EventOuterClass.Event>.assertNotEmptyIds() {
    forEach { it.id.isEmpty() shouldNotBeEqualTo true }
  }
}
