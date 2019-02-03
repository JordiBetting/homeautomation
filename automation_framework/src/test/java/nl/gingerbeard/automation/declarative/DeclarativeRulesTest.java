package nl.gingerbeard.automation.declarative;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class DeclarativeRulesTest {

	private DeclarativeRules rules;
	private TransmitterToDeviceUpdate deviceManager;
	private TestLogger log;
	private Switch switchInput;
	private Switch switchOutput;

	// glue-ing the output of NextState to update device to improve test readability
	private static class TransmitterToDeviceUpdate implements IDeviceUpdate {

		private final Map<Integer, Device<?>> devices = new HashMap<>();

		public TransmitterToDeviceUpdate(final Device<?>... devices) {
			Arrays.stream(devices).forEach((device) -> this.devices.put(device.getIdx(), device));
		}

		@Override
		public void updateDevice(final NextState<?> nextState) {
			final Device<?> changedDevice = nextState.getDevice();
			if (devices.containsKey(changedDevice.getIdx())) {
				changedDevice.updateState(nextState.get().toString());
			} else {
				fail("Got update of non existent device with idx=" + changedDevice.getIdx() + " : " + changedDevice.toString());
			}
			// nextState.getDevice()
		}
	}

	@BeforeEach
	public void createRules() {
		switchInput = new Switch(1);
		switchOutput = new Switch(2);
		log = new TestLogger();
		deviceManager = new TransmitterToDeviceUpdate(switchInput, switchOutput);
		rules = new DeclarativeRules(log, deviceManager);
	}

	@Test
	public void whenthen_single() {

		rules.when(switchInput, OnOffState.ON).then(switchOutput, OnOffState.ON);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);

		assertEquals(OnOffState.ON, switchOutput.getState());
	}

	@Test
	public void whenthen_2conditions() {
		final Switch switchInput = new Switch(1);
		final Switch switchOutput = new Switch(2);

		rules.when(switchInput, OnOffState.ON).then(switchOutput, OnOffState.ON);
		rules.when(switchInput, OnOffState.OFF).then(switchOutput, OnOffState.OFF);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.ON, switchOutput.getState());

		switchInput.setState(OnOffState.OFF);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.OFF, switchOutput.getState());
	}

	@Test
	public void when_withoutThen_warningLogged() {

		final Switch switchInput = new Switch(1);
		switchInput.setState(OnOffState.ON);

		rules.when(switchInput, OnOffState.ON);
		rules.deviceUpdated(switchInput);

		log.assertContains(LogLevel.WARNING, "No action defined for idx=1, state=ON");
	}

}
