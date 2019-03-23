package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.autocontrol.AutoControl.AutoControlOutputListener;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class AutoControlTest {

	private Switch sensor1, sensor2, actuator;
	private AutoControl autoControl;
	private TestListener listener;

	private static class TestListener implements AutoControlOutputListener {

		private Optional<List<NextState<OnOffState>>> output = Optional.empty();
		private final Optional<CountDownLatch> triggerLatch;

		private TestListener() {
			triggerLatch = Optional.empty();
		}

		private TestListener(final CountDownLatch triggerLatch) {
			this.triggerLatch = Optional.of(triggerLatch);

		}

		@Override
		public void outputChanged(final List<NextState<OnOffState>> output) {
			this.output = Optional.of(output);
			if (triggerLatch.isPresent()) {
				triggerLatch.get().countDown();
			}
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
		autoControl = new AutoControl(listener);
		sensor1 = new Switch(1);
		sensor2 = new Switch(2);
		actuator = new Switch(3);
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
		final List<NextState<OnOffState>> out = triggerAutoControlAndGetResult(sensor);
		for (final NextState<OnOffState> next : out) {
			next.getDevice().setState(next.get());
		}
	}

	private List<NextState<OnOffState>> triggerAutoControlAndGetResult(final Switch sensor) {
		autoControl.switchChanged(sensor);

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
		final Annotation[] declaredAnnotations = AutoControl.class.getDeclaredMethod("switchChanged", Switch.class).getDeclaredAnnotations();

		assertEquals(1, declaredAnnotations.length);
		assertEquals(Subscribe.class, declaredAnnotations[0].annotationType());
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

		autoControl.switchChanged(new Switch(666));

		listener.assertActuatorUnchanged();
	}

	@Test
	public void delayedExecution() throws InterruptedException {
		final CountDownLatch outputReceivedLatch = new CountDownLatch(1);
		final TestListener listener = new TestListener(outputReceivedLatch);
		final AutoControl autoControl = new AutoControl(listener);

		autoControl.addSensor(sensor1);
		autoControl.addActuator(actuator);
		autoControl.setDelayedOff(1, TimeUnit.SECONDS);

		sensor1.setState(OnOffState.OFF);
		autoControl.switchChanged(sensor1);

		listener.assertActuatorUnchanged();
		assertTrue(outputReceivedLatch.await(30, TimeUnit.SECONDS));
		listener.assertActuatorChanged();
	}

	@Test
	public void timeoutExtendedWhenSensorChanged() throws InterruptedException {
		final CountDownLatch outputReceivedLatch = new CountDownLatch(2);
		final TestListener listener = new TestListener(outputReceivedLatch);
		final AutoControl autoControl = new AutoControl(listener);

		autoControl.addSensor(sensor1);
		autoControl.addActuator(actuator);
		autoControl.setDelayedOff(1, TimeUnit.SECONDS);

		sensor1.setState(OnOffState.OFF);
		final long startTime = System.currentTimeMillis();
		autoControl.switchChanged(sensor1);
		listener.assertActuatorUnchanged();

		Thread.sleep(500);

		sensor1.setState(OnOffState.ON);
		autoControl.switchChanged(sensor1); // should extend timeout
		listener.assertActuatorChanged();

		sensor1.setState(OnOffState.OFF);
		autoControl.switchChanged(sensor1);
		listener.assertActuatorUnchanged();

		assertTrue(outputReceivedLatch.await(30, TimeUnit.SECONDS));
		final long endTime = System.currentTimeMillis();
		listener.assertActuatorChanged();
		assertTrue(endTime - startTime >= 1500);
	}
}
