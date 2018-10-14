package nl.jordibetting.automation.controlloop;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import nl.jordibetting.automation.devices.Device;
import nl.jordibetting.automation.event.EventResult;
import nl.jordibetting.automation.event.EventResultEmpty;
import nl.jordibetting.automation.event.Events;

public class ControlloopTest {

	private static class EventsStore implements Events {

		private final List<Object> receivedEvents = new ArrayList<>();

		@Override
		public void subscribe(final Object subscriber) {
		}

		@Override
		public EventResult trigger(final Object event) {
			getReceivedEvents().add(event);
			return EventResultEmpty.create();
		}

		public List<Object> getReceivedEvents() {
			return receivedEvents;
		}

	}

	@Test
	public void testStateChanged() {
		final EventsStore events = new EventsStore();
		final Controlloop control = new Controlloop(events);
		final Device myDevice = new Device();

		assertEquals(0, events.getReceivedEvents().size());

		control.triggerDeviceChanged(myDevice);

		assertEquals(1, events.getReceivedEvents().size());
		assertEquals(myDevice, events.getReceivedEvents().get(0));
	}
}
