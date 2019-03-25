package nl.gingerbeard.automation.configuration;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;

public final class SupportedRequest {
	private final Pattern uriPattern;
	private final Method method;
	private final Function<Request, Response> executor;

	public SupportedRequest(final String uriPattern, final Method method, final Function<Request, Response> executor) {
		this.uriPattern = Pattern.compile(uriPattern);
		this.method = method;
		this.executor = executor;
	}

	public boolean matchesUri(final IHTTPSession session) {
		return uriMatches(session.getUri());
	}

	public boolean matchesMethod(final IHTTPSession session) {
		return method == session.getMethod();
	}

	private boolean uriMatches(final String uri) {
		return uriPattern.matcher(uri).matches();
	}

	public Response execute(final IHTTPSession session) {
		final Matcher matcher = uriPattern.matcher(session.getUri());
		return executor.apply(new Request(createParameterArray(matcher)));
	}

	private String[] createParameterArray(final Matcher matcher) {
		matcher.matches();
		final String[] parameters = new String[matcher.groupCount()];
		for (int i = 0; i < matcher.groupCount(); i++) {
			parameters[i] = matcher.group(i + 1);
		}
		return parameters;
	}
}
