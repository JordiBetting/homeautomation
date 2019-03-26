package nl.gingerbeard.automation.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nl.gingerbeard.automation.autocontrol.AutoControl;
import nl.gingerbeard.automation.event.annotations.EventState;

final class Subscriber {
	private final Object instance;
	private final Method method;
	private final EventState eventState;
	boolean enabled = true;

	Subscriber(final Object instance, final Method method, final EventState eventState) {
		this.instance = instance;
		this.method = method;
		this.eventState = eventState;
	}

	EventResult call(final Object event) throws Exception {
		EventResult result = EventResult.empty();
		if (enabled) {
			result = executeCall(event);
		}
		return result;
	}

	private EventResult executeCall(final Object event) throws IllegalAccessException, InvocationTargetException {
		final Object returned = method.invoke(instance, event);
		return handleReturned(returned);
	}

	private EventResult handleReturned(final Object returned) {
		EventResult result = EventResult.empty();
		if (returned != null) {
			if (EventResult.class.isAssignableFrom(returned.getClass())) {
				result = (EventResult) returned;
			} else {
				result = EventResult.of(returned);
			}
		}
		return result;
	}

	public EventState getEventState() {
		return eventState;
	}

	void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	boolean hasSimpleName(final String classSimpleName) {
		return getSimpleName().equals(classSimpleName);
	}

	String getSimpleName() {
		if (instance instanceof AutoControl) {
			final AutoControl control = (AutoControl) instance;
			return control.getOwner().replaceAll(".*\\$", "");
		}
		return instance.getClass().getSimpleName();
	}
}
