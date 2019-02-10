package nl.gingerbeard.automation.controlloop;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.state.TimeOfDayValues;
import nl.gingerbeard.automation.testdevices.TestDevice;

public class ControlloopTest {

	@Test
	public void testStateChanged() {
		final IEvents events = mock(IEvents.class);
		final ILogger log = mock(ILogger.class);
		final State state = new State();
		final IDomoticzUpdateTransmitter transmitter = mock(IDomoticzUpdateTransmitter.class);
		when(events.trigger(any())).thenReturn(EventResult.empty());
		final Controlloop control = new Controlloop(events, transmitter, state, log);
		final TestDevice myDevice = new TestDevice();

		control.statusChanged(myDevice);

		verify(events, times(1)).trigger(myDevice);
		verifyNoMoreInteractions(events);
	}

	@Test
	public void learn_isAssignebleFrom() {
		assertTrue(Collection.class.isAssignableFrom(ArrayList.class));
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
		final IEvents events = mock(IEvents.class);
		final ILogger log = mock(ILogger.class);
		final State state = new State();
		final Controlloop control = new Controlloop(events, transmitter, state, log);

		when(events.trigger(any())).thenReturn(EventResult.of(new NextState<>(mockDevice1, OnOffState.ON)));

		control.statusChanged(changedDevice);

		assertEquals(1, transmitter.getTransmitted().size());
		assertTransmitted(transmitter, 0, mockDevice1, OnOffState.ON);
	}

	@Test
	public void eventResultWithNextStateCollection_transmitted() {
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final IEvents events = mock(IEvents.class);
		final ILogger log = mock(ILogger.class);
		final State state = new State();
		final Controlloop control = new Controlloop(events, transmitter, state, log);
		when(events.trigger(any())).thenReturn(EventResult.of(Lists.newArrayList(//
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
		final IEvents events = mock(IEvents.class);
		final ILogger log = mock(ILogger.class);
		final State state = new State();
		final Controlloop control = new Controlloop(events, transmitter, state, log);
		when(events.trigger(any())).thenReturn(EventResult.of("StringIsNotNextState"));

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
		final ThrowExceptionOnFirstTransmit_Transmitter transmitter = new ThrowExceptionOnFirstTransmit_Transmitter();
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final State state = new State();
		final Controlloop control = new Controlloop(events, transmitter, state, log);
		when(events.trigger(any())).thenReturn(EventResult.of(Lists.newArrayList(//
				new NextState<>(mockDevice1, OnOffState.ON), //
				new NextState<>(mockDevice2, OnOffState.OFF)//
		)));

		control.statusChanged(changedDevice);

		assertEquals(1, transmitter.getTransmitted().size());
		assertTransmitted(transmitter, 0, mockDevice2, OnOffState.OFF);
		log.assertContains(LogLevel.EXCEPTION, "Failed to transmit device update");
	}

	@Test
	public void timeUpdated_eventTriggered() {
		final IDomoticzUpdateTransmitter transmitter = mock(IDomoticzUpdateTransmitter.class);
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final State state = new State();
		state.setTimeOfDay(TimeOfDay.NIGHTTIME);
		final Controlloop control = new Controlloop(events, transmitter, state, log);

		control.timeChanged(new TimeOfDayValues(5, 1, 10));

		assertEquals(TimeOfDay.DAYTIME, state.getTimeOfDay());
		verify(events, times(1)).trigger(any(TimeOfDay.class));
	}

	@Test
	public void alarmUpdated_eventTriggered() {
		final IDomoticzUpdateTransmitter transmitter = mock(IDomoticzUpdateTransmitter.class);
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final State state = new State();
		state.setAlarmState(AlarmState.DISARMED);

		final Controlloop control = new Controlloop(events, transmitter, state, log);

		control.alarmChanged(AlarmState.ARM_AWAY);

		assertEquals(AlarmState.ARM_AWAY, state.getAlarmState());
		verify(events, times(1)).trigger(AlarmState.ARM_AWAY);
	}

	@Test
	public void alarmUpdatedSameState_noeventTriggered() {
		final IDomoticzUpdateTransmitter transmitter = mock(IDomoticzUpdateTransmitter.class);
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final State state = new State();
		state.setAlarmState(AlarmState.DISARMED);

		final Controlloop control = new Controlloop(events, transmitter, state, log);

		control.alarmChanged(AlarmState.ARM_AWAY);
		control.alarmChanged(AlarmState.ARM_AWAY);

		verify(events, times(1)).trigger(AlarmState.ARM_AWAY);
	}
}
