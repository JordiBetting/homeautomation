package nl.gingerbeard.automation.domoticz.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.state.TimeOfDayValues;

public class TimeSyncTest {

	@Mock
	public IState state;
	
	@Mock
	public TimeOfDayClient todClient;
	
	@Mock
	public TimeOfDayValues todValues;
	
	@BeforeEach
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void dayTime() throws IOException {
		TimeSync sync = new TimeSync(state, todClient);
		when(todClient.createTimeOfDayValues()).thenReturn(todValues);
		when(todValues.isDayTime()).thenReturn(true);
		
		sync.syncTime();
		
		verify(state, Mockito.times(1)).setTimeOfDay(TimeOfDay.DAYTIME);
	}
	
	@Test
	public void nightTime() throws IOException {
		TimeSync sync = new TimeSync(state, todClient);
		when(todClient.createTimeOfDayValues()).thenReturn(todValues);
		when(todValues.isDayTime()).thenReturn(false);
		
		sync.syncTime();
		
		verify(state, Mockito.times(1)).setTimeOfDay(TimeOfDay.NIGHTTIME);
	}
	
	@Test
	public void syncException() throws IOException {
		TimeSync sync = new TimeSync(state, todClient);
		when(todClient.createTimeOfDayValues()).thenThrow(new IOException("Testcase"));
		
		IOException e = assertThrows(IOException.class, () -> sync.syncTime());
		assertEquals("Testcase", e.getMessage());
	}
}
