package nl.gingerbeard.automation.event;

import java.util.Collection;
import java.util.Optional;

import com.google.common.collect.Lists;

public final class EventResultEmpty implements EventResult {

	static final EventResult emptyResult = new EventResultEmpty();

	EventResultEmpty() {
	}

	@Override
	public void add(final EventResult call) {
		throw new UnsupportedOperationException("Cannot add values to empty event result");
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Optional<Object> get(final int index) {
		return Optional.empty();
	}

	@Override
	public Collection<Object> getAll() {
		return Lists.newArrayList();
	}

}
