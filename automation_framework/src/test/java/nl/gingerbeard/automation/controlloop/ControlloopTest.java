package nl.gingerbeard.automation.controlloop;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.TestDevice;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class ControlloopTest {

	private static class EventsStore implements IEvents {

		private final List<Object> receivedEvents = new ArrayList<>();

		@Override
		public void subscribe(final Object subscriber) {
		}

		@Override
		public EventResult trigger(final Object event) {
			getReceivedEvents().add(event);
			return EventResult.empty();
		}

		public List<Object> getReceivedEvents() {
			return receivedEvents;
		}

	}

	private static class MockTransmitter implements IDomoticzUpdateTransmitter {

		@Override
		public <T> void transmitDeviceUpdate(final NextState<T> newState) throws IOException {

		}
	}

	@Test
	public void testStateChanged() {
		final EventsStore events = new EventsStore();
		final Controlloop control = new Controlloop(events, new MockTransmitter());
		final TestDevice myDevice = new TestDevice();

		assertEquals(0, events.getReceivedEvents().size());

		control.statusChanged(myDevice);

		assertEquals(1, events.getReceivedEvents().size());
		assertEquals(myDevice, events.getReceivedEvents().get(0));
	}

	@Test
	public void learn_isAssignebleFrom() {
		assertTrue(Collection.class.isAssignableFrom(ArrayList.class));
	}

	private static class CustomEvents implements IEvents {

		private EventResult result = EventResult.empty();

		@Override
		public void subscribe(final Object subscriber) {
		}

		@Override
		public EventResult trigger(final Object event) {
			return result;
		}

		public void setResult(final EventResult result) {
			this.result = result;
		}
	}

	private static class RecordingTransmitter implements IDomoticzUpdateTransmitter {

		private final List<NextState<?>> transmitted = new ArrayList<>();

		@Override
		public <T> void transmitDeviceUpdate(final NextState<T> newState) throws IOException {
			transmitted.add(newState);
		}

		public List<NextState<?>> getTransmitted() {
			return transmitted;
		}

	}

	private static final Switch mockDevice1 = new Switch(0);
	private static final Switch mockDevice2 = new Switch(1);
	private static final Switch changedDevice = new Switch(666);

	@Test
	public void eventResultWithSingleNextState_transmitted() {
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final CustomEvents events = new CustomEvents();
		final Controlloop control = new Controlloop(events, transmitter);
		events.setResult(EventResult.of(new NextState<>(mockDevice1, OnOffState.ON)));

		control.statusChanged(changedDevice);

		assertEquals(1, transmitter.getTransmitted().size());
		assertTransmitted(transmitter, 0, mockDevice1, OnOffState.ON);
	}

	@Test
	public void eventResultWithNextStateCollection_transmitted() {
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final CustomEvents events = new CustomEvents();
		final Controlloop control = new Controlloop(events, transmitter);
		events.setResult(EventResult.of(Lists.newArrayList(//
				new NextState<>(mockDevice1, OnOffState.ON), //
				new NextState<>(mockDevice2, OnOffState.OFF)//
		)));

		control.statusChanged(changedDevice);

		assertEquals(2, transmitter.getTransmitted().size());
		assertTransmitted(transmitter, 0, mockDevice1, OnOffState.ON);
		assertTransmitted(transmitter, 1, mockDevice2, OnOffState.OFF);
	}

	private void assertTransmitted(final RecordingTransmitter transmitter, final int index, final Switch expectedDevice, final OnOffState expectedState) {
		final NextState<?> message = transmitter.getTransmitted().get(index);
		assertEquals(expectedDevice, message.getDevice());
		assertEquals(expectedState, message.get());
	}

	@Test
	public void eventResultOtherType_ignored() {
		// TODO: Should this somehow be handled/explictely tested? Like an error list, test logging or even throw an exception?
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final CustomEvents events = new CustomEvents();
		final Controlloop control = new Controlloop(events, transmitter);
		events.setResult(EventResult.of("StringIsNotNextState"));

		control.statusChanged(changedDevice);
		assertEquals(0, transmitter.getTransmitted().size());
	}

	private static class ThrowExceptionOnFirstTransmit_Transmitter extends RecordingTransmitter {

		private int callCount = 0;

		@Override
		public <T> void transmitDeviceUpdate(final NextState<T> newState) throws IOException {
			if (callCount++ == 0) {
				throw new IOException("test exception");
			}
			super.transmitDeviceUpdate(newState);
		}

	}

	@Test
	public void transmitterThrowsException_ignoredNoException() {
		// TODO: Test logging here?
		final ThrowExceptionOnFirstTransmit_Transmitter transmitter = new ThrowExceptionOnFirstTransmit_Transmitter();
		final CustomEvents events = new CustomEvents();
		final Controlloop control = new Controlloop(events, transmitter);
		events.setResult(EventResult.of(Lists.newArrayList(//
				new NextState<>(mockDevice1, OnOffState.ON), //
				new NextState<>(mockDevice2, OnOffState.OFF)//
		)));

		control.statusChanged(changedDevice);

		assertEquals(1, transmitter.getTransmitted().size());
		assertTransmitted(transmitter, 0, mockDevice2, OnOffState.OFF);
	}
}
