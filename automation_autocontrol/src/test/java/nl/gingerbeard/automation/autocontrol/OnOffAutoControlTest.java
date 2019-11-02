package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.DimmeableLight;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.ThermostatModeDevice;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.Level;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class OnOffAutoControlTest {

	private Switch sensor1, sensor2, actuator;
	private OnOffAutoControl<Switch, OnOffState, OnOffState> autoControl;
	private TestListener listener;

	private class TestListener implements AutoControlListener {

		private Optional<List<NextState<OnOffState>>> output = Optional.empty();
		private final Optional<CountDownLatch> triggerLatch;

		private TestListener() {
			triggerLatch = Optional.empty();
		}

		private TestListener(final CountDownLatch triggerLatch) {
			this.triggerLatch = Optional.of(triggerLatch);

		}

		@SuppressWarnings("unchecked")
		@Override
		public void outputChanged(final String owner, final List<NextState<?>> output) {
			final List<NextState<OnOffState>> onoffOut = new ArrayList<>();
			output.stream().forEach((out) -> onoffOut.add((NextState<OnOffState>) out));
			this.output = Optional.of(onoffOut);
			applyNextState(onoffOut);
			triggerLatch.ifPresent((latch) -> latch.countDown());
		}

		public Optional<List<NextState<OnOffState>>> getAndClearOutput() {
			final Optional<List<NextState<OnOffState>>> out = output;
			reset();
			return out;
		}

		private void reset() {
			output = Optional.empty();
		}

		public void assertActuatorUnchanged() {
			if (output.isPresent()) {
				fail("Expected no output, but output was: " + output.get());
			}
			reset();
		}

		public void assertActuatorChanged() {
			if (!output.isPresent()) {
				fail("Expected output to be present, but no output was received");
			}
			reset();
		}

	}

	@BeforeEach
	public void initSensorsActuators() {
		listener = new TestListener();
		autoControl = new OnOffAutoControl<>(OnOffState.ON, OnOffState.ON, OnOffState.OFF);
		autoControl.setListener(listener);
		sensor1 = new Switch(1);
		sensor2 = new Switch(2);
		actuator = new Switch(3);
	}
	
	@Test
	public void implementsAutoControlInterface() {
		autoControl.addActuator(actuator);
		autoControl.addSensor(sensor1);
		autoControl.addSensor(sensor2);
		
		List<IDevice<?>> devices = autoControl.getDevices();
		assertEquals(3, devices.size());
		assertTrue(devices.contains(actuator));
		assertTrue(devices.contains(sensor1));
		assertTrue(devices.contains(sensor2));
	}

	@Test
	public void onOffSync() {
		actuator.setState(OnOffState.OFF);
		autoControl.addSensor(sensor1);
		autoControl.addActuator(actuator);

		sensor1.setState(OnOffState.ON);
		triggerAutoControl(sensor1);
		assertEquals(OnOffState.ON, actuator.getState());

		sensor1.setState(OnOffState.OFF);
		triggerAutoControl(sensor1);
		assertEquals(OnOffState.OFF, actuator.getState());
	}

	private void triggerAutoControl(final Switch sensor) {
		triggerAutoControlAndGetResult(sensor);
	}

	<T> void applyNextState(final List<NextState<T>> out) {
		for (final NextState<T> next : out) {
			next.getDevice().setState(next.get());
		}
	}

	private List<NextState<OnOffState>> triggerAutoControlAndGetResult(final Switch sensor) {
		autoControl.sensorChanged(sensor);

		final Optional<List<NextState<OnOffState>>> output = listener.getAndClearOutput();
		assertTrue(output.isPresent());

		return output.get();
	}

	@Test
	public void switchChanged_noActuator_emptyList() {
		actuator.setState(OnOffState.OFF);
		autoControl.addSensor(sensor1);

		sensor1.setState(OnOffState.ON);
		final List<NextState<OnOffState>> output = triggerAutoControlAndGetResult(sensor1);

		assertEquals(0, output.size());
	}

	@Test
	public void hasSubscribeMethod() throws NoSuchMethodException, SecurityException {
		boolean found = false;
		final Method[] methods = OnOffAutoControl.class.getDeclaredMethods();
		for (final Method method : methods) {
			if ("sensorChanged".equals(method.getName()) && method.isAnnotationPresent(Subscribe.class)) {
				found = true;
				break;
			}
		}

		assertTrue(found);
	}

	@Test
	public void subscriberCalledWithWrongType_noException() {
		assertDoesNotThrow(() -> autoControl.sensorChanged(new ThermostatModeDevice(666)));
	}

	@Test
	public void multipleSensors_actuatorOnWhenAtLeast1SensorOn() {
		actuator.setState(OnOffState.OFF);
		autoControl.addSensor(sensor1);
		autoControl.addSensor(sensor2);
		autoControl.addActuator(actuator);

		sensor1.setState(OnOffState.ON);
		sensor2.setState(OnOffState.OFF);

		triggerAutoControl(sensor1);
		assertEquals(OnOffState.ON, actuator.getState());

		triggerAutoControl(sensor2);
		assertEquals(OnOffState.ON, actuator.getState());

		sensor2.setState(OnOffState.ON);
		triggerAutoControl(sensor2);
		assertEquals(OnOffState.ON, actuator.getState());

		sensor1.setState(OnOffState.OFF);
		sensor2.setState(OnOffState.OFF);
		triggerAutoControl(sensor1);
		assertEquals(OnOffState.OFF, actuator.getState());
	}

	@Test
	public void unknownDeviceUpdated_noOutput() {
		autoControl.addSensor(sensor1);

		autoControl.sensorChanged(new Switch(666));

		listener.assertActuatorUnchanged();
	}

	@Test
	public void delayedExecution() throws InterruptedException {
		final CountDownLatch outputReceivedLatch = new CountDownLatch(1);
		final TestListener listener = new TestListener(outputReceivedLatch);
		final OnOffAutoControl<Switch, OnOffState, OnOffState> autoControl = new OnOffAutoControl<>(OnOffState.ON, OnOffState.ON, OnOffState.OFF);
		autoControl.setListener(listener);

		autoControl.addSensor(sensor1);
		autoControl.addActuator(actuator);
		autoControl.setDelayedOff(1, TimeUnit.SECONDS);

		sensor1.setState(OnOffState.OFF);
		autoControl.sensorChanged(sensor1);

		listener.assertActuatorUnchanged();
		assertTrue(outputReceivedLatch.await(30, TimeUnit.SECONDS));
		listener.assertActuatorChanged();
	}

	@Test
	public void timeoutExtendedWhenSensorChanged() throws InterruptedException {
		final CountDownLatch outputReceivedLatch = new CountDownLatch(2);
		final TestListener listener = new TestListener(outputReceivedLatch);
		final OnOffAutoControl<Switch, OnOffState, OnOffState> autoControl = new OnOffAutoControl<>(OnOffState.ON, OnOffState.ON, OnOffState.OFF);
		autoControl.setListener(listener);

		autoControl.addSensor(sensor1);
		autoControl.addActuator(actuator);
		autoControl.setDelayedOff(1, TimeUnit.SECONDS);

		sensor1.setState(OnOffState.OFF);
		final long startTime = System.currentTimeMillis();
		autoControl.sensorChanged(sensor1);
		listener.assertActuatorUnchanged();

		Thread.sleep(500);

		sensor1.setState(OnOffState.ON);
		autoControl.sensorChanged(sensor1); // should extend timeout
		listener.assertActuatorChanged();

		sensor1.setState(OnOffState.OFF);
		autoControl.sensorChanged(sensor1);
		listener.assertActuatorUnchanged();

		assertTrue(outputReceivedLatch.await(30, TimeUnit.SECONDS));
		final long endTime = System.currentTimeMillis();
		listener.assertActuatorChanged();
		assertTrue(endTime - startTime >= 1500);
	}

	@Test
	public void differentInputOutputTypes() {
		final CountDownLatch outputReceivedLatch = new CountDownLatch(2);
		final TestListener listener = new TestListener(outputReceivedLatch);
		final OnOffAutoControl<DimmeableLight, Level, OnOffState> autoControl = new OnOffAutoControl<>(new Level(40), OnOffState.ON, OnOffState.OFF);
		autoControl.setListener(listener);

		final DimmeableLight dimLight = new DimmeableLight(42);
		autoControl.addSensor(dimLight);
		autoControl.addActuator(actuator);

		dimLight.setState(new Level(1));
		autoControl.sensorChanged(dimLight);
		assertEquals(OnOffState.OFF, actuator.getState());

		dimLight.setState(new Level(40));
		autoControl.sensorChanged(dimLight);
		assertEquals(OnOffState.ON, actuator.getState());
	}
}
