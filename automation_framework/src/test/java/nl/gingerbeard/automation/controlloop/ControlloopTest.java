package nl.gingerbeard.automation.controlloop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.event.EventResult;
import nl.gingerbeard.automation.event.EventResultEmpty;
import nl.gingerbeard.automation.event.Events;
import nl.gingerbeard.automation.state.State;

public class ControlloopTest {

	private static class EventsStore implements Events {

		private final List<Object> receivedEvents = new ArrayList<>();

		@Override
		public void subscribe(final Object subscriber) {
		}

		@Override
		public EventResult trigger(final Device event) {
			getReceivedEvents().add(event);
			return EventResultEmpty.create();
		}

		public List<Object> getReceivedEvents() {
			return receivedEvents;
		}

		@Override
		public EventResult trigger(final State event) {
			return null;
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
