package nl.gingerbeard.automation;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.autocontrol.AutoControlToDomoticz;
import nl.gingerbeard.automation.configuration.ConfigurationServerSettings;
import nl.gingerbeard.automation.deviceregistry.DeviceRegistry;
import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.devices.Thermostat;
import nl.gingerbeard.automation.devices.ThermostatModeDevice;
import nl.gingerbeard.automation.devices.ThermostatSetpointDevice;
import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.event.annotations.EventState;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.logging.ILogOutput;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.logging.TestLogger.LogOutputToTestLogger;
import nl.gingerbeard.automation.service.Container;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.HomeAway;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.OnOffState;
import nl.gingerbeard.automation.state.TimeOfDay;
import nl.gingerbeard.automation.testdevices.TestDevice;

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

	private AutomationFrameworkContainer container;
	private LogOutputToTestLogger log;
	private IAutomationFrameworkInterface framework;

	@BeforeEach
	public void initLogger() {
		log = new LogOutputToTestLogger();
	}

	@AfterEach
	public void removeContainer() {
		if (container != null) {
			container.stop();
			container = null;
		}
		log = null;
	}

	@SafeVarargs
	private final void createIntegration(Class<? extends Room> ... rooms) throws InterruptedException {
		DomoticzConfiguration domoticzConfig = new DomoticzConfiguration(0, createMockUrl());
		domoticzConfig.disableInit();
		domoticzConfig.setEventHandlingSynchronous();
		container = IAutomationFrameworkInterface.createFrameworkContainer(domoticzConfig, log, new ConfigurationServerSettings(0));
		container.start(rooms);
		framework = container.getAutomationFramework();
	}

	private URL createMockUrl() {
		try {
			return new URL("http://localhost/mock");
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Expected URL mock to be valid", e);
		}
	}

	private IState getState() {
		final Optional<IState> service = container.getRuntime().getService(IState.class);
		assertTrue(service.isPresent());
		return service.get();
	}

	@Test
	public void timeOfDay_correctState_eventReceived() throws IOException, InterruptedException {
		createIntegration(TimeOfDaySubscriber.class);
		getState().setTimeOfDay(TimeOfDay.DAYTIME);

		framework.deviceChanged(new TestDevice());

		TimeOfDaySubscriber subscriber = framework.getRoom(TimeOfDaySubscriber.class);
		assertEquals(1, subscriber.counter);
	}

	@Test
	public void timeOfDay_OtherState_nothingReceived() throws IOException, InterruptedException {
		createIntegration(TimeOfDaySubscriber.class);
		getState().setTimeOfDay(TimeOfDay.NIGHTTIME);

		final TimeOfDaySubscriber subscriber = framework.getRoom(TimeOfDaySubscriber.class);
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void timeOfDay_allday_received() throws IOException, InterruptedException {
		createIntegration(TimeOfDaySubscriber.class);
		getState().setTimeOfDay(TimeOfDay.DAYTIME);

		framework.deviceChanged(new TestDevice());

		final TimeOfDaySubscriber subscriber = framework.getRoom(TimeOfDaySubscriber.class);
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
	public void alarm_correctState_eventReceived() throws IOException, InterruptedException {
		createIntegration(TimeOfDaySubscriber.class);
		getState().setAlarmState(AlarmState.ARM_HOME);

		final TimeOfDaySubscriber subscriber = framework.getRoom(TimeOfDaySubscriber.class);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void alarm_otherState_nothingReceived() throws IOException, InterruptedException {
		createIntegration(AlarmSubscriber.class);
		getState().setAlarmState(AlarmState.DISARMED);

		final AlarmSubscriber subscriber = framework.getRoom(AlarmSubscriber.class);
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void alarm_all_received() throws IOException, InterruptedException {
		createIntegration(AllAlarmSubscriber.class);
		getState().setAlarmState(AlarmState.ARM_AWAY);

		final AllAlarmSubscriber subscriber = framework.getRoom(AllAlarmSubscriber.class);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void alarm_armed_received() throws IOException, InterruptedException {
		createIntegration(ArmAlarmSubscriber.class);
		getState().setAlarmState(AlarmState.ARM_AWAY);

		final ArmAlarmSubscriber subscriber = framework.getRoom(ArmAlarmSubscriber.class);
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
	public void homeSubscriber_home_received() throws IOException, InterruptedException {
		createIntegration(HomeSubscriber.class);
		getState().setHomeAway(HomeAway.HOME);
		final HomeSubscriber subscriber = framework.getRoom(HomeSubscriber.class);
		
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void homeSubscriber_away_nothingReceived() throws IOException, InterruptedException {
		createIntegration(HomeSubscriber.class);
		getState().setHomeAway(HomeAway.AWAY);

		final HomeSubscriber subscriber = framework.getRoom(HomeSubscriber.class);
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void homeAwayAlwaysSubscriber_away_received() throws IOException, InterruptedException {
		createIntegration(AlwaysHomeAwaySubscriber.class);
		getState().setHomeAway(HomeAway.AWAY);

		final AlwaysHomeAwaySubscriber subscriber = framework.getRoom(AlwaysHomeAwaySubscriber.class);
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void createAndStop_noException() throws InterruptedException {
		createIntegration(TimeOfDaySubscriber.class);
		container.stop();
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
	public void integration_updateEventWebserverReceived_deviceUpdated() throws IOException, InterruptedException {
		createIntegration(TestRoom.class);
		final TestRoom testRoom = framework.getRoom(TestRoom.class);

		updateDevice(1, "on");
		assertEquals(OnOffState.ON, testRoom.testSwitch.getState());
		assertEquals(1, testRoom.updateEventCount);

		updateDevice(1, "off");
		assertEquals(OnOffState.OFF, testRoom.testSwitch.getState());
		assertEquals(2, testRoom.updateEventCount);
	}

	private void updateDevice(final int idx, final String newValue) throws MalformedURLException, IOException, ProtocolException {
		final URL url = new URL("http://localhost:" + getListeningPort() + "/device/" + idx + "/" + newValue + "/");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		assertEquals(200, con.getResponseCode());
	}

	private int getListeningPort() {
		final Optional<DomoticzConfiguration> configurationService = container.getRuntime().getService(DomoticzConfiguration.class);
		assertTrue(configurationService.isPresent());
		final DomoticzConfiguration config = configurationService.get();
		final int port = config.getListenPort();
		
		assertNotEquals(0, port);
		
		return port;
	}

	public static class ThermostatRoom extends Room {
		public ThermostatRoom() {
			final Thermostat thermostat = new Thermostat(2, 1);
			addDevice(thermostat);
		}
	}

	@Test
	public void compositeTest_allDevicesAdded() throws InterruptedException {
		final DeviceRegistry registry = new DeviceRegistry();
		final IAutomationFrameworkInterface framework = new AutomationFramework(mock(IEvents.class), registry, mock(IState.class), mock(AutoControlToDomoticz.class), new TestLogger(), mock(DomoticzApi.class));

		framework.start(ThermostatRoom.class);

		final List<Device<?>> devices = registry.getAllDevices();
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

	public static class FakeDevice implements IDevice<Void> {

		@Override
		public Void getState() {
			return null;
		}

		@Override
		public void setState(final Void newState) {
		}

		@Override
		public int getIdx() {
			return 0;
		}

	}

	public static class RoomWithFakeDevice extends Room {
		public RoomWithFakeDevice() {
			addDevice(new FakeDevice());
		}
	}

	@Test
	public void addUnsupportedDevice_throwsException() {
		final IAutomationFrameworkInterface framework = new AutomationFramework(mock(IEvents.class), mock(IDeviceRegistry.class), mock(IState.class), mock(AutoControlToDomoticz.class), new TestLogger(), mock(DomoticzApi.class));
		assertThrows(UnsupportedOperationException.class, () -> framework.start(RoomWithFakeDevice.class));
	}

	@Test
	public void automationFrameworkContainerTest() throws InterruptedException {
		DomoticzConfiguration domoticzConfig = new DomoticzConfiguration(0, createMockUrl());
		domoticzConfig.disableInit();
		final AutomationFrameworkContainer container = IAutomationFrameworkInterface.createFrameworkContainer(domoticzConfig, new ConfigurationServerSettings(0));
		container.start();
		final IAutomationFrameworkInterface framework = container.getAutomationFramework();
		final Container runtime = container.getRuntime();
		container.stop();

		assertNotNull(framework);
		assertNotNull(runtime);
	}

	@Test
	public void automationFramework() throws MalformedURLException, ProtocolException, IOException, InterruptedException {
		final ILogOutput logOut = mock(ILogOutput.class);
		DomoticzConfiguration domoticzConfig = new DomoticzConfiguration(0, createMockUrl());
		domoticzConfig.disableInit();
		container = IAutomationFrameworkInterface.createFrameworkContainer(domoticzConfig, logOut, new ConfigurationServerSettings(0));
		container.start(TestRoom.class);
		updateDevice(1, "on");
		container.stop();
	}

	public static class StateRoom extends Room {

		public IState exposeState() {
			return getState();
		}

	}

	@Test
	public void roomState_notAddedToFramework_throwsException() {
		final StateRoom stateroom = new StateRoom();

		final IllegalStateException e = assertThrows(IllegalStateException.class, () -> stateroom.exposeState());
		assertEquals("State is not available when room has not been added to the automation framework.", e.getMessage());
	}

	@Test
	public void roomState_isSystemState() throws InterruptedException {
		createIntegration(StateRoom.class);
		final Optional<IState> optionalState = container.getRuntime().getService(IState.class);
		assertTrue(optionalState.isPresent());
		final IState state = optionalState.get();
		state.setTimeOfDay(TimeOfDay.DAYTIME);

		final StateRoom stateroom = framework.getRoom(StateRoom.class);

		assertDoesNotThrow(() -> stateroom.getState());
		assertEquals(TimeOfDay.DAYTIME, stateroom.getState().getTimeOfDay());
		state.setTimeOfDay(TimeOfDay.NIGHTTIME);
		assertEquals(TimeOfDay.NIGHTTIME, stateroom.getState().getTimeOfDay());
	}

	public static class WorkingRoom extends Room {

	}

	@Test
	public void createRoom() {
		final AutomationFramework automation = new AutomationFramework(mock(IEvents.class), mock(IDeviceRegistry.class), mock(IState.class), mock(AutoControlToDomoticz.class), new TestLogger(), mock(DomoticzApi.class));

		final WorkingRoom room = automation.createRoom(WorkingRoom.class);

		assertNotNull(room);
	}

	public static class ThrowingNPEConstructorRoom extends Room {
		public ThrowingNPEConstructorRoom() {
			throw new NullPointerException("Test NPE");
		}
	}

	@Test
	public void createRoom_constructorThrowsNPE_exceptionThrown() {
		final AutomationFramework automation = new AutomationFramework(mock(IEvents.class), mock(IDeviceRegistry.class), mock(IState.class), mock(AutoControlToDomoticz.class), new TestLogger(), mock(DomoticzApi.class));

		final NullPointerException e = assertThrows(NullPointerException.class, () -> automation.createRoom(ThrowingNPEConstructorRoom.class));
		assertEquals("Test NPE", e.getMessage());
	}

	public static class ThrowingCheckedExceptionConstructorRoom extends Room {
		public ThrowingCheckedExceptionConstructorRoom() throws IOException {
			throw new IOException("TEST IOE");
		}
	}

	@Test
	public void createRoom_constructorThrowsChecked_exceptionWrappedInRTE() {
		final AutomationFramework automation = new AutomationFramework(mock(IEvents.class), mock(IDeviceRegistry.class), mock(IState.class), mock(AutoControlToDomoticz.class), new TestLogger(), mock(DomoticzApi.class));

		final RuntimeException e = assertThrows(RuntimeException.class, () -> automation.createRoom(ThrowingCheckedExceptionConstructorRoom.class));

		assertNotNull(e.getCause());
		assertEquals(IOException.class, e.getCause().getClass());
		assertEquals("TEST IOE", e.getCause().getMessage());
	}

	public static class PrivateConstructorRoom extends Room {
		private PrivateConstructorRoom() {
		}
	}

	@Test
	public void createRoom_privateConstructor_throwsException() {
		final AutomationFramework automation = new AutomationFramework(mock(IEvents.class), mock(IDeviceRegistry.class), mock(IState.class), mock(AutoControlToDomoticz.class), new TestLogger(), mock(DomoticzApi.class));

		final RuntimeException e = assertThrows(RuntimeException.class, () -> automation.createRoom(PrivateConstructorRoom.class));
		assertEquals("Is the room and its default constructor public?", e.getMessage());
		assertEquals(NoSuchMethodException.class, e.getCause().getClass());
	}

	@Test
	public void getRoom_notAdded_returnsNull() throws InterruptedException {
		createIntegration(StateRoom.class);
		
		assertNull(framework.getRoom(AlarmSubscriber.class));
	}
	
	@Test
	public void start_collection_allRoomsAdded() throws InterruptedException {
		DomoticzConfiguration domoticzConfig = new DomoticzConfiguration(0, createMockUrl());
		domoticzConfig.disableInit();
		domoticzConfig.setEventHandlingSynchronous();
		container = IAutomationFrameworkInterface.createFrameworkContainer(domoticzConfig, log, new ConfigurationServerSettings(0));
		
		container.start(Lists.newArrayList(StateRoom.class));
		
		assertNotNull(container.getAutomationFramework().getRoom(StateRoom.class));
	}
	
	@Test
	public void initFailed_warningLogged() throws InterruptedException, DomoticzException {
		DomoticzApi domoticz = mock(DomoticzApi.class);
		TestLogger log = new TestLogger();
		final AutomationFramework automation = new AutomationFramework(mock(IEvents.class), mock(IDeviceRegistry.class), mock(IState.class), mock(AutoControlToDomoticz.class), log, domoticz);

		Mockito.doThrow(DomoticzException.class).when(domoticz).syncFullState();
		
		automation.start(StateRoom.class);
		
		log.assertContains(LogLevel.WARNING, "Could not sync full state at startup, continuing without initial state. This may result in misbehaving rules.");
	}
	
	@Test
	public void createRoom_nullRoom_throwsException() {
		final AutomationFramework automation = new AutomationFramework(mock(IEvents.class), mock(IDeviceRegistry.class), mock(IState.class), mock(AutoControlToDomoticz.class), new TestLogger(), mock(DomoticzApi.class));

		final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> automation.start((Class<? extends Room>)null));
		
		assertEquals("Please provide a non-null room", e.getMessage());
	}
	
	
}
