package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.TestDevice;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.devices.ThermostatModeDevice;
import nl.gingerbeard.automation.devices.ThermostatSetpointDevice;
import nl.gingerbeard.automation.domoticz.IDomoticz;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.IDomoticzEventReceiver;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.event.annotations.EventState;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.HomeAway;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.TimeOfDay;

public class AutomationFrameworkTest {

	@EventState(timeOfDay = TimeOfDay.DAYTIME)
	public static class TimeOfDaySubscriber extends Room {

		int counter = 0;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	@EventState(timeOfDay = TimeOfDay.ALLDAY)
	public static class AllDaySubscriber extends Room {

		int counter = 0;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	private Container container;

	@AfterEach
	public void removeContainer() {
		if (container != null) {
			container.shutDown();
			container = null;
		}
	}

	private IAutomationFrameworkInterface createIntegration() {
		container = IAutomationFrameworkInterface.createFrameworkContainer();
		container.register(DomoticzConfiguration.class, new DomoticzConfiguration(0, createMockUrl()), 1);
		container.start();

		final Optional<IAutomationFrameworkInterface> framework = container.getService(IAutomationFrameworkInterface.class);
		assertTrue(framework.isPresent());

		return framework.get();
	}

	private URL createMockUrl() {
		try {
			return new URL("http://localhost/mock");
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Expected URL mock to be valid", e);
		}
	}

	private State getState() {
		final Optional<State> service = container.getService(State.class);
		assertTrue(service.isPresent());
		return service.get();
	}

	@Test
	public void timeOfDay_correctState_eventReceived() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setTimeOfDay(TimeOfDay.DAYTIME);
		final TimeOfDaySubscriber subscriber = new TimeOfDaySubscriber();
		framework.addRoom(subscriber);

		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void timeOfDay_OtherState_nothingReceived() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setTimeOfDay(TimeOfDay.NIGHTTIME);

		final TimeOfDaySubscriber subscriber = new TimeOfDaySubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void timeOfDay_allday_received() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setTimeOfDay(TimeOfDay.DAYTIME);
		final TimeOfDaySubscriber subscriber = new TimeOfDaySubscriber();

		framework.addRoom(subscriber);

		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@EventState(alarmState = AlarmState.ARM_HOME)
	public static class AlarmSubscriber extends Room {

		int counter = 0;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	@EventState(alarmState = AlarmState.ALWAYS)
	public static class AllAlarmSubscriber extends Room {

		int counter = 0;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	@EventState(alarmState = AlarmState.ARMED)
	public static class ArmAlarmSubscriber extends Room {

		int counter = 0;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	@Test
	public void alarm_correctState_eventReceived() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setAlarmState(AlarmState.ARM_HOME);

		final AlarmSubscriber subscriber = new AlarmSubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void alarm_otherState_nothingReceived() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setAlarmState(AlarmState.DISARMED);

		final AlarmSubscriber subscriber = new AlarmSubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void alarm_all_received() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setAlarmState(AlarmState.ARM_AWAY);

		final AllAlarmSubscriber subscriber = new AllAlarmSubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void alarm_armed_received() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setAlarmState(AlarmState.ARM_AWAY);

		final ArmAlarmSubscriber subscriber = new ArmAlarmSubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@EventState(homeAway = HomeAway.HOME)
	public static class HomeSubscriber extends Room {

		int counter;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	@EventState(homeAway = HomeAway.ALWAYS)
	public static class AlwaysHomeAwaySubscriber extends Room {

		int counter;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	@Test
	public void homeSubscriber_home_received() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setHomeAway(HomeAway.HOME);

		final HomeSubscriber subscriber = new HomeSubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void homeSubscriber_away_nothingReceived() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setHomeAway(HomeAway.AWAY);

		final HomeSubscriber subscriber = new HomeSubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void homeAwayAlwaysSubscriber_away_received() throws IOException {
		final IAutomationFrameworkInterface framework = createIntegration();
		getState().setHomeAway(HomeAway.ALWAYS);

		final AlwaysHomeAwaySubscriber subscriber = new AlwaysHomeAwaySubscriber();
		framework.addRoom(subscriber);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void createAndStop_noException() {
		createIntegration();
		container.shutDown();
		container = null;
	}

	public static class TestRoom extends Room {

		public final Switch testSwitch = new Switch(1);
		public int updateEventCount;

		public TestRoom() {
			super();
			addDevice(testSwitch);
		}

		@Subscribe
		public void stateChanged(final Device<?> device) {
			updateEventCount++;
			assertEquals(testSwitch, device);
		}

	}

	@Test
	public void integration_updateEventWebserverReceived_deviceUpdated() throws IOException {
		final TestRoom testRoom = new TestRoom();
		final IAutomationFrameworkInterface framework = createIntegration();
		framework.addRoom(testRoom);

		updateDevice(1, "on");
		assertEquals(OnOffState.ON, testRoom.testSwitch.getState());
		assertEquals(1, testRoom.updateEventCount);

		updateDevice(1, "off");
		assertEquals(OnOffState.OFF, testRoom.testSwitch.getState());
		assertEquals(2, testRoom.updateEventCount);
	}

	private void updateDevice(final int idx, final String newValue) throws MalformedURLException, IOException, ProtocolException {
		final URL url = new URL("http://localhost:" + getListeningPort() + "/" + idx + "/" + newValue + "/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	private int getListeningPort() {
		final Optional<IDomoticzEventReceiver> eventReceiverOptional = container.getService(IDomoticzEventReceiver.class);
		assertTrue(eventReceiverOptional.isPresent());
		final IDomoticzEventReceiver eventReceiver = eventReceiverOptional.get();
		final int port = eventReceiver.getListeningPort();
		assertNotEquals(0, port);
		return port;
	}

	private static class ThermostatRoom extends Room {
		public ThermostatRoom() {
			final Thermostat thermostat = new Thermostat(1, 2);
			addDevice(thermostat);
		}
	}

	@Test
	public void compositeTest_allDevicesAdded() {
		final DomoticzTransmitRecorder recorder = new DomoticzTransmitRecorder();
		final IAutomationFrameworkInterface framework = new AutomationFramework(new MockEvents(), recorder);

		framework.addRoom(new ThermostatRoom());

		final List<Device<?>> devices = recorder.getDevices();
		assertEquals(2, devices.size());

		Optional<ThermostatSetpointDevice> setpoint = Optional.empty();
		Optional<ThermostatModeDevice> mode = Optional.empty();
		for (final Device<?> device : devices) {
			if (device instanceof ThermostatSetpointDevice) {
				setpoint = Optional.ofNullable((ThermostatSetpointDevice) device);
			} else if (device instanceof ThermostatModeDevice) {
				mode = Optional.ofNullable((ThermostatModeDevice) device);
			}
		}

		assertTrue(setpoint.isPresent());
		assertTrue(mode.isPresent());

		assertEquals(1, setpoint.get().getIdx());
		assertEquals(2, mode.get().getIdx());
	}

	private class RoomWithInvalidDevice extends Room {

		public RoomWithInvalidDevice() {
			addDevice(new IDevice<Void>() {

				@Override
				public boolean updateState(final String newState) {
					return false;
				}

				@Override
				public Void getState() {
					return null;
				}

				@Override
				public void setState(final Void newState) {
				}
			});
			addDevice(new Switch(1));
		}
	}

	@Test
	public void roomWithValidAndInvalidDevice_invalidIgnored() {
		final DomoticzTransmitRecorder recorder = new DomoticzTransmitRecorder();
		final IAutomationFrameworkInterface framework = new AutomationFramework(new MockEvents(), recorder);

		framework.addRoom(new RoomWithInvalidDevice());

		final List<Device<?>> devices = recorder.getDevices();
		assertEquals(1, devices.size());

		assertEquals(Switch.class, devices.get(0).getClass());
	}

	private static class DomoticzTransmitRecorder implements IDomoticz {

		private final List<Device<?>> devices = new ArrayList<>();

		@Override
		public boolean addDevice(final Device<?> device) {
			devices.add(device);
			return true;
		}

		public List<Device<?>> getDevices() {
			return devices;
		}

	}

	private static class MockEvents implements IEvents {

		@Override
		public EventResult trigger(final Object event) {
			return null;
		}

		@Override
		public void subscribe(final Object subscriber) {
		}
	}

}
