package org.tsaikd.java.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class WebClientTest {

	static Log log = LogFactory.getLog(WebClientTest.class);

	@Test
	public void testHttps() throws Exception {
		CloseableHttpClient client = WebClient.newHttpClient();
		HttpGet method = new HttpGet("https://www.google.com/");
		HttpResponse cliRes = client.execute(method);

		int status = cliRes.getStatusLine().getStatusCode();
		if (status != HttpStatus.SC_OK) {
			throw new RuntimeException("http response status: " + status);
		}

		HttpEntity entity = cliRes.getEntity();
		EntityUtils.consume(entity);
	}

}
