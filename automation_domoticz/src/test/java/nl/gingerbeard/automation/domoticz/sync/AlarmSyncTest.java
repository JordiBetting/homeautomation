package nl.gingerbeard.automation.domoticz.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import nl.gingerbeard.automation.domoticz.clients.AlarmStateClient;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.IState;

public class AlarmSyncTest {

	@Mock
	public IState state;

	@Mock
	public AlarmStateClient client;

	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@ParameterizedTest
	@EnumSource(//
			value = AlarmState.class,// 
			names = { "DISARMED", "ARM_HOME", "ARM_AWAY" })
	public void disarmed(AlarmState testedState) throws IOException {
		when(client.getAlarmState()).thenReturn(testedState);
		AlarmSync sync = new AlarmSync(state, client);

		sync.syncAlarm();

		Mockito.verify(state, Mockito.times(1)).setAlarmState(testedState);
	}
	
	@Test
	public void exceptionThrown() throws IOException {
		when(client.getAlarmState()).thenThrow(new IOException("TestMessage"));
		AlarmSync sync = new AlarmSync(state, client);
		
		IOException e = assertThrows(IOException.class, () -> sync.syncAlarm());
		assertEquals("TestMessage", e.getMessage());
	}

}
