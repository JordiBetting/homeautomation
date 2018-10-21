package nl.gingerbeard.automation.event;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.state.State;

public class StateEventsFilter implements Events {

	@EventState
	private static class EventStateDefaults {

	}

	private static final EventStateDefaults defaults = new EventStateDefaults();

	private final Map<Object, EventState> subscribers = Maps.newHashMap();
	private final State currentState;

	public StateEventsFilter(final State currentState) {
		this.currentState = currentState;
	}

	@Override
	public void subscribe(final Object subscriber) {
		final EventState interests = getSubscriberInterests(subscriber);
		subscribers.put(subscriber, interests);
	}

	private EventState getSubscriberInterests(final Object subscriber) {
		final Class<?> annotatedObject = getStateAnnotation(subscriber);
		final EventState interests = annotatedObject.getAnnotation(EventState.class);
		return interests;
	}

	private Class<?> getStateAnnotation(final Object subscriber) {
		Class<?> annotatedObject;
		if (subscriber.getClass().isAnnotationPresent(EventState.class)) {
			annotatedObject = subscriber.getClass();
		} else {
			annotatedObject = defaults.getClass();
		}
		return annotatedObject;
	}

	@Override
	public EventResult trigger(final Device event) {
		// subscribers.entrySet().stream().filter(doFilter()).forEach(action);
		return null;
	}

	private Predicate<? super Entry<Object, EventState>> doFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventResult trigger(final State event) {
		// TODO Auto-generated method stub
		return null;
	}

}
