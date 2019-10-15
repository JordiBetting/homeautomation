package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SceneTest {
	
	@Test
	public void idx_works() {
		Scene scene = new Scene(42);
		
		assertEquals(42, scene.getIdx());
	}
	
	@Test
	public void updateState_coveredBy_OnOffDevice() {
		assertTrue(OnOffDevice.class.isAssignableFrom(Scene.class));
	}
}
