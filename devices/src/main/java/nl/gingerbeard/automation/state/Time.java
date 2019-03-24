package nl.gingerbeard.automation.state;

import java.time.LocalDateTime;
import java.util.Optional;

public final class Time {

	private final Optional<LocalDateTime> fixedTime;

	public Time() {
		fixedTime = Optional.empty();
	}

	public Time(final LocalDateTime fixedTime) {
		this.fixedTime = Optional.of(fixedTime);
	}

	public LocalDateTime getNow() {
		return fixedTime.orElse(LocalDateTime.now());
	}

	@Override
	public String toString() {
		return "Time [fixedTime=" + fixedTime + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (fixedTime == null ? 0 : fixedTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Time other = (Time) obj;
		if (fixedTime == null) {
			if (other.fixedTime != null) {
				return false;
			}
		} else if (!fixedTime.equals(other.fixedTime)) {
			return false;
		}
		return true;
	}

}
