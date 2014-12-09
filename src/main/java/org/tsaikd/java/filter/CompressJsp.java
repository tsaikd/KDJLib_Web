package org.tsaikd.java.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.filter.io.CompressHttpResponseWrapper;

public class CompressJsp implements Filter {

	static Log log = LogFactory.getLog(CompressJsp.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		if (isEnabled(req, res)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CompressHttpResponseWrapper chrw = new CompressHttpResponseWrapper(res, baos);

			// pass the request/response on
			chain.doFilter(req, chrw);

			chrw.flushBuffer();

			String resbody = chrw.compress();
			res.setContentLength(resbody.getBytes("utf-8").length);
			PrintWriter writer = res.getWriter();
			writer.write(resbody);
			writer.close();
		} else {
			// pass the request/response on
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

	protected boolean isEnabled(HttpServletRequest req, HttpServletResponse res) {
		Boolean isDebug = (Boolean) req.getAttribute("isDebug");
		if (isDebug == null || isDebug == false) {
			return true;
		} else {
			return false;
		}
	}

}
