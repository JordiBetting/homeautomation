package nl.gingerbeard.automation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.autocontrol.OnOffAutoControl;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.state.OnOffState;

public class AutoControlIntegrationTest extends IntegrationTest {

	private static class TestRoom extends Room {

		private final Switch sensor = new Switch(1);
		public final Switch actuator = new Switch(2);

		public TestRoom() {
			addDevice(sensor).and(actuator);
			final OnOffAutoControl<Switch, OnOffState, OnOffState> autoControl = createAutoControl();
			assertTrue(super.getAutoControls().isEmpty());
			addAutoControl(autoControl);
			assertFalse(super.getAutoControls().isEmpty());
		}

		private OnOffAutoControl<Switch, OnOffState, OnOffState> createAutoControl() {
			final OnOffAutoControl<Switch, OnOffState, OnOffState> autoControl = new OnOffAutoControl<>(OnOffState.ON, OnOffState.ON, OnOffState.OFF);
			autoControl.addActuator(actuator);
			autoControl.addSensor(sensor);
			autoControl.setDelayedOff(50, TimeUnit.MILLISECONDS);
			return autoControl;
		}
	}

	@Test
	public void theTest() throws IOException, InterruptedException {

		final TestRoom room = new TestRoom();
		automation.addRoom(room);
		CountDownLatch requestLatch;

		requestLatch = resetLatch();
		deviceChanged(1, "on");
		assertTrue(requestLatch.await(5, TimeUnit.SECONDS));
		final List<String> requests = webserver.getRequests();
		assertEquals(1, requests.size());
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=2&switchcmd=On", requests.get(0));
		requests.clear();

		requestLatch = resetLatch();
		deviceChanged(1, "off");
		assertTrue(requestLatch.await(5, TimeUnit.SECONDS));
		assertEquals("GET /json.htm?type=command&param=switchlight&idx=2&switchcmd=Off", requests.get(0));
	}

	private CountDownLatch resetLatch() {
		final CountDownLatch requestLatch = new CountDownLatch(1);
		webserver.setRequestLatch(requestLatch);
		return requestLatch;
	}

}
