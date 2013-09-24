package org.tsaikd.java.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class WebRequestBuilder {

	static Log log = LogFactory.getLog(WebRequestBuilder.class);

	protected enum METHOD {
		GET,
		POST,
	}

	protected METHOD method = METHOD.GET;

	protected String url;

	protected LinkedList<BasicNameValuePair> urlParams = new LinkedList<>();

	protected LinkedList<BasicNameValuePair> bodyParams = new LinkedList<>();

	protected WebRequestBuilder() {}

	public static WebRequestBuilder get() {
		WebRequestBuilder builder = new WebRequestBuilder();
		builder.method = METHOD.GET;
		return builder;
	}

	public static WebRequestBuilder get(String url) {
		return get().setUrl(url);
	}

	public static WebRequestBuilder post() {
		WebRequestBuilder builder = new WebRequestBuilder();
		builder.method = METHOD.POST;
		return builder;
	}

	public static WebRequestBuilder post(String url) {
		return post().setUrl(url);
	}

	public WebRequestBuilder setUrl(String url) {
		this.url = url;
		return this;
	}

	public WebRequestBuilder addUrlParam(String name, String value) {
		urlParams.add(new BasicNameValuePair(name, value));
		return this;
	}

	public WebRequestBuilder clearUrlParam() {
		urlParams.clear();
		return this;
	}

	public WebRequestBuilder addBodyParam(String name, String value) {
		bodyParams.add(new BasicNameValuePair(name, value));
		return this;
	}

	public WebRequestBuilder clearBodyParam() {
		bodyParams.clear();
		return this;
	}

	protected String buildReqUrl() {
		String appendurl = "";
		if (!urlParams.isEmpty()) {
			appendurl += (url.indexOf('?') >= 0) ? "&" : "?";
			appendurl += URLEncodedUtils.format(urlParams, "UTF-8");
		}
		return url + appendurl;
	}

	protected HttpUriRequest buildGet() {
		HttpGet request = new HttpGet(buildReqUrl());
		return request;
	}

	protected HttpUriRequest buildPost() {
		HttpPost request = new HttpPost(buildReqUrl());
		if (!bodyParams.isEmpty()) {
			try {
				request.setEntity(new UrlEncodedFormEntity(bodyParams, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return request;
	}

	public HttpUriRequest build() {
		switch (method) {
		case POST:
			return buildPost();
		default:
			return buildGet();
		}
	}

	public CloseableHttpResponse executeToString() {
		try {
			CloseableHttpClient httpClient = WebClient.newHttpClient();
			CloseableHttpResponse response = httpClient.execute(build());
			response.setEntity(new StringEntity(EntityUtils.toString(response.getEntity())));
			httpClient.close();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
