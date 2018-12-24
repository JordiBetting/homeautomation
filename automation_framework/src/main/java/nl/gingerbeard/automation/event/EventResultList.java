package nl.gingerbeard.automation.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

public final class EventResultList implements EventResult {

	private final List<String> results = new ArrayList<>();

	@Override
	public void add(final EventResult call) {
		results.addAll(call.getAll());
	}

	@Override
	public int size() {
		return results.size();
	}

	@Override
	public Optional<String> get(final int index) {
		if (index < results.size() && index >= 0) {
			return Optional.of(results.get(index));
		} else {
			return Optional.empty();
		}
	}

	public static EventResult of(final String string) {
		final EventResultList result = new EventResultList();
		result.results.add(string);
		return result;
	}

	@Override
	public Collection<String> getAll() {
		return ImmutableList.copyOf(results);
	}

}
