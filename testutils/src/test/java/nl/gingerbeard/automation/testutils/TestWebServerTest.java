package nl.gingerbeard.automation.testutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import fi.iki.elonen.NanoHTTPD.Response.Status;

public class TestWebServerTest {

	@Test
	public void record() throws IOException {
		final TestWebServer webserver = new TestWebServer();
		webserver.start();
		final int port = webserver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/blaat");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode());
		assertEquals(1, webserver.getRequests().size());
	}

	@Test
	public void response() throws IOException {
		final TestWebServer webserver = new TestWebServer();
		webserver.start();
		final int port = webserver.getListeningPort();

		webserver.setResponse("/failure?a=b", Status.METHOD_NOT_ALLOWED, "");

		final URL url = new URL("http://localhost:" + port + "/failure?a=b");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(405, con.getResponseCode());
		assertEquals(1, webserver.getRequests().size());
	}

	@Test
	public void forgetRequest() throws IOException {
		final TestWebServer webserver = new TestWebServer();
		webserver.start();
		final int port = webserver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/blaat?a=b");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode());

		assertEquals(1, webserver.getRequests().size());
		webserver.forgetRequest("GET /blaat?a=b");
		assertEquals(0, webserver.getRequests().size());
	}

	@Test
	public void latchTriggered() throws IOException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		final TestWebServer webserver = new TestWebServer();
		webserver.setRequestLatch(latch);
		webserver.start();
		final int port = webserver.getListeningPort();

		final URL url = new URL("http://localhost:" + port + "/blaat?a=b");
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		assertEquals(200, con.getResponseCode());
		assertTrue(latch.await(10, TimeUnit.SECONDS));
	}

}
