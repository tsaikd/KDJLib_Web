package org.tsaikd.java.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.filter.io.EtagHttpResponseWrapper;
import org.tsaikd.java.utils.ServletUtils;

public class CheckEtag implements Filter {

	static Log log = LogFactory.getLog(CheckEtag.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		EtagHttpResponseWrapper ehrw = new EtagHttpResponseWrapper(res, baos);

		// pass the request/response on
		chain.doFilter(req, ehrw);

		ehrw.flushBuffer();

		if (ehrw.getHeader("ETag") == null	// etag already set
				&& ehrw.getStatus() >= 200	// etag no need in error
				&& ehrw.getStatus() < 400) {
			if (ServletUtils.checkEtagIsCached(req, ehrw, ehrw.getEtag())) {
				return;
			}
		}

		ehrw.setContentLength(baos.size());
		ServletOutputStream sos = res.getOutputStream();
		sos.write(baos.toByteArray());
		sos.close();
	}

	@Override
	public void destroy() {
	}

}
