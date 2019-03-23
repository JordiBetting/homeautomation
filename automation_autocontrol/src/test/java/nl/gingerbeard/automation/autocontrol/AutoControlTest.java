package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

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

		@Override
		public void outputChanged(final List<NextState<OnOffState>> output) {
			this.output = Optional.of(output);
		}

		public Optional<List<NextState<OnOffState>>> getAndClearOutput() {
			final Optional<List<NextState<OnOffState>>> out = output;
			output = Optional.empty();
			return out;
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

		assertFalse(listener.getAndClearOutput().isPresent());
	}

	// @Test
	// public void delayedExecution() {
	// autoControl.addSensor(sensor1);
	// autoControl.addActuator(actuator);
	//
	// autoControl.setDelayedOff(1, TimeUnit.SECONDS);
	//
	// sensor1.setState(OnOffState.ON);
	//
	// }
}
