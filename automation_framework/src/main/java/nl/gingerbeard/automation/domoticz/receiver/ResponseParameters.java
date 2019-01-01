package nl.gingerbeard.automation.domoticz.receiver;

final class ResponseParameters {
	private final int idx;
	private final String state;

	ResponseParameters(final int idx, final String state) {
		this.idx = idx;
		this.state = state;
	}

	int getIdx() {
		return idx;
	}

	String getState() {
		return state;
	}

}