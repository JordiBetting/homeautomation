package nl.gingerbeard.automation.declarative;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
	private Switch switchInput1;
	private Switch switchInput2;
	private Switch switchOutput1;
	private Switch switchOutput2;
	private Switch switchOutput3;

	// glue-ing the output of NextState to update device to improve test readability
	private static class TransmitterToDeviceUpdate implements IDeviceUpdate {

		private final Map<Integer, Device<?>> devices = new HashMap<>();
		private Optional<CountDownLatch> latch = Optional.empty();

		public TransmitterToDeviceUpdate(final Device<?>... devices) {
			Arrays.stream(devices).forEach((device) -> this.devices.put(device.getIdx(), device));
		}

		void setLatch(final CountDownLatch latch) {
			this.latch = Optional.ofNullable(latch);
		}

		@Override
		public void updateDevice(final NextState<?> nextState) {
			final Device<?> changedDevice = nextState.getDevice();
			if (devices.containsKey(changedDevice.getIdx())) {
				changedDevice.updateState(nextState.get().toString());
				latch.ifPresent((latch) -> latch.countDown());
			} else {
				fail("Got update of non existent device with idx=" + changedDevice.getIdx() + " : " + changedDevice.toString());
			}
		}
	}

	@BeforeEach
	public void createRules() {
		switchInput1 = new Switch(1);
		switchInput2 = new Switch(2);
		switchOutput1 = new Switch(3);
		switchOutput2 = new Switch(4);
		switchOutput3 = new Switch(5);
		log = new TestLogger();
		deviceManager = new TransmitterToDeviceUpdate(switchInput1, switchInput2, switchOutput1, switchOutput2, switchOutput3);
		rules = new DeclarativeRules(log, deviceManager);
	}

	@Test
	public void whenthen_single() {
		rules.when(switchInput1, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON);

		switchInput1.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);

		assertEquals(OnOffState.ON, switchOutput1.getState());
	}

	@Test
	public void whenthen_2conditions() {
		rules.when(switchInput1, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON);
		rules.when(switchInput1, OnOffState.OFF)//
				.then(switchOutput1, OnOffState.OFF);

		switchInput1.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.ON, switchOutput1.getState());

		switchInput1.setState(OnOffState.OFF);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
	}

	@Test
	public void when_withoutThen_warningLogged() {
		switchInput1.setState(OnOffState.ON);

		rules.when(switchInput1, OnOffState.ON);
		rules.updateDevice(switchInput1);

		log.assertContains(LogLevel.WARNING, "No action defined for idx=1, state=ON");
	}

	@Test
	public void when_multipleThen() {
		rules.when(switchInput1, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON)//
				.and(switchOutput2, OnOffState.ON);

		switchInput1.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.ON, switchOutput1.getState());
		assertEquals(OnOffState.ON, switchOutput2.getState());

	}

	@Test
	public void when_multipleThen_2conditions() {
		rules.when(switchInput1, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON)//
				.and(switchOutput2, OnOffState.ON);
		rules.when(switchInput1, OnOffState.OFF)//
				.then(switchOutput1, OnOffState.OFF)//
				.and(switchOutput2, OnOffState.OFF);

		switchInput1.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.ON, switchOutput1.getState());
		assertEquals(OnOffState.ON, switchOutput2.getState());

		switchInput1.setState(OnOffState.OFF);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
		assertEquals(OnOffState.OFF, switchOutput2.getState());
	}

	@Test
	public void whenThenElse() {
		rules.when(switchInput1, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON) //
				.orElse(switchOutput1, OnOffState.OFF);

		switchInput1.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);

		assertEquals(OnOffState.ON, switchOutput1.getState());

		switchInput1.setState(OnOffState.OFF);
		rules.updateDevice(switchInput1);

		assertEquals(OnOffState.OFF, switchOutput1.getState());
	}

	@Test
	public void whenThenAndOrElseAnd() {
		rules.when(switchInput1, OnOffState.ON)//
				.then(switchOutput1, OnOffState.ON) //
				.and(switchOutput2, OnOffState.OFF) //
				.orElse(switchOutput1, OnOffState.OFF) //
				.and(switchOutput2, OnOffState.ON);

		switchInput1.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.ON, switchOutput1.getState());
		assertEquals(OnOffState.OFF, switchOutput2.getState());

		switchInput1.setState(OnOffState.OFF);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
		assertEquals(OnOffState.ON, switchOutput2.getState());
	}

	@Test
	public void whenAndWhenThen() {
		rules.when(switchInput1, OnOffState.ON) //
				.and(switchInput2, OnOffState.ON) //
				.then(switchOutput1, OnOffState.ON) //
				.orElse(switchOutput1, OnOffState.OFF);

		switchInput1.setState(OnOffState.OFF);
		switchInput2.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.OFF, switchOutput1.getState());

		switchInput1.setState(OnOffState.ON);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.ON, switchOutput1.getState());

		switchInput2.setState(OnOffState.OFF);
		rules.updateDevice(switchInput2);
		assertEquals(OnOffState.OFF, switchOutput1.getState());

		switchInput1.setState(OnOffState.OFF);
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
	}

	@Test
	public void whenForDuration() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		deviceManager.setLatch(latch);

		switchInput1.setState(OnOffState.ON);
		switchOutput1.setState(OnOffState.OFF);

		rules.when(switchInput1, OnOffState.ON) //
				.forDuration(Duration.ofMillis(250)) //
				.then(switchOutput1, OnOffState.ON);

		// trigger and directly check that output is still off (i.e. it has been delayed)
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.OFF, switchOutput1.getState());

		// await outcome of rule
		assertTrue(latch.await(30, TimeUnit.SECONDS)); // returns false on timeout

		// assert that after rule is completd, the output has been updated
		assertEquals(OnOffState.ON, switchOutput1.getState());
	}

	@Test
	public void whenForDuration_cancelled() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		deviceManager.setLatch(latch);

		switchInput1.setState(OnOffState.ON);
		switchOutput1.setState(OnOffState.OFF);

		rules.when(switchInput1, OnOffState.ON) //
				.forDuration(Duration.ofMillis(500)) //
				.then(switchOutput1, OnOffState.ON);

		// trigger and directly check that output is still off (i.e. it has been delayed)
		rules.updateDevice(switchInput1);
		assertEquals(OnOffState.OFF, switchOutput1.getState());
		switchInput1.setState(OnOffState.OFF);
		rules.updateDevice(switchInput1);

		// await outcome of rule
		assertFalse(latch.await(1, TimeUnit.SECONDS)); // returns false on timeout
	}
}
