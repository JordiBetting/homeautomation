package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.devices.OnkyoReceiver;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class OnkyoIntegration extends IntegrationTest {

	public static class OnkyoRoom extends Room {
		
		private final OnkyoReceiver receiver;
		private final Switch sensor = new Switch(2);

		public OnkyoRoom() {
			receiver = new OnkyoReceiver("1.2.3.4");
			addDevice(receiver).and(sensor);
		}
		
		@Subscribe
		public List<NextState<OnOffState>> receiverOff(Switch update) {
			return receiver.createNextStateMainAndZone2(OnOffState.OFF);
		}
	}
	
	@Test
	public void doSomething() throws IOException, InterruptedException {
		start(OnkyoRoom.class);
		
		deviceChanged(2, "off");
		
		verify(onkyoDriver, times(1)).setMainOff();
		verify(onkyoDriver, times(1)).setZone2Off();
		verifyNoMoreInteractions(onkyoDriver);
		
		assertEquals(0, webserver.getRequests().size());
	}
}
