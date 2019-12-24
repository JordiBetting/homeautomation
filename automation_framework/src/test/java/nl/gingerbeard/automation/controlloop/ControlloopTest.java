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

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Group;
import nl.gingerbeard.automation.devices.OnkyoReceiver;
import nl.gingerbeard.automation.devices.Scene;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.api.IDomoticzAlarmChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzDeviceStatusChanged;
import nl.gingerbeard.automation.domoticz.api.IDomoticzTimeOfDayChanged;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.onkyo.IOnkyoTransmitter;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;
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
		final IState state = mock(IState.class);
		final DomoticzApi transmitter = mock(DomoticzApi.class);
		when(events.trigger(any())).thenReturn(EventResult.empty());
		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));
		final TestDevice myDevice = new TestDevice();

		control.statusChanged(myDevice);

		verify(events, times(1)).trigger(myDevice);
		verifyNoMoreInteractions(events);
	}

	@Test
	public void learn_isAssignebleFrom() {
		assertTrue(Collection.class.isAssignableFrom(ArrayList.class));
	}

	private static abstract class AbstractTransmitter implements DomoticzApi {
		@Override
		public void setAlarmListener(IDomoticzAlarmChanged alarmListener) {
		}

		@Override
		public void setDeviceListener(IDomoticzDeviceStatusChanged deviceListener) {
		}

		@Override
		public void setTimeListener(IDomoticzTimeOfDayChanged timeListener) {
		}

		@Override
		public void syncFullState() throws InterruptedException, DomoticzException {
		}

	}
	
	private static class RecordingTransmitter extends AbstractTransmitter {

		private final List<NextState<?>> transmitted = new ArrayList<>();

		@Override
		public <T> void transmitDeviceUpdate(final NextState<T> newState) throws DomoticzException {
			transmitted.add(newState);
		}

		public List<NextState<?>> getTransmitted() {
			return transmitted;
		}

	}

	private static final Switch mockDevice1 = new Switch(1);
	private static final Switch mockDevice2 = new Switch(2);
	private static final Switch changedDevice = new Switch(666);
	private static final OnkyoReceiver receiver = new OnkyoReceiver("1.2.3.4");

	@Test
	public void eventResultWithSingleNextState_transmitted() {
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final IEvents events = mock(IEvents.class);
		final ILogger log = mock(ILogger.class);
		when(log.createContext(any())).thenReturn(log);
		final IState state = new State();
		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));

		final EventResult eventResult = EventResult.of(new NextState<>(mockDevice1, OnOffState.ON));
		when(events.trigger(any())).thenReturn(eventResult);

		control.statusChanged(changedDevice);

		assertEquals(1, transmitter.getTransmitted().size());
		assertTransmitted(transmitter, 0, mockDevice1, OnOffState.ON);
	}

	@Test
	public void eventResultWithNextStateCollection_transmitted() {
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final IEvents events = mock(IEvents.class);
		final ILogger log = mock(ILogger.class);
		when(log.createContext(any())).thenReturn(log);
		final IState state = mock(IState.class);
		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));
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

	// TODO: many intializations that are just different. Create something better for this.

	@Test
	public void eventResultOtherType_ignored() {
		// TODO: Should this somehow be handled/explictely tested? Like an error list, test logging or even throw an exception?
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final IEvents events = mock(IEvents.class);
		final ILogger log = mock(ILogger.class);
		final IState state = mock(IState.class);
		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));
		when(events.trigger(any())).thenReturn(EventResult.of("StringIsNotNextState"));

		control.statusChanged(changedDevice);
		assertEquals(0, transmitter.getTransmitted().size());
	}

	private static class ThrowExceptionOnFirstTransmit_Transmitter extends RecordingTransmitter {

		private int callCount = 0;

		@Override
		public <T> void transmitDeviceUpdate(final NextState<T> newState) throws DomoticzException {
			if (callCount++ == 0) {
				throw new DomoticzException("test exception");
			}
			super.transmitDeviceUpdate(newState);
		}

	}

	@Test
	public void transmitterThrowsException_ignoredNoException() {
		final ThrowExceptionOnFirstTransmit_Transmitter transmitter = new ThrowExceptionOnFirstTransmit_Transmitter();
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final IState state = mock(IState.class);
		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));
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
		final DomoticzApi transmitter = mock(DomoticzApi.class);
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final IState state = new State();
		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));

		when(events.trigger(any())).thenReturn(EventResult.empty());

		control.timeChanged(new TimeOfDayValues(5, 1, 10, 1, 10));

		verify(events, times(1)).trigger(any(TimeOfDay.class));
	}

	@Test
	public void alarmUpdated_eventTriggered() {
		final DomoticzApi transmitter = mock(DomoticzApi.class);
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final IState state = new State();
		when(events.trigger(any())).thenReturn(EventResult.empty());

		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));

		control.alarmChanged(AlarmState.ARM_AWAY);

		verify(events, times(1)).trigger(AlarmState.ARM_AWAY);
	}

	@Test
	public void nextState_noUpdate_notTransmitted() throws IOException, DomoticzException {
		final Switch device = new Switch(1);
		final NextState<OnOffState> nextState = new NextState<>(device, OnOffState.ON);

		final IEvents events = mock(IEvents.class);
		when(events.trigger(any(Device.class))).thenReturn(EventResult.of(nextState));

		final DomoticzApi transmitter = mock(DomoticzApi.class);
		final Controlloop control = new Controlloop(events, transmitter, mock(IState.class), mock(ILogger.class), mock(IOnkyoTransmitter.class));

		device.setState(OnOffState.ON);
		control.statusChanged(device);

		verify(transmitter, times(0)).transmitDeviceUpdate(nextState);
	}
	
	@Test
	public void nextState_scene_reportedAgain() throws IOException, DomoticzException {
		final TestLogger log = new TestLogger();
		final Scene device = new Scene(1);
		final NextState<OnOffState> nextState = new NextState<>(device, OnOffState.ON);

		final IEvents events = mock(IEvents.class);
		when(events.trigger(any(Device.class))).thenReturn(EventResult.of(nextState));

		final DomoticzApi transmitter = mock(DomoticzApi.class);
		final Controlloop control = new Controlloop(events, transmitter, mock(IState.class), log, mock(IOnkyoTransmitter.class));

		device.setState(OnOffState.ON);
		control.statusChanged(device);

		verify(transmitter, times(1)).transmitDeviceUpdate(nextState);
	}

	@Test
	public void nextState_group_reportedAgain() throws IOException, DomoticzException {
		final TestLogger log = new TestLogger();
		final Group device = new Group(42);
		final NextState<OnOffState> nextState = new NextState<>(device, OnOffState.ON);

		final IEvents events = mock(IEvents.class);
		when(events.trigger(any(Device.class))).thenReturn(EventResult.of(nextState));

		final DomoticzApi transmitter = mock(DomoticzApi.class);
		final Controlloop control = new Controlloop(events, transmitter, mock(IState.class), log, mock(IOnkyoTransmitter.class));

		device.setState(OnOffState.ON);
		control.statusChanged(device);

		verify(transmitter, times(1)).transmitDeviceUpdate(nextState);
	}

	
	@Test
	public void transmitted_logContainsTrigger() {
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final IState state = mock(IState.class);
		final Controlloop control = new Controlloop(events, transmitter, state, log, mock(IOnkyoTransmitter.class));

		final EventResult eventResult = EventResult.of(new NextState<>(mockDevice1, OnOffState.ON));
		when(events.trigger(any())).thenReturn(eventResult);

		control.statusChanged(changedDevice);

		assertEquals(1, transmitter.getTransmitted().size());
		assertTransmitted(transmitter, 0, mockDevice1, OnOffState.ON);
		log.assertContains(LogLevel.INFO, "[INFO] [trace] " + getClass().getSimpleName() + ": NextState [device=Device [idx=1, name=Optional.empty, state=null], nextState=ON]");
	}
	
	@Test
	public void onkyoTransmission() {
		final RecordingTransmitter transmitter = new RecordingTransmitter();
		final IEvents events = mock(IEvents.class);
		final TestLogger log = new TestLogger();
		final IState state = mock(IState.class);
		IOnkyoTransmitter onkyoTransmitter = mock(IOnkyoTransmitter.class);
		final Controlloop control = new Controlloop(events, transmitter, state, log, onkyoTransmitter);
		
		EventResult eventResult = EventResult.of(receiver.createNextStateMainAndZone2(OnOffState.ON));
		when(events.trigger(any())).thenReturn(eventResult);
		
		control.statusChanged(changedDevice);
		
		verify(onkyoTransmitter, times(2)).transmit(any());
	}
}
