package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

public class URLBuilderTest {

	@Test
	public void throwMalformedUrl() throws MalformedURLException {
		final URLBuilder builder = new URLBuilder("fakenews");

		final URL result = builder.build();

		assertNull(result);
	}
}
