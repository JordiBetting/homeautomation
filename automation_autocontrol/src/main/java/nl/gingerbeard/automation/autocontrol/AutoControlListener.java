package nl.gingerbeard.automation.autocontrol;

import java.util.List;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public interface AutoControlListener {
	void outputChanged(List<NextState<OnOffState>> output);
}