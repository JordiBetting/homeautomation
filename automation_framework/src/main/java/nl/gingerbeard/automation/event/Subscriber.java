package nl.gingerbeard.automation.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Subscriber {
	private final Object instance;
	private final Method method;
	private final EventState eventState;

	Subscriber(final Object instance, final Method method, final EventState eventState) {
		this.instance = instance;
		this.method = method;
		this.eventState = eventState;
	}

	EventResult call(final Object event) {
		try {
			final Object returned = method.invoke(instance, event);
			if (returned != null && EventResult.class.isAssignableFrom(returned.getClass())) {
				return (EventResult) returned;
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.println("Received exception during invocation of subscriber. Ignoring " + e.getClass().getName() + ": " + e.getMessage());
			// e.printStackTrace(); TODO
		}
		return EventResultEmpty.create();
	}

	public EventState getEventState() {
		return eventState;
	}
}
