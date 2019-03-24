package nl.gingerbeard.automation.event;

import java.util.Collection;
import java.util.Optional;

public interface EventResult {

	void add(EventResult call);

	int size();

	Optional<Object> get(int index);

	Collection<Object> getAll();

	public static EventResult of(final Object newValue) {
		final EventResultList result = new EventResultList();
		result.addResult(newValue);
		return result;
	}

	public static EventResult empty() {
		return EventResultEmpty.emptyResult;
	}

	void setSubscriberName(String name);

	Optional<String> getSubscriberName();
}