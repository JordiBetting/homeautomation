package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class OnkyoReceiverTest {

	@Test
	public void updateState_notSupported() {
		OnkyoReceiver onkyo = new OnkyoReceiver();
		assertThrows(UnsupportedOperationException.class, () -> onkyo.updateState("anything"));
	}
	
	@Test
	public void initialState_set() {
		OnkyoReceiver onkyo = new OnkyoReceiver();
		assertNotNull(onkyo.getState());
	}
}
