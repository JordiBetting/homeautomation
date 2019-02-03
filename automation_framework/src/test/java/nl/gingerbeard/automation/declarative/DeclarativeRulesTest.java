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
	private Switch switchOutput1;
	private Switch switchOutput2;
	private Switch switchOutput3;

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
		}
	}

	@BeforeEach
	public void createRules() {
		switchInput = new Switch(1);
		switchOutput1 = new Switch(2);
		switchOutput2 = new Switch(3);
		switchOutput3 = new Switch(4);
		log = new TestLogger();
		deviceManager = new TransmitterToDeviceUpdate(switchInput, switchOutput1, switchOutput2, switchOutput3);
		rules = new DeclarativeRules(log, deviceManager);
	}

	@Test
	public void whenthen_single() {
		rules.when(switchInput, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);

		assertEquals(OnOffState.ON, switchOutput1.getState());
	}

	@Test
	public void whenthen_2conditions() {
		rules.when(switchInput, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON);
		rules.when(switchInput, OnOffState.OFF)//
				.then(switchOutput1, OnOffState.OFF);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.ON, switchOutput1.getState());

		switchInput.setState(OnOffState.OFF);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
	}

	@Test
	public void when_withoutThen_warningLogged() {
		switchInput.setState(OnOffState.ON);

		rules.when(switchInput, OnOffState.ON);
		rules.deviceUpdated(switchInput);

		log.assertContains(LogLevel.WARNING, "No action defined for idx=1, state=ON");
	}

	@Test
	public void when_multipleThen() {
		rules.when(switchInput, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON)//
				.and(switchOutput2, OnOffState.ON);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.ON, switchOutput1.getState());
		assertEquals(OnOffState.ON, switchOutput2.getState());

	}

	@Test
	public void when_multipleThen_2conditions() {
		rules.when(switchInput, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON)//
				.and(switchOutput2, OnOffState.ON);
		rules.when(switchInput, OnOffState.OFF)//
				.then(switchOutput1, OnOffState.OFF)//
				.and(switchOutput2, OnOffState.OFF);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.ON, switchOutput1.getState());
		assertEquals(OnOffState.ON, switchOutput2.getState());

		switchInput.setState(OnOffState.OFF);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
		assertEquals(OnOffState.OFF, switchOutput2.getState());
	}

	@Test
	public void whenThenElse() {
		rules.when(switchInput, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON) //
				.orElse(switchOutput1, OnOffState.OFF);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);

		assertEquals(OnOffState.ON, switchOutput1.getState());

		switchInput.setState(OnOffState.OFF);
		rules.deviceUpdated(switchInput);

		assertEquals(OnOffState.OFF, switchOutput1.getState());
	}

	@Test
	public void whenThenAndOrElseAnd() {
		rules.when(switchInput, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON) //
				.and(switchOutput2, OnOffState.OFF) //
				.orElse(switchOutput1, OnOffState.OFF) //
				.and(switchOutput2, OnOffState.ON);

		switchInput.setState(OnOffState.ON);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.ON, switchOutput1.getState());
		assertEquals(OnOffState.OFF, switchOutput2.getState());

		switchInput.setState(OnOffState.OFF);
		rules.deviceUpdated(switchInput);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
		assertEquals(OnOffState.ON, switchOutput2.getState());
	}
}
