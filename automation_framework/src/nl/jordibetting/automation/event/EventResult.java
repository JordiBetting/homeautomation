package nl.jordibetting.automation.event;

import java.util.Collection;
import java.util.Optional;

public interface EventResult {

	void add(EventResult call);

	int size();

	Optional<String> get(int index);

	Collection<String> getAll();

}