package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class OnkyoReceiverTest {

	@Test
	public void updateState_notSupported() {
		OnkyoReceiver onkyo = new OnkyoReceiver("");
		assertThrows(UnsupportedOperationException.class, () -> onkyo.updateState("anything"));
	}

	@Test
	public void initialState_set() {
		OnkyoReceiver onkyo = new OnkyoReceiver("");
		assertNotNull(onkyo.getState());
	}

	@Test
	public void initialState_host() {
		OnkyoReceiver onkyo = new OnkyoReceiver("onkyoHostName.domain");

		assertEquals("onkyoHostName.domain", onkyo.getHost());
	}
	
	@Test
	public void main_update_notSupported() {
		OnkyoZoneMain main = new OnkyoZoneMain();
		assertThrows(UnsupportedOperationException.class, () -> main.updateState("anything"));
	}
	
	@Test
	public void zone2_update_notSupported() {
		OnkyoZone2 zone2 = new OnkyoZone2();
		assertThrows(UnsupportedOperationException.class, () -> zone2.updateState("anything"));
	}
}
