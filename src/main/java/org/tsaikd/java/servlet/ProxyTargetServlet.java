package org.tsaikd.java.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>init servlet param: proxytarget</p>
 *
 * example: <pre>
 * @WebInitParam(name = "proxytarget", value = "http://127.0.0.1:8080/webservice")
 * </pre>
 */
public class ProxyTargetServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(ProxyTargetServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String urlString = getInitParameter("proxytarget");
		String queryString = req.getQueryString();

		if (queryString != null && !queryString.isEmpty()) {
			urlString += "?" + queryString;
		}
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		String methodName = req.getMethod();
		conn.setRequestMethod(methodName);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(true);

		for (Enumeration<?> e=req.getHeaderNames() ; e.hasMoreElements() ; ) {
			String headerName = e.nextElement().toString();
			conn.setRequestProperty(headerName, req.getHeader(headerName));
		}

		conn.connect();

		if (methodName.equals("POST")) {
			BufferedInputStream clientToProxyBuf = new BufferedInputStream(req.getInputStream());
			BufferedOutputStream proxyToWebBuf = new BufferedOutputStream(conn.getOutputStream());
			int oneByte;
			while ((oneByte = clientToProxyBuf.read()) != -1) {
				proxyToWebBuf.write(oneByte);
			}
			proxyToWebBuf.flush();
			proxyToWebBuf.close();
			clientToProxyBuf.close();
		}

		int statusCode = conn.getResponseCode();
		res.setStatus(statusCode);
		for (Iterator<?> i=conn.getHeaderFields().entrySet().iterator() ; i.hasNext() ; ) {
			Map.Entry<?, ?> mapEntry = (Map.Entry<?, ?>)i.next();
			if (mapEntry.getKey() != null) {
				res.setHeader(mapEntry.getKey().toString(), ((List<?>)mapEntry.getValue()).get(0).toString());
			}
		}

		BufferedInputStream webToProxyBuf;
		try {
			webToProxyBuf = new BufferedInputStream(conn.getInputStream());
		} catch (FileNotFoundException e) {
			webToProxyBuf = new BufferedInputStream(conn.getErrorStream());
		}
		BufferedOutputStream proxyToClientBuf = new BufferedOutputStream(res.getOutputStream());

		int oneByte;
		while ((oneByte = webToProxyBuf.read()) != -1) {
			proxyToClientBuf.write(oneByte);
		}
		proxyToClientBuf.flush();
		proxyToClientBuf.close();

		webToProxyBuf.close();
		conn.disconnect();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

}
