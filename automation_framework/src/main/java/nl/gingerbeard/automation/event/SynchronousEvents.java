package nl.gingerbeard.automation.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.util.ReflectionUtil;

public class SynchronousEvents implements Events {

	private final ListMultimap<Class<?>, Subscriber> callback = ArrayListMultimap.create();
	private final List<Object> subscribers = new ArrayList<>();

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
			final Class<?> interestedIn = method.getParameterTypes()[0];
			callback.put(interestedIn, new Subscriber(subscriber, method));
		}
	}

	private EventResult trigger(final Object event) {
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
			results.add(subscriber.call(event));
		}
	}

	@Override
	public EventResult trigger(final Device event) {
		return trigger((Object) event);
	}

	@Override
	public EventResult trigger(final State event) {
		return trigger((Object) event);
	}

}
