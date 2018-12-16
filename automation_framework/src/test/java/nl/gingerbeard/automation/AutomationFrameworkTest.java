package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.TestDevice;
import nl.gingerbeard.automation.event.EventState;
import nl.gingerbeard.automation.event.Subscribe;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.HomeAway;
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

	@Test
	public void timeOfDay_correctState_eventReceived() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setTimeOfDay(TimeOfDay.DAYTIME);
		final TimeOfDaySubscriber subscriber = new TimeOfDaySubscriber();

		framework.addRoom(subscriber);
		framework.start();

		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void timeOfDay_OtherState_nothingReceived() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setTimeOfDay(TimeOfDay.NIGHTTIME);

		final TimeOfDaySubscriber subscriber = new TimeOfDaySubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void timeOfDay_allday_received() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setTimeOfDay(TimeOfDay.DAYTIME);
		final TimeOfDaySubscriber subscriber = new TimeOfDaySubscriber();

		framework.addRoom(subscriber);
		framework.start();

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
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setAlarmState(AlarmState.ARM_HOME);

		final AlarmSubscriber subscriber = new AlarmSubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void alarm_otherState_nothingReceived() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setAlarmState(AlarmState.DISARMED);

		final AlarmSubscriber subscriber = new AlarmSubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void alarm_all_received() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setAlarmState(AlarmState.ARM_AWAY);

		final AllAlarmSubscriber subscriber = new AllAlarmSubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void alarm_armed_received() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setAlarmState(AlarmState.ARM_AWAY);

		final ArmAlarmSubscriber subscriber = new ArmAlarmSubscriber();
		framework.addRoom(subscriber);
		framework.start();
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
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setHomeAway(HomeAway.HOME);

		final HomeSubscriber subscriber = new HomeSubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void homeSubscriber_away_nothingReceived() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setHomeAway(HomeAway.AWAY);

		final HomeSubscriber subscriber = new HomeSubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new TestDevice());

		assertEquals(0, subscriber.counter);
	}

	@Test
	public void homeAwayAlwaysSubscriber_away_received() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setHomeAway(HomeAway.ALWAYS);

		final AlwaysHomeAwaySubscriber subscriber = new AlwaysHomeAwaySubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new TestDevice());

		assertEquals(1, subscriber.counter);
	}

}
