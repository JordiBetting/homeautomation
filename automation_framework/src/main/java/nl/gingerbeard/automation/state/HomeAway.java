package nl.gingerbeard.automation.state;

public enum HomeAway {
	ALWAYS, HOME, AWAY;

	public boolean meets(final HomeAway other) {
		return equals(other) || other.equals(ALWAYS);
	}
}
