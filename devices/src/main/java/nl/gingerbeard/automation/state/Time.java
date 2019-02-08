package nl.gingerbeard.automation.state;

import java.time.LocalDateTime;
import java.util.Optional;

public final class Time {

	private Optional<LocalDateTime> fixedTime = Optional.empty();

	public Time() {
	}

	public Time(final LocalDateTime fixedTime) {
		this.fixedTime = Optional.of(fixedTime);
	}

	public LocalDateTime getNow() {
		return fixedTime.orElse(LocalDateTime.now());
	}

}
