package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.gingerbeard.automation.state.TimeOfDay;

public class HeatingAutoControlTest {

	private static class TestListener implements AutoControlListener {

		List<NextState<?>> output;
		volatile Optional<CountDownLatch> latch = Optional.empty();

		@Override
		public void outputChanged(String owner, List<NextState<?>> output) {
			this.output = output;
			latch.ifPresent((latch) -> latch.countDown());
		}

	}

	@Test
	public void defaultTemp_armed_lowTemperature() {
		HeatingAutoControl sut = new HeatingAutoControl(new State());
		sut.addThermostat(new Thermostat(1, 2));

		List<NextState<?>> result = sut.alarmChanged(AlarmState.ARM_AWAY);

		assertTemperature(15, result);
	}

	@Test
	public void setTemp_armed_lowTemperature() {
		HeatingAutoControl sut = new HeatingAutoControl(new State());
		sut.addThermostat(new Thermostat(1, 2));
		sut.setOffTemperature(Temperature.celcius(1));

		List<NextState<?>> result = sut.alarmChanged(AlarmState.ARM_AWAY);

		assertTemperature(1, result);
	}

	@Test
	public void daytimeArmed_disarm_heatingOnDelayed() throws InterruptedException {
		TestListener listener = new TestListener();
		listener.latch = Optional.of(new CountDownLatch(1));

		State state = new State();
		state.setTimeOfDay(TimeOfDay.DAYTIME);
		state.setAlarmState(AlarmState.ARM_AWAY);

		HeatingAutoControl sut = new HeatingAutoControl(state);
		sut.addThermostat(new Thermostat(1, 2));
		sut.setDelayOnMillis(500);
		sut.setListener(listener);

		List<NextState<?>> result = sut.alarmChanged(AlarmState.DISARMED);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result); 
		listener.latch.get().await(3, TimeUnit.SECONDS);
		
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY, listener.output);
	}
	
	@Test
	public void daytimeDisarmed_Night_heatingUp() throws InterruptedException {

		State state = new State();
		state.setTimeOfDay(TimeOfDay.DAYTIME);
		state.setAlarmState(AlarmState.DISARMED);

		HeatingAutoControl sut = new HeatingAutoControl(state);
		sut.addThermostat(new Thermostat(1, 2));
		sut.setDelayOnMillis(0);

		sut.alarmChanged(AlarmState.DISARMED);
		List<NextState<?>> result = sut.timeOfDayChanged(TimeOfDay.NIGHTTIME);
		
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT, result);
	}

	private void assertTemperature(double temperature, List<NextState<?>> result) {
		assertFalse(result.isEmpty());
		for (NextState<?> nextState : result) {
			if (nextState.get() instanceof ThermostatMode) {
				assertEquals(ThermostatMode.SETPOINT, nextState.get());
			} else {
				assertEquals(Temperature.celcius(temperature), nextState.get());
			}
		}

	}
}
