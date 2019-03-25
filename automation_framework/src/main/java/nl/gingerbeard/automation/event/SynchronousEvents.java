package nl.gingerbeard.automation.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import nl.gingerbeard.automation.event.annotations.EventState;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.util.ReflectionUtil;

final class SynchronousEvents implements IEvents {
	private static final EventStateDefaults defaults = new EventStateDefaults();

	private final ListMultimap<Class<?>, Subscriber> callback = ArrayListMultimap.create();
	private final List<Object> subscribers = new ArrayList<>();
	private final IState state;

	private final ILogger log;

	@EventState
	private static class EventStateDefaults {

	}

	public SynchronousEvents(final IState state, final ILogger log) {
		this.state = state;
		this.log = log;
	}

	@Override
	public void subscribe(final Object subscriber) {
		Preconditions.checkArgument(subscriber != null);
		if (!subscribers.contains(subscriber)) {
			subscribers.add(subscriber);
			registerSubscriberInterests(subscriber);
		}
	}

	private void registerSubscriberInterests(final Object subscriber) {
		for (final Method method : ReflectionUtil.getMethodsAnnotatedWith(subscriber.getClass(), Subscribe.class, 1)) {
			final Class<?> eventInterest = method.getParameterTypes()[0];
			final EventState subscriberInterests = getSubscriberInterests(subscriber);
			callback.put(eventInterest, new Subscriber(subscriber, method, subscriberInterests));
		}
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
	public EventResult trigger(final Object event) {
		Preconditions.checkArgument(event != null);

		final EventResult results = new EventResultList();

		final Class<?> eventType = event.getClass();
		for (final Class<?> callbackEventType : callback.keySet()) {
			if (callbackEventType.isAssignableFrom(eventType)) {
				triggerSubscribers(event, results, callbackEventType);
			}
		}
		return results;
	}

	private void triggerSubscribers(final Object event, final EventResult results, final Class<?> callbackEventType) {
		for (final Subscriber subscriber : callback.get(callbackEventType)) {
			if (stateMeets(subscriber.getEventState())) {
				try {
					results.add(subscriber.call(event));
				} catch (final Exception e) {
					log.exception(e, "Received exception during invocation of subscriber. Ignoring.");
				}
			}
		}
	}

	private boolean stateMeets(final EventState eventState) {
		return state.getTimeOfDay().meets(eventState.timeOfDay()) && state.getAlarmState().meets(eventState.alarmState()) && state.getHomeAway().meets(eventState.homeAway());
	}

	public void clear() {
		callback.clear();
		subscribers.clear();
	}

}
