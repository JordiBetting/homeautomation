package nl.gingerbeard.automation.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

public final class EventResultList implements EventResult {

	private final List<Object> results = new ArrayList<>();
	private Optional<String> subscriberName = Optional.empty();

	EventResultList() {
	}

	@Override
	public void add(final EventResult call) {
		results.addAll(call.getAll());
	}

	@Override
	public int size() {
		return results.size();
	}

	@Override
	public Optional<Object> get(final int index) {
		if (index < results.size() && index >= 0) {
			return Optional.of(results.get(index));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Collection<Object> getAll() {
		return ImmutableList.copyOf(results);
	}

	public void addResult(final Object newValue) {
		results.add(newValue);
	}

	@Override
	public void setSubscriberName(final String name) {
		subscriberName = Optional.ofNullable(name);
	}

	@Override
	public Optional<String> getSubscriberName() {
		return subscriberName;
	}

}
