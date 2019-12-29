package nl.gingerbeard.automation.autocontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.autocontrol.heating.states.StateHeatingOff;
import nl.gingerbeard.automation.autocontrol.heating.states.StateHeatingOnDaytime;
import nl.gingerbeard.automation.autocontrol.heating.states.StateHeatingOnNighttime;
import nl.gingerbeard.automation.devices.DoorSensor;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.OnOffDevice;
import nl.gingerbeard.automation.devices.OpenCloseDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.OpenCloseState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;
import nl.gingerbeard.automation.state.TimeOfDay;

public class HeatingAutoControlTest {

	private class TestListener implements AutoControlListener {

		private List<NextState<?>> output = new ArrayList<>();
		volatile CountDownLatch notifyLatch;

		public TestListener() {
			reset();
		}
		
		@Override
		public void outputChanged(String owner, List<NextState<?>> output) {
			this.output.addAll(output);
			notifyLatch.countDown();
		}

		public void assertTemperature(double expectedTemperature) {
			HeatingAutoControlTest.this.assertTemperature(expectedTemperature, output);
			reset();
		}

		public void reset() {
			output.clear();
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
	private TestLogger log;
	
	private void initSut() {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.DISARMED);
	}
	
	private void initStartupState(TimeOfDay initialTimeOfDay, AlarmState initialAlarmState) {
		state = new State();
		state.alarm = initialAlarmState;
		state.setTimeOfDay(initialTimeOfDay);
		initSut(state);
	}
	
	private void initSut(TimeOfDay initialTimeOfDay, AlarmState initialAlarmState) {
		initStartupState(initialTimeOfDay, initialAlarmState);
		listener.reset();
	}

	private void initSut(State state) {
		this.state = state;
		sut = new HeatingAutoControl();
		listener = new TestListener();
		testDevice = new Thermostat(2, 1);
		sut.addThermostat(testDevice);
		log = new TestLogger();
		sut.init(listener, state, log);
	}
	
	@Test
	public void implementsAutoControlInterface() {
		initSut(); // adds a thermostat
		Switch testDevice2 = new Switch(5);
		DoorSensor testDevice3 = new DoorSensor(4);
		sut.addPauseDevice(testDevice3);
		sut.addPauseDevice(testDevice2);
		List<IDevice<?>> devices = sut.getDevices();
		
		assertEquals(3, devices.size());
		assertEquals(testDevice, devices.get(0));
		assertEquals(testDevice2, devices.get(1));
		assertEquals(testDevice3, devices.get(2));
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
			assertEquals(StateHeatingOnDaytime.class, sut.getControlState());
		} else {
			assertEquals(StateHeatingOnNighttime.class, sut.getControlState());
		}
	}

	private void awaitDelayedOutput() throws InterruptedException {
		boolean notified = listener.notifyLatch.await(10, TimeUnit.SECONDS);
		assertTrue(notified);
	}

	private void assertDelayedNotTriggered() throws InterruptedException {
		boolean notified = listener.notifyLatch.await(1, TimeUnit.SECONDS);
		assertFalse(notified);
	}

	@Test
	public void daytimeDisarmed_Night_heatingUp() throws InterruptedException {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);

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

		assertDelayedNotTriggered();
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
		Switch pauseDevice = addPauseDevice();
		
		List<NextState<?>> result = switchOn(pauseDevice);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF, result);
		
		result = switchOff(pauseDevice);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY, result);
	}
	

	@Test
	public void pauseDevices_on_logged() {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);
		Switch pauseDevice = addPauseDevice();
		
		switchOn(pauseDevice);
		log.assertContains(LogLevel.INFO, "HeatingAutoControl for HeatingAutoControlTest detected that pause device Device [idx=3, name=Optional.empty, state=ON] now has state ON");
	}
	
	@Test
	public void setDelayPauseSecondsToMillis() {
		initSut();
		
		sut.setDelayPauseSeconds(5);
		
		assertEquals(5000, sut.getContext().delayPauseMillis);
	}
	
	@Test
	public void daytime_pauseDevices_delayApplied() throws InterruptedException {
		initSut(TimeOfDay.DAYTIME, AlarmState.DISARMED);
		sut.setDelayPauseMillis(500);
		DoorSensor pauseDevice = addPauseDeviceDoorSensor();
		
		switchOn(pauseDevice);
		listener.assertNoUpdate();
		awaitDelayedOutput();
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
		
		List<NextState<?>> result = switchOff(pauseDevice);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY, result);
	}
	
	@Test
	public void nighttime_pauseDevices_delayApplied() throws InterruptedException {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.DISARMED);
		sut.setDelayPauseMillis(500);
		Switch pauseDevice = addPauseDevice();
		
		switchOn(pauseDevice);
		listener.assertNoUpdate();
		
		awaitDelayedOutput();
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
		
		List<NextState<?>> result = switchOff(pauseDevice);
		assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT, result);
	}

	private Switch addPauseDevice() {
		Switch pauseDevice = new Switch(3);
		sut.addPauseDevice(pauseDevice);
		return pauseDevice;
	}
	
	private DoorSensor addPauseDeviceDoorSensor() {
		DoorSensor doorSensor = new DoorSensor(4);
		sut.addPauseDevice(doorSensor);
		return doorSensor;
	}
	
	@Test
	public void armed_pauseDeviceOnOff_ignored() throws InterruptedException {
		initSut(TimeOfDay.DAYTIME, AlarmState.ARM_AWAY);
		Switch pauseDevice = addPauseDevice();
		
		switchOn(pauseDevice);
		assertDelayedNotTriggered();
		
		switchOff(pauseDevice);
		assertDelayedNotTriggered();
	}

	@Test
	public void onPauseDelay_armed_delayIgnored() throws InterruptedException {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.DISARMED);
		Switch pauseDevice = addPauseDevice();
		sut.setDelayPauseMillis(500);

		switchOn(pauseDevice);
		updateAlarm(AlarmState.ARM_AWAY);

		assertDelayedNotTriggered();
	}
	
	@Test
	public void onPauseDelay_pauseDeviceOffWithinTimeout_delayIgnored() throws InterruptedException {
		initSut(TimeOfDay.NIGHTTIME, AlarmState.DISARMED);
		Switch pauseDevice = addPauseDevice();
		sut.setDelayPauseMillis(500);

		switchOn(pauseDevice);
		switchOff(pauseDevice);

		assertDelayedNotTriggered();
	}
	
	@Test
	public void stateChange_logged() {
		initSut(TimeOfDay.DAYTIME, AlarmState.ARM_AWAY);

		updateAlarm(AlarmState.DISARMED);
		
		log.assertContains(LogLevel.INFO, "[INFO] [root] HeatingAutoControl for HeatingAutoControlTest changing state from StateHeatingOff to StateHeatingOnDelay");
		log.assertContains(LogLevel.INFO, "[INFO] [root] HeatingAutoControl for HeatingAutoControlTest changing state from StateHeatingOnDelay to StateHeatingOnDaytime");
	}
	
	@Test
	public void init_onNightTime() {
		initStartupState(TimeOfDay.NIGHTTIME, AlarmState.DISARMED);
		
		assertEquals(StateHeatingOnNighttime.class, sut.getControlState());
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT);
	}
	
	@Test
	public void init_onDaytime() {
		initStartupState(TimeOfDay.DAYTIME, AlarmState.DISARMED);
		
		assertEquals(StateHeatingOnDaytime.class, sut.getControlState());
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
	}

	@Test
	public void init_offNightTime() {
		initStartupState(TimeOfDay.NIGHTTIME, AlarmState.ARM_AWAY);
		
		assertEquals(StateHeatingOff.class, sut.getControlState());
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
	}
	
	@Test
	public void init_offDayTime() {
		initStartupState(TimeOfDay.DAYTIME, AlarmState.ARM_AWAY);
		
		assertEquals(StateHeatingOff.class, sut.getControlState());
		listener.assertTemperature(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
	}
	
	private List<NextState<?>> switchOn(OnOffDevice pauseDevice) {
		return switchDevice(pauseDevice, OnOffState.ON);
	}
	
	private List<NextState<?>> switchOff(OnOffDevice pauseDevice) {
		return switchDevice(pauseDevice, OnOffState.OFF);
	}
	
	private List<NextState<?>> switchOff(OpenCloseDevice pauseDevice) {
		pauseDevice.setState(OpenCloseState.CLOSE);
		return sut.deviceChanged(pauseDevice);
	}
	
	private List<NextState<?>> switchOn(OpenCloseDevice pauseDevice) {
		pauseDevice.setState(OpenCloseState.OPEN);
		return sut.deviceChanged(pauseDevice);
	}

	private List<NextState<?>> switchDevice(OnOffDevice pauseDevice, OnOffState newState) {
		pauseDevice.setState(newState);
		return sut.deviceChanged(pauseDevice);
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
