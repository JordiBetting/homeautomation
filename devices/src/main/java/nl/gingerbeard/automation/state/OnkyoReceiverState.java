package nl.gingerbeard.automation.state;

public class OnkyoReceiverState {
	private final OnOffState main;
	private final OnOffState zone2;

	public OnkyoReceiverState(OnOffState main, OnOffState zone2) {
		this.main = main;
		this.zone2 = zone2;
	}

	public OnOffState getMain() {
		return main;
	}

	public OnOffState getZone2() {
		return zone2;
	}

}
