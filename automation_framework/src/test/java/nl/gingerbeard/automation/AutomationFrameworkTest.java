package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.event.EventState;
import nl.gingerbeard.automation.event.NamedDevice;
import nl.gingerbeard.automation.event.Subscribe;
import nl.gingerbeard.automation.state.TimeOfDay;

public class AutomationFrameworkTest {

	@EventState(timeOfDay = TimeOfDay.DAYTIME)
	public static class StateSubscriber extends Room {

		int counter = 0;

		@Subscribe
		public void receive(final Object event) {
			counter++;
		}
	}

	@Test
	public void testStateSubscribe() {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setTimeOfDay(TimeOfDay.NIGHTTIME);
		final StateSubscriber subscriber = new StateSubscriber();

		framework.addRoom(subscriber);
		framework.start();

		framework.deviceChanged(new NamedDevice("test"));

		assertEquals(1, subscriber.counter);
	}

	@Test
	public void otherState_subscribe_nothingReceived() {
		final AutomationFramework framework = AutomationFramework.create();
		framework.getState().setTimeOfDay(TimeOfDay.NIGHTTIME);

		final StateSubscriber subscriber = new StateSubscriber();
		framework.addRoom(subscriber);
		framework.start();
		framework.deviceChanged(new NamedDevice("test"));

		assertEquals(0, subscriber.counter);
	}
}
