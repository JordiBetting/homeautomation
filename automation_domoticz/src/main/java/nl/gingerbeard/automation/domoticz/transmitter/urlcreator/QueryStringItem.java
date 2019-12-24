package nl.gingerbeard.automation.domoticz.transmitter.urlcreator;

import java.util.Locale;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

public interface QueryStringItem {
	static final Escaper ESCAPER = UrlEscapers.urlFragmentEscaper();

	String name();

	default String getName() {
		return name().toLowerCase(Locale.US);
	}

	default String getString() {
		final String string = getName();
		final String withSpaces = string.replace('_', ' ');
		return ESCAPER.escape(withSpaces);
	}
}