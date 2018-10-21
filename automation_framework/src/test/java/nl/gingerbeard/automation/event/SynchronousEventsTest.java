package nl.gingerbeard.automation.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.state.State;

public class SynchronousEventsTest {

	private static class TestSubscriber {

		public Optional<Device> event = Optional.empty();
		public int count = 0;

		@Subscribe
		public void test(final Device param) {
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

	private static class MyEvent extends Device {

	}

	private static class DerivedMyEvent extends MyEvent {

	}

	private static class MultipleSubscribe {
		public int counter = 0;

		@Subscribe
		public void first(final Device a) {
			counter++;
		}

		@Subscribe
		public void second(final Device e) {
			counter++;
		}
	}

	@Test
	public void receiveCallback() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertFalse(subscriber.event.isPresent());
		events.trigger(new NamedDevice("Hello"));

		assertTrue(subscriber.event.isPresent());
		assertTrue(subscriber.event.get() instanceof NamedDevice);
		assertEquals("Hello", ((NamedDevice) subscriber.event.get()).getName());
	}

	@Test
	public void feedbackReceivedOnce() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.count);
		events.trigger(new NamedDevice("Hi"));

		assertEquals(1, subscriber.count);
	}

	@Test
	public void otherTypeNotReceived() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.count);
		events.trigger(new State());

		assertEquals(0, subscriber.count);
	}

	@Test
	public void superClassNotDeliveredToChild() {
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
	public void derivedClassAlsoDeliveredtoSuperClass() {
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
	public void multipleSubscribed() {
		final Events events = new SynchronousEvents();
		final MultipleSubscribe subscriber = new MultipleSubscribe();
		events.subscribe(subscriber);

		events.trigger(new NamedDevice("Howdy"));

		assertEquals(2, subscriber.counter);
	}

	@Test
	public void nullCallbackThrowsException() {
		final Events events = new SynchronousEvents();
		try {
			events.trigger((Device) null);
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void callbackNoSubscribersNoException() {
		final Events events = new SynchronousEvents();
		events.trigger(new NamedDevice("test"));
	}

	@Test
	public void registerSubscriberTwoReceivesSingle() {
		final Events events = new SynchronousEvents();
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);
		events.subscribe(subscriber);

		events.trigger(new NamedDevice("test"));

		assertEquals(1, subscriber.count);
	}

	private static class TooManyParametersSubscriber {
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

	private static class NoParametersSubscriber {
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
		final EventResult returned = events.trigger(new NamedDevice("test"));

		assertNotNull(returned);
		assertEquals(0, returned.size());
	}

	private static class ReturningSubscriber {

		@Subscribe
		public EventResult test(final Device hi) {
			return EventResultList.of("Good morning");
		}
	}

	@Test
	public void testReturnTypeReturnsValue() {
		final Events events = new SynchronousEvents();
		final ReturningSubscriber subscriber = new ReturningSubscriber();
		events.subscribe(subscriber);

		final EventResult returned = events.trigger(new NamedDevice("test"));

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

		final EventResult trigger = events.trigger(new NamedDevice(""));

		assertEquals(0, trigger.getAll().size());
	}

	private static class NullSubscriber {
		@Subscribe
		public void throwException(final String a) {
			throw new RuntimeException();
		}
	}

	@Test
	public void test_returnNull_noresult() {
		final Events events = new SynchronousEvents();
		final NullSubscriber subscriber = new NullSubscriber();
		events.subscribe(subscriber);

		final EventResult trigger = events.trigger(new NamedDevice(""));

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

		final EventResult trigger = events.trigger(new NamedDevice(""));

		assertEquals(0, trigger.getAll().size());
	}
}
