package nl.gingerbeard.automation.configuration;

import java.util.List;

public interface IConfigurationProvider {

	void disable(String room);

	void enable(String room);

	List<String> getRooms();

	boolean isEnabled(String room);
}
