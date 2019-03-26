package nl.gingerbeard.automation.configuration;

import java.util.List;

import com.google.common.collect.ImmutableList;

public final class Request {
	private final List<String> uriParameters;

	public Request(final List<String> uriParameters) {
		this.uriParameters = uriParameters;
	}

	public List<String> getUriParameters() {
		return ImmutableList.copyOf(uriParameters);
	}

}