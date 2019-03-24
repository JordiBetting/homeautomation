package nl.gingerbeard.automation.event;

import java.util.Collection;
import java.util.Optional;

public interface EventResult {

	/**
	 * Records result.
	 *
	 * @param call
	 *            The result
	 */
	void add(EventResult result);

	/**
	 * Returns the amount of recorded results
	 *
	 * @return
	 */
	int size();

	/**
	 * Returns the result at specified index.
	 *
	 * @param index
	 *            The index to return.
	 * @return The result at index
	 */
	Optional<Object> get(int index);

	/**
	 * Returns all recorded results
	 *
	 * @return all results.
	 */
	Collection<Object> getAll();

	public static EventResult of(final Object newValue) {
		final EventResultList result = new EventResultList();
		result.addResult(newValue);
		return result;
	}

	/**
	 * Returns an empty, immutable EventResult.
	 *
	 * @return empty.
	 */
	public static EventResult empty() {
		return EventResultEmpty.emptyResult;
	}

}