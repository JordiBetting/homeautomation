package nl.gingerbeard.automation.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.autocontrol.AutoControl;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.State;

public class SynchronousEventsTest {

	private static ILogger logMock;

	@BeforeAll
	public static void createLogMock() {
		logMock = mock(ILogger.class);
	}

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

	private static class MyEvent extends Device<Void> {

		public MyEvent() {
			super(1);
		}

		@Override
		public boolean updateState(final String newState) {
			return false;
		}

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
		public void second(final Object e) {
			counter++;
		}
	}

	@Test
	public void receiveCallback() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertFalse(subscriber.event.isPresent());
		events.trigger("Hello");

		assertTrue(subscriber.event.isPresent());
		assertEquals("Hello", subscriber.event.get());
	}

	@Test
	public void feedbackReceivedOnce() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.count);
		events.trigger("Hi");

		assertEquals(1, subscriber.count);
	}

	@Test
	public void otherTypeNotReceived() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);

		assertEquals(0, subscriber.count);
		events.trigger(new State());

		assertEquals(0, subscriber.count);
	}

	@Test
	public void superClassNotDeliveredToChild() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
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
		final IEvents events = new SynchronousEvents(new State(), logMock);
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
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final MultipleSubscribe subscriber = new MultipleSubscribe();
		events.subscribe(subscriber);

		events.trigger("Howdy");

		assertEquals(2, subscriber.counter);
	}

	@Test
	public void nullCallbackThrowsException() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		try {
			events.trigger((Device<?>) null);
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void callbackNoSubscribersNoException() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		events.trigger("test");
	}

	@Test
	public void registerSubscriberTwoReceivesSingle() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber subscriber = new TestSubscriber();
		events.subscribe(subscriber);
		events.subscribe(subscriber);

		events.trigger("test");

		assertEquals(1, subscriber.count);
	}

	private static class TooManyParametersSubscriber {
		@Subscribe
		public void twoParameters(final Object one, final Object two) {

		}
	}

	@Test
	public void registerTooManyParametersIgnored() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
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
		final IEvents events = new SynchronousEvents(new State(), logMock);
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
		final IEvents events = new SynchronousEvents(new State(), logMock);
		try {
			events.subscribe(null);
			fail("Expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void testReturnTypeNoSubscribersEmptyList() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final EventResult returned = events.trigger("test");

		assertNotNull(returned);
		assertEquals(0, returned.size());
	}

	private static class ReturningSubscriber {

		@Subscribe
		public EventResult test(final String parameter) {
			return EventResult.of("Good morning");
		}
	}

	@Test
	public void testReturnTypeReturnsValue() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
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
		final TestLogger log = new TestLogger();
		final IEvents events = new SynchronousEvents(new State(), log);
		final ThrowingSubscriber subscriber = new ThrowingSubscriber();
		events.subscribe(subscriber);

		final EventResult trigger = events.trigger("");

		assertEquals(0, trigger.getAll().size());
		log.assertContains(LogLevel.EXCEPTION, "Received exception during invocation of subscriber. Ignoring.");
	}

	private static class NullSubscriber {
		@Subscribe
		public EventResult throwException(final String a) {
			return null;
		}
	}

	@Test
	public void test_returnNull_noresult() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final NullSubscriber subscriber = new NullSubscriber();
		events.subscribe(subscriber);

		final EventResult trigger = events.trigger("");

		assertEquals(0, trigger.getAll().size());
	}

	private static class WrongReturnTypeSubscriber {
		@Subscribe
		public WrongReturnTypeSubscriber returnObject(final String in) {
			return this;
		}
	}

	@Test
	public void return_otherType_wrapped() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final WrongReturnTypeSubscriber subscriber = new WrongReturnTypeSubscriber();
		events.subscribe(subscriber);

		final EventResult trigger = events.trigger("");

		assertEquals(1, trigger.getAll().size());
		assertTrue(trigger.get(0).isPresent());
		assertEquals(subscriber, trigger.get(0).get());
	}

	@Test
	public void disableClass() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber testSubscriber = new TestSubscriber();
		events.subscribe(testSubscriber);
		events.subscribe(new MultipleSubscribe());

		events.disable(TestSubscriber.class.getSimpleName());
		events.trigger("hi");

		assertEquals(0, testSubscriber.count);
	}

	@Test
	public void disableEnableClass() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber testSubscriber = new TestSubscriber();
		events.subscribe(testSubscriber);
		events.subscribe(new MultipleSubscribe());

		events.disable(TestSubscriber.class.getSimpleName());
		events.enable(TestSubscriber.class.getSimpleName());
		events.trigger("hi");

		assertEquals(1, testSubscriber.count);
	}

	@Test
	public void disableIdempotent() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber testSubscriber = new TestSubscriber();
		events.subscribe(testSubscriber);

		events.disable(TestSubscriber.class.getSimpleName());
		events.disable(TestSubscriber.class.getSimpleName());
		events.trigger("hi");

		assertEquals(0, testSubscriber.count);
	}

	@Test
	public void enableIdempotent() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final TestSubscriber testSubscriber = new TestSubscriber();
		events.subscribe(testSubscriber);

		events.disable(TestSubscriber.class.getSimpleName());
		events.enable(TestSubscriber.class.getSimpleName());
		events.enable(TestSubscriber.class.getSimpleName());
		events.trigger("hi");

		assertEquals(1, testSubscriber.count);
	}

	@Test
	public void enableNull_throwsException() {
		final IEvents events = new SynchronousEvents(new State(), logMock);

		assertThrows(IllegalArgumentException.class, () -> events.enable(null));
	}

	@Test
	public void disableNull_throwsException() {
		final IEvents events = new SynchronousEvents(new State(), logMock);

		assertThrows(IllegalArgumentException.class, () -> events.disable(null));
	}

	@Test
	public void disableNonExisting_throwsException() {
		final IEvents events = new SynchronousEvents(new State(), logMock);

		assertThrows(IllegalArgumentException.class, () -> events.disable("nothingpresent"));
	}

	@Test
	public void enableNonExisting_throwsException() {
		final IEvents events = new SynchronousEvents(new State(), logMock);

		assertThrows(IllegalArgumentException.class, () -> events.enable("nothingpresent"));
	}

	public static class AutoControlSubscriber {

		public static class BlaatControl extends AutoControl {
			private int count;

			@Override
			public List<IDevice<?>> getDevices() {
				return Lists.newArrayList();
			}

			@Subscribe
			public void receiveStrings(final String event) {
				count++;
			}

			public int getCount() {
				return count;
			}
		}

		private final BlaatControl control;

		public AutoControlSubscriber() {
			control = new BlaatControl();
		}

		public int getCount() {
			return control.getCount();
		}

		public BlaatControl getControl() {
			return control;
		}

	}

	@Test
	public void disableAutoControlOwnerUsed() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final AutoControlSubscriber subscriber = new AutoControlSubscriber();
		events.subscribe(subscriber.getControl());

		events.disable(AutoControlSubscriber.class.getSimpleName());
		events.trigger("hi");

		assertEquals(0, subscriber.getCount());
	}

	@Test
	public void enableAutoControlOwnerUsed() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final AutoControlSubscriber subscriber = new AutoControlSubscriber();
		events.subscribe(subscriber.getControl());

		events.disable(AutoControlSubscriber.class.getSimpleName());
		events.enable(AutoControlSubscriber.class.getSimpleName());
		events.trigger("goedendag");
		assertEquals(1, subscriber.getCount());
	}

	public static class AutoControlSubscriber2 {

		public static class BlaatControl2 extends AutoControl {
			private int count;

			@Override
			public List<IDevice<?>> getDevices() {
				return Lists.newArrayList();
			}

			@Subscribe
			public void receiveStrings(final String event) {
				count++;
			}

			public int getCount() {
				return count;
			}
		}

		private final BlaatControl2 control;

		public AutoControlSubscriber2() {
			control = new BlaatControl2();
		}

		public int getCount() {
			return control.getCount();
		}

		public BlaatControl2 getControl() {
			return control;
		}

	}

	@Test
	public void disableAutoControlOwnerUsed_multipleAutoControls() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final AutoControlSubscriber subscriber = new AutoControlSubscriber();
		final AutoControlSubscriber2 subscriber2 = new AutoControlSubscriber2();
		events.subscribe(subscriber.getControl());
		events.subscribe(subscriber2.getControl());

		events.disable(AutoControlSubscriber2.class.getSimpleName());
		events.trigger("hi");

		assertEquals(1, subscriber.getCount());
		assertEquals(0, subscriber2.getCount());
	}

	@Test
	public void getSubscribers_listComplete() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final AutoControlSubscriber subscriber = new AutoControlSubscriber();
		final AutoControlSubscriber2 subscriber2 = new AutoControlSubscriber2();
		events.subscribe(subscriber.getControl());
		events.subscribe(subscriber2.getControl());
		events.subscribe(new TestSubscriber());

		final List<String> subscribers = events.getSubscribers();

		assertEquals(3, subscribers.size());
		assertTrue(subscribers.contains("AutoControlSubscriber"));
		assertTrue(subscribers.contains("AutoControlSubscriber2"));
		assertTrue(subscribers.contains("TestSubscriber"));
	}

	@Test
	public void isEnabled() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final AutoControlSubscriber subscriber = new AutoControlSubscriber();
		final AutoControlSubscriber2 subscriber2 = new AutoControlSubscriber2();
		events.subscribe(subscriber.getControl());
		events.subscribe(subscriber2.getControl());

		events.disable(AutoControlSubscriber2.class.getSimpleName());

		assertTrue(events.isEnabled(AutoControlSubscriber.class.getSimpleName()));
		assertFalse(events.isEnabled(AutoControlSubscriber2.class.getSimpleName()));
	}

	@Test
	public void isEnabled_notExisting_returnsFalse() {
		final IEvents events = new SynchronousEvents(new State(), logMock);
		final AutoControlSubscriber subscriber = new AutoControlSubscriber();
		final AutoControlSubscriber2 subscriber2 = new AutoControlSubscriber2();
		events.subscribe(subscriber.getControl());
		events.subscribe(subscriber2.getControl());

		final boolean result = events.isEnabled("space");

		assertFalse(result);
	}
}
