package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.autocontrol.heatingstates.StateHeatingOnDaytime;
import nl.gingerbeard.automation.autocontrol.heatingstates.StateHeatingOnNighttime;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
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

		public void assertNoUpdate() {
			assertTrue(output == null || output.size() == 0);
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
		
		assertEquals(0, sut.getContext().delayOnMillis);
	}
	
	@Test
	public void setDelayMillisSetting() {
		initSut();
		
		sut.setDelayOnMillis(42);
		
		assertEquals(42, sut.getContext().delayOnMillis);
	}
	
	@Test
	public void setDelayMinutesSetting() {
		initSut();
		
		sut.setDelayOnMinutes(2);
		
		assertEquals(2 * 60 * 1000, sut.getContext().delayOnMillis);
	}

	@Test
	public void daytimeArmed_disarm_heatingOnDelayed() throws InterruptedException {
		initSut(TimeOfDay.DAYTIME, AlarmState.ARM_AWAY);
		sut.setDelayOnMillis(500);

		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result); 
		
		awaitDelayedOnAssertingSuccess();
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
	}

	private void awaitDelayedOnAssertingSuccess() throws InterruptedException {
		awaitDelayedOutput();
		if (state.getTimeOfDay() == TimeOfDay.DAYTIME) {
			assertEquals(StateHeatingOnDaytime.class, sut.getState());
		} else {
			assertEquals(StateHeatingOnNighttime.class, sut.getState());
		}
	}

	private void awaitDelayedOutput() throws InterruptedException {
		boolean notified = listener.notifyLatch.await(30, TimeUnit.SECONDS);
		assertTrue(notified);
	}

	private void assertDelayedOnNotTriggered() throws InterruptedException {
		boolean notified = listener.notifyLatch.await(1, TimeUnit.SECONDS);
		assertFalse(notified);
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
		awaitDelayedOnAssertingSuccess();
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT);
		
		result = updateTimeOfDay(TimeOfDay.DAYTIME);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY, result);
		
		result = updateAlarm(AlarmState.ARM_AWAY);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
		
		result = updateAlarm(AlarmState.DISARMED);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
		awaitDelayedOnAssertingSuccess();
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
		
		result = updateTimeOfDay(TimeOfDay.NIGHTTIME);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT, result);
		
		result = updateAlarm(AlarmState.ARM_AWAY);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
	}
	
	@Test
	public void nightDisarmed_disarm_noEffect() {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.DISARMED);
		
		List<NextState<?>> result = updateAlarm(AlarmState.DISARMED);
		
		assertEquals(0, result.size());
	}
	
	@Test
	public void onDelay_arm_onCancelled() throws InterruptedException {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.ARM_AWAY);
		sut.setDelayOnMillis(500);

		updateAlarm(AlarmState.DISARMED);
		updateAlarm(AlarmState.ARM_AWAY);

		assertDelayedOnNotTriggered();
	}
	
	@Test
	public void onDelay_disarmAgain_noEffect() throws InterruptedException {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.ARM_AWAY);
		sut.setDelayOnMillis(500);

		updateAlarm(AlarmState.DISARMED);
		updateAlarm(AlarmState.DISARMED);
		
		awaitDelayedOnAssertingSuccess();
	}
	
	@Test
	public void pauseDevices_onAndResume_heatingOff() {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);
		Switch pauseDevice = new Switch(3);
		sut.addPauseDevice(pauseDevice);
		
		List<NextState<?>> result = switchOn(pauseDevice);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
		
		result = switchOff(pauseDevice);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY, result);
	}
	
	@Test
	public void setDelayPauseSecondsToMillis() {
		initSut();
		
		sut.setDelayPauseSeconds(5);
		
		assertEquals(5000, sut.getContext().delayPauseMillis);
	}
	
	@Test
	public void pauseDevices_delayApplied() throws InterruptedException {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);
		Switch pauseDevice = new Switch(3);
		sut.addPauseDevice(pauseDevice);
		sut.setDelayPauseMillis(500);
		
		switchOn(pauseDevice);
		listener.assertNoUpdate();
		
		awaitDelayedOutput();
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
	}

	private List<NextState<?>> switchOn(OnOffDevice pauseDevice) {
		return switchDevice(pauseDevice, OnOffState.ON);
	}
	
	private List<NextState<?>> switchOff(OnOffDevice pauseDevice) {
		return switchDevice(pauseDevice, OnOffState.OFF);
	}
	
	private List<NextState<?>> switchDevice(OnOffDevice pauseDevice, OnOffState newState) {
		pauseDevice.setState(newState);
		return sut.deviceUpdated(null);
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
