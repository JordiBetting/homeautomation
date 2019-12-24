package nl.gingerbeard.automation.domoticz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.clients.UpdateTransmitterClient;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration.DomoticzInitBehaviorConfig;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer;
import nl.gingerbeard.automation.domoticz.threadhandler.DomoticzThreadHandler;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.util.RetryUtil.RetryTask;

public class DomoticzImplTest {

	private ILogger log;
	private DomoticzThreadHandler threadHandler;
	private DomoticzEventReceiverServer receiver;
	private UpdateTransmitterClient transmitter;
	private DomoticzImpl domoticz;
	private TimeOfDayClient todClient;
	private DomoticzConfiguration config;

	@BeforeEach
	public void setup() {
		transmitter = mock(UpdateTransmitterClient.class);
		receiver = mock(DomoticzEventReceiverServer.class);
		threadHandler = mock(DomoticzThreadHandler.class);
		log = mock(ILogger.class);
		todClient = mock(TimeOfDayClient.class);
		config = mock(DomoticzConfiguration.class);
		domoticz = new DomoticzImpl(transmitter, receiver, threadHandler, todClient, config, log);
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

	@Test
	public void updateTime_handlerDoesNotHanledTime_returnsFalse() throws DomoticzException {
		when(threadHandler.handlesTime()).thenReturn(false);

		boolean result = domoticz.timeChanged(1, 2, 3);

		assertFalse(result);
	}

	@Test
	public void updateAlarm_handlerDoesNotHandleAlarm_returnsFalse() throws DomoticzException {
		when(threadHandler.handlesAlarm()).thenReturn(false);

		boolean result = domoticz.alarmChanged("ARM_HOME");

		assertFalse(result);
	}

	@Test
	public void updateDevice_handlerDoesNotHandleDevice_returnsFalse() throws DomoticzException {
		when(threadHandler.handlesDevice(1)).thenReturn(false);

		boolean result = domoticz.deviceChanged(1, "niks");

		assertFalse(result);
	}

	@Test
	public void syncFull_interrupted_throwsInterruptedException() throws InterruptedException, DomoticzException {
		DomoticzImpl handler = new InterruptingImpl(transmitter, receiver, threadHandler, todClient, config, log);
		when(config.isInitEnabled()).thenReturn(true);
		when(config.getInitConfig()).thenReturn(Optional.of(new DomoticzInitBehaviorConfig(5, 15)));

		Thread.currentThread().interrupt();
		DomoticzException e = assertThrows(DomoticzException.class, () -> handler.syncFullState());
		
		assertEquals("Interrupted while retrying syncFullState", e.getMessage());
		assertEquals(InterruptedException.class, e.getCause().getClass());
	}
	
	private static class InterruptingImpl extends DomoticzImpl {

		InterruptingImpl(UpdateTransmitterClient transmitter, DomoticzEventReceiverServer receiver,
				DomoticzThreadHandler threadHandler, TimeOfDayClient todClient, DomoticzConfiguration config,
				ILogger log) {
			super(transmitter, receiver, threadHandler, todClient, config, log);
		}
		
		@Override
		void executeTaskWithRetries(RetryTask task, DomoticzInitBehaviorConfig config)
				throws InterruptedException, DomoticzException {
			throw new InterruptedException("test interruption");
		}
	}

	@Test
	public void retryFailed() throws IOException, InterruptedException, DomoticzException {
		DomoticzException e = assertThrows(DomoticzException.class, () -> domoticz.executeTaskWithRetries(() -> {
			throw new Exception("Failing test task");
		}, new DomoticzInitBehaviorConfig(1, 1)));
		assertEquals("Failed to sync full state with Domoticz", e.getMessage());
		assertEquals("Failing test task", e.getCause().getMessage());
	}
}
