package nl.gingerbeard.automation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class AutoControlToDomoticzTest {

	private static final NextState<OnOffState> NEXT_STATE1 = new NextState<>(new Switch(1), OnOffState.ON);
	private static final NextState<OnOffState> NEXT_STATE2 = new NextState<>(new Switch(2), OnOffState.OFF);

	@Test
	public void forwarded() throws IOException {
		final IDomoticzUpdateTransmitter transmitter = mock(IDomoticzUpdateTransmitter.class);
		final AutoControlToDomoticz sut = new AutoControlToDomoticz(new TestLogger(), transmitter);

		sut.outputChanged("", createList(NEXT_STATE1, NEXT_STATE2));

		verify(transmitter, times(1)).transmitDeviceUpdate(NEXT_STATE1);
		verify(transmitter, times(1)).transmitDeviceUpdate(NEXT_STATE2);
		verifyNoMoreInteractions(transmitter);
	}

	private List<NextState<?>> createList(final NextState<?>... items) {
		// TODO update to Java 9
		final List<NextState<?>> list = new ArrayList<>();
		Arrays.stream(items).forEach((item) -> list.add(item));
		return list;
	}

	@Test
	public void transmitterThrowsException_exceptionLogged() throws IOException {
		final ILogger log = spy(new TestLogger());
		final IDomoticzUpdateTransmitter transmitter = mock(IDomoticzUpdateTransmitter.class);
		final AutoControlToDomoticz sut = new AutoControlToDomoticz(log, transmitter);

		doThrow(IOException.class).when(transmitter).transmitDeviceUpdate(any());
		sut.outputChanged("", createList(NEXT_STATE1));

		verify(log, times(1)).warning(any(), eq("Could not transmit update"));
	}

	@Test
	public void forwarded_tracelogPresent() throws IOException {
		final TestLogger log = new TestLogger();
		final AutoControlToDomoticz sut = new AutoControlToDomoticz(log, mock(IDomoticzUpdateTransmitter.class));

		sut.outputChanged(getClass().getSimpleName(), createList(NEXT_STATE1));

		log.assertContains(LogLevel.INFO, "[trace] " + getClass().getSimpleName() + ": NextState [device=Device [idx=1, name=Optional.empty, state=null], nextState=ON]");
	}
}
