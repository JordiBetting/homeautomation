package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes.ChainOfCommandType;
import nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes.ThermostatModeType;

public class ChainOfCommandsLearningTest {

	@Test
	public void reflections_getAllSubtypes() {
		final Reflections refl = new Reflections("nl.gingerbeard.automation.domoticz.transmitter.urlcreator.devicetypes");
		@SuppressWarnings("rawtypes")
		final Set<Class<? extends ChainOfCommandType>> subTypesOf = refl.getSubTypesOf(ChainOfCommandType.class);
		assertTrue(subTypesOf.size() > 0);
		assertTrue(subTypesOf.contains(ThermostatModeType.class));
	}
}
