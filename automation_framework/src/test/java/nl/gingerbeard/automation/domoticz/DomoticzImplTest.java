package nl.gingerbeard.automation.domoticz;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer;
import nl.gingerbeard.automation.domoticz.threadhandler.DomoticzThreadHandler;
import nl.gingerbeard.automation.domoticz.transmitter.DomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;

public class DomoticzImplTest {

	private ILogger log;
	private DomoticzThreadHandler threadHandler;
	private DomoticzEventReceiverServer receiver;
	private DomoticzUpdateTransmitter transmitter;
	private DomoticzImpl domoticz;
	private TimeOfDayClient todClient;

	@BeforeEach
	public void setup() {
		transmitter = mock(DomoticzUpdateTransmitter.class);
		receiver = mock(DomoticzEventReceiverServer.class);
		threadHandler = mock(DomoticzThreadHandler.class);
		log = mock(ILogger.class);
		todClient = mock(TimeOfDayClient.class);
		domoticz = new DomoticzImpl(transmitter, receiver, threadHandler, todClient, log);
	}
	
	@Test
	public void deviceChange_interrupted_exceptionRethrown() throws InterruptedException, DomoticzException {
		when(threadHandler.handlesDevice(1)).thenReturn(true);
		
		doThrow(InterruptedException.class).when(threadHandler).deviceChanged(1, "aap");
		
		assertThrows(DomoticzException.class, () -> domoticz.deviceChanged(1, "aap"));
	}

	@Test
	public void timeChange_interrupted_exceptionRethrown() throws InterruptedException, DomoticzException, IOException {
		when(threadHandler.handlesTime()).thenReturn(true);
		
		when(todClient.createTimeOfDayValues()).thenReturn(null);
		doThrow(InterruptedException.class).when(threadHandler).timeChanged(any());
		
		assertThrows(DomoticzException.class, () -> domoticz.timeChanged(1, 2, 3));
	}
	
	@Test
	public void alarmChange_interrupted_exceptionRethrown() throws InterruptedException, DomoticzException {
		when(threadHandler.handlesAlarm()).thenReturn(true);
		
		doThrow(InterruptedException.class).when(threadHandler).alarmChanged(any());
		
		assertThrows(DomoticzException.class, () -> domoticz.alarmChanged(""));
	}
}
