package org.tsaikd.java.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

public class ConfigHttpClient extends DefaultHttpClient {

	static Log log = LogFactory.getLog(ConfigHttpClient.class);

	public static int DefaultRetryMax = 1;
	public static long DefaultRetryWaitMs = 0;
	public static long DefaultReceiveTimeoutMs = 600000;

	public ConfigHttpClient() {
		super();
		initProxyConfig();
	}

	public ConfigHttpClient(boolean noProxy) {
		super();
		if (!noProxy) {
			initProxyConfig();
		}
	}

	private void initProxyConfig() {
		HttpHost proxy = getDefaultProxy();
		if (proxy != null) {
			setProxy(proxy);
		}
	}

	private static boolean initDefaultProxy = false;
	private static HttpHost defaultProxy = null;
	public static HttpHost getDefaultProxy() {
		if (!initDefaultProxy) {
			initDefaultProxy = true;

			String host;
			int port;

			host = ConfigUtils.get("proxyHost");
			if (host != null) {
				port = ConfigUtils.getInt("proxyPort", 0);
				if (port != 0) {
					defaultProxy = new HttpHost(host, port);
					return defaultProxy;
				}
			}

			host = ConfigUtils.get("http.proxyHost");
			if (host != null) {
				port = ConfigUtils.getInt("http.proxyPort", 0);
				if (port != 0) {
					defaultProxy = new HttpHost(host, port);
					return defaultProxy;
				}
			}
		}
		return defaultProxy;
	}

	public void setProxy(HttpHost proxy) {
		getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	}

}
