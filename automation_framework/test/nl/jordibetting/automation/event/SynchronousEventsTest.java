package nl.jordibetting.automation.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class SynchronousEventsTest {

	private static class TestSubscriber {

		public Optional<String> event = Optional.empty();
		public int count = 0;

		@Subscribe
		public void test(final String param) {
			event = Optional.ofNullable(param);
			count++;
		}

	}

	private static class MyEventSubscriber {
		public int myEventCounter = 0;
		public int myDerivedEventCounter = 0;

		@Subscribe
		public void event(final MyEvent event) {
			myEventCounter++;
		}

		@Subscribe
		public void derivedEvent(final DerivedMyEvent event) {
			myDerivedEventCounter++;
		}
	}

	private static class MyEvent {

	}

	private static class DerivedMyEvent extends MyEvent {

	}

	private static class MultipleSubscribe {
		public int counter = 0;

		@Subscribe
		public void first(final String a) {
			counter++;
		}

		@Subscribe
		public void second(final String e) {
			counter++;
		}
	}

	@Test
	public void receiveCallback() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertFalse(subscriber.event.isPresent());
		events.trigger("Hello");

		assertTrue(subscriber.event.isPresent());
		assertEquals("Hello", subscriber.event.get());
	}

	@Test
	public void feedbackReceivedOnce() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.count);
		events.trigger("Hi");

		assertEquals(1, subscriber.count);
	}

	@Test
	public void otherTypeNotReceived() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.count);
		events.trigger(new Exception());

		assertEquals(0, subscriber.count);
	}

	@Test
	public void SuperClassNotDeliveredToChild() {
		final Events events = new SynchronousEvents();
		final MyEventSubscriber subscriber = new MyEventSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.myEventCounter);
		assertEquals(0, subscriber.myDerivedEventCounter);
		events.trigger(new MyEvent());

		assertEquals(1, subscriber.myEventCounter);
		assertEquals(0, subscriber.myDerivedEventCounter);
	}

	@Test
	public void DerivedClassAlsoDeliveredtoSuperClass() {
		final Events events = new SynchronousEvents();
		final MyEventSubscriber subscriber = new MyEventSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.myEventCounter);
		assertEquals(0, subscriber.myDerivedEventCounter);
		events.trigger(new DerivedMyEvent());

		assertEquals(1, subscriber.myEventCounter);
		assertEquals(1, subscriber.myDerivedEventCounter);
	}

	@Test
	public void MultipleSubscribed() {
		final Events events = new SynchronousEvents();
		final MultipleSubscribe subscriber = new MultipleSubscribe();
		events.subscribe(subscriber);

		events.trigger("Howdy");

		assertEquals(2, subscriber.counter);
	}

	@Test
	public void nullCallbackThrowsException() {
		final Events events = new SynchronousEvents();
		try {
			events.trigger(null);
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void callbackNoSubscribersNoException() {
		final Events events = new SynchronousEvents();
		events.trigger("test");
	}

	@Test
	public void registerSubscriberTwoReceivesSingle() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);
		events.subscribe(subscriber);

		events.trigger("test");

		assertEquals(1, subscriber.count);
	}

	private class TooManyParametersSubscriber {
		@Subscribe
		public void twoParameters(final Object one, final Object two) {

		}
	}

	@Test
	public void registerTooManyParametersIgnored() {
		final Events events = new SynchronousEvents();
		final TooManyParametersSubscriber subscriber = new TooManyParametersSubscriber();

		try {
			events.subscribe(subscriber);
			fail("Exception expected");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected subscribe with exectly 1 parameter, method: twoParameters", e.getMessage());
		}
	}

	@Test
	public void registerNoParametersThrowsException() {
		final Events events = new SynchronousEvents();
		final NoParametersSubscriber subscriber = new NoParametersSubscriber();

		try {
			events.subscribe(subscriber);
			fail("Exception expected");
		} catch (final IllegalArgumentException e) {
			assertEquals("Expected subscribe with exectly 1 parameter, method: noParameters", e.getMessage());
		}
	}

	private class NoParametersSubscriber {
		@Subscribe
		public void noParameters() {

		}
	}

	@Test
	public void registerNullThrowsException() {
		final Events events = new SynchronousEvents();
		try {
			events.subscribe(null);
			fail("Expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testReturnTypeNoSubscribersEmptyList() {
		final Events events = new SynchronousEvents();
		final EventResult returned = events.trigger("test");

		assertNotNull(returned);
		assertEquals(0, returned.size());
	}

	private static class ReturningSubscriber {

		@Subscribe
		public EventResult test(final String hi) {
			return EventResultList.of("Good morning");
		}
	}

	@Test
	public void testReturnTypeReturnsValue() {
		final Events events = new SynchronousEvents();
		final ReturningSubscriber subscriber = new ReturningSubscriber();
		events.subscribe(subscriber);

		final EventResult returned = events.trigger("test");

		assertEquals(1, returned.size());
		assertTrue(returned.get(0).isPresent());
		assertEquals("Good morning", returned.get(0).get());
	}

	private static class ThrowingSubscriber {
		@Subscribe
		public void throwException(final String a) {
			throw new RuntimeException();
		}
	}

	@Test
	public void testThrowExeption_noresult() {
		final Events events = new SynchronousEvents();
		final ThrowingSubscriber subscriber = new ThrowingSubscriber();
		events.subscribe(subscriber);

		final EventResult trigger = events.trigger("");

		assertEquals(0, trigger.getAll().size());
	}

	private static class NullSubscriber {
		@Subscribe
		public void throwException(final String a) {
			throw null;
		}
	}

	@Test
	public void test_returnNull_noresult() {
		final Events events = new SynchronousEvents();
		final NullSubscriber subscriber = new NullSubscriber();
		events.subscribe(subscriber);

		final EventResult trigger = events.trigger("");

		assertEquals(0, trigger.getAll().size());
	}

	private static class WrongReturnTypeSubscriber {
		@Subscribe
		public Object returnObject(final String in) {
			return new Object();
		}
	}

	@Test
	public void return_wrongReturnType_ignored() {
		final Events events = new SynchronousEvents();
		final WrongReturnTypeSubscriber subscriber = new WrongReturnTypeSubscriber();
		events.subscribe(subscriber);

		final EventResult trigger = events.trigger("");

		assertEquals(0, trigger.getAll().size());
	}
}
