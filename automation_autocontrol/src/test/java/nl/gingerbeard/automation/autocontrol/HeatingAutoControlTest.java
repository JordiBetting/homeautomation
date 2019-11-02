package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControl.HeatingAutoControlState;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.gingerbeard.automation.state.TimeOfDay;

public class HeatingAutoControlTest {

	private class TestListener implements AutoControlListener {

		private List<NextState<?>> output;
		volatile CountDownLatch notifyLatch;

		public TestListener() {
			reset();
		}
		
		@Override
		public void outputChanged(String owner, List<NextState<?>> output) {
			this.output = output;
			notifyLatch.countDown();
		}

		public void assertTemperature(double expectedTemperature) {
			HeatingAutoControlTest.this.assertTemperature(expectedTemperature, output);
			reset();
		}

		private void reset() {
			output = null;
			notifyLatch = new CountDownLatch(1);
		}

	}

	private State state;
	private HeatingAutoControl sut;
	private TestListener listener;
	private Thermostat testDevice;
	
	private void initSut() {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.DISARMED);
	}
	
	private void initSut(TimeOfDay initialTimeOfDay, AlarmState initialAlarmState) {
		state = new State();
		sut = new HeatingAutoControl(state);
		listener = new TestListener();
		testDevice = new Thermostat(1, 2);
		sut.addThermostat(testDevice);
		sut.setListener(listener);
		updateTimeOfDay(initialTimeOfDay);
		updateAlarm(initialAlarmState);
	}
	
	@Test
	public void implementsAutoControlInterface() {
		initSut(); // adds a single testDevice
		List<IDevice<?>> devices = sut.getDevices();
		
		assertEquals(1, devices.size());
		assertEquals(testDevice, devices.get(0));
	}

	@Test
	public void defaultTemp_armed_dayTemperature() {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);
		
		List<NextState<?>> result = updateAlarm(AlarmState.ARM_AWAY);

		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
	}
	
	@Test
	public void setTemp_armed_dayTemperature() {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);
		sut.setOffTemperature(Temperature.celcius(1));

		List<NextState<?>> result = updateAlarm(AlarmState.ARM_AWAY);

		assertTemperature(1, result);
	}
	
	@Test
	public void defaultTemp_disarmed_nightTemperature() {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.ARM_AWAY);
		
		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT, result);
	}
	
	@Test
	public void setTemp_disarmed_nightTemperature() {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.ARM_AWAY);
		sut.setNighttimeTemperature(Temperature.celcius(42));
		
		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		
		assertTemperature(42, result);
	}
	
	@Test
	public void defaultTemp_disarmed_dayTemperature() {
		initSut(TimeOfDay.DAYTIME, AlarmState.ARM_AWAY);
		
		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY, result);
	}
	
	@Test
	public void setTemp_disarmed_dayTemperature() {
		initSut(TimeOfDay.DAYTIME, AlarmState.ARM_AWAY);
		sut.setDaytimeTemperature(Temperature.celcius(12));

		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		
		assertTemperature(12, result);
	}
	
	@Test
	public void defaultDelaySetting_zero() {
		initSut();
		
		assertEquals(0, sut.getDelayOnMillis());
	}
	
	@Test
	public void setDelayMillisSetting() {
		initSut();
		
		sut.setDelayOnMillis(42);
		
		assertEquals(42, sut.getDelayOnMillis());
	}
	
	@Test
	public void setDelayMinutesSetting() {
		initSut();
		
		sut.setDelayOnMinutes(2);
		
		assertEquals(2 * 60 * 1000, sut.getDelayOnMillis());
	}

	@Test
	public void daytimeArmed_disarm_heatingOnDelayed() throws InterruptedException {
		initSut(TimeOfDay.DAYTIME, AlarmState.ARM_AWAY);
		sut.setDelayOnMillis(500);

		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result); 
		
		awaitDelayedOnAssertingSuccess(listener);
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
	}

	private void awaitDelayedOnAssertingSuccess(TestListener listener) throws InterruptedException {
		boolean notified = listener.notifyLatch.await(30, TimeUnit.SECONDS);
		assertTrue(notified);
		if (state.getTimeOfDay() == TimeOfDay.DAYTIME) {
			assertEquals(HeatingAutoControlState.ON_DAYTIME, sut.getState());
		} else {
			assertEquals(HeatingAutoControlState.ON_EVENING, sut.getState());
		}
		
	}
	
	@Test
	public void daytimeDisarmed_Night_heatingUp() throws InterruptedException {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);

		updateAlarm(AlarmState.DISARMED);
		List<NextState<?>> result = updateTimeOfDay(TimeOfDay.NIGHTTIME);
		
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT, result);
	}
	
	@Test
	public void fullFlowTypicalDay() throws InterruptedException {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.ARM_AWAY);
		sut.setDelayOnMillis(500);
		
		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
		awaitDelayedOnAssertingSuccess(listener);
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT);
		
		result = updateTimeOfDay(TimeOfDay.DAYTIME);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY, result);
		
		result = updateAlarm(AlarmState.ARM_AWAY);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
		
		result = updateAlarm(AlarmState.DISARMED);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
		awaitDelayedOnAssertingSuccess(listener);
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
		
		result = updateTimeOfDay(TimeOfDay.NIGHTTIME);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT, result);
		
		result = updateAlarm(AlarmState.ARM_AWAY);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
	}

	private List<NextState<?>> updateAlarm(AlarmState alarmState) {
		state.setAlarmState(alarmState);
		return sut.alarmChanged(null);
	}
	
	private List<NextState<?>> updateTimeOfDay(TimeOfDay timeOfDay) {
		state.setTimeOfDay(timeOfDay);
		return sut.timeOfDayChanged(null);
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
