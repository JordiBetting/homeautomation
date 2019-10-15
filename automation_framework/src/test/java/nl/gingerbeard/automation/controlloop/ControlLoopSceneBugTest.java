package nl.gingerbeard.automation.controlloop;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.testdevices.TestDevice;

public class ControlLoopSceneBugTest {

		@Test
		public void testStateChanged() {
			final IEvents events = mock(IEvents.class);
			final ILogger log = mock(ILogger.class);
			final IState state = mock(IState.class);
			final IDomoticzUpdateTransmitter transmitter = mock(IDomoticzUpdateTransmitter.class);
			when(events.trigger(any())).thenReturn(EventResult.empty());
			final Controlloop control = new Controlloop(events, transmitter, state, log);
			final TestDevice myDevice = new TestDevice();

			control.statusChanged(myDevice);

			verify(events, times(1)).trigger(myDevice);
			verifyNoMoreInteractions(events);
		}

}
