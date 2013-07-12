package org.tsaikd.java.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServletUtils {

	static Log log = LogFactory.getLog(ServletUtils.class);

	public static int inlineBoundSize = 2048;

	public static String getRemoteRealIP(HttpServletRequest req) {
		String ip = req.getHeader("x-forwarded-for");
		if (ip == null || ip.trim().isEmpty() || ip.trim().equalsIgnoreCase("unknown")) {
			ip = req.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.trim().isEmpty() || ip.trim().equalsIgnoreCase("unknown")) {
			ip = req.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.trim().isEmpty() || ip.trim().equalsIgnoreCase("unknown")) {
			ip = req.getRemoteAddr();
		}
		return ip;
	}

	public static File getRealFile(HttpServletRequest request, String path) {
		return new File(request.getServletContext().getRealPath(path));
	}

	public static boolean isFileExists(HttpServletRequest request, String path) {
		return getRealFile(request, path).exists();
	}

	public static void includeJs(JspWriter out, HttpServletRequest request, String path) throws IOException {
		File file = getRealFile(request, path);
		if (file.exists()) {
			if (file.length() < inlineBoundSize) {
				out.println("<script type=\"text/javascript\">");
				IOUtils.copy(new FileReader(file), out);
				out.println("</script>");
			} else {
				out.println("<script type=\"text/javascript\" src=\"" + path + "?" + file.lastModified() + "\"></script>");
			}
		} else {
			log.warn("Include a non exists file: " + path);
			out.println("<script type=\"text/javascript\" src=\"" + path + "\"></script>");
		}
	}

	public static void includeCss(JspWriter out, HttpServletRequest request, String path) throws IOException {
		File file = getRealFile(request, path);
		if (file.exists()) {
			if (file.length() < inlineBoundSize) {
				out.println("<style type=\"text/css\">");
				IOUtils.copy(new FileReader(file), out);
				out.println("</style>");
			} else {
				out.println("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + path + "?" + file.lastModified() + "\"/>");
			}
		} else {
			log.warn("Include a non exists file: " + path);
			out.println("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + path + "\"/>");
		}
	}

	public static boolean checkEtagIsCached(HttpServletRequest req, HttpServletResponse res, String etag) {
		res.setHeader("ETag", etag);

		String previousToken = req.getHeader("If-None-Match");
		if ((res.getStatus() < 400) && previousToken != null && previousToken.equals(etag)) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

			String modelLastModified = req.getHeader("If-Modified-Since");
			if (modelLastModified != null) {
				res.setHeader("Last-Modified", modelLastModified);
			}

			return true;
		}

		res.setDateHeader("Last-Modified", System.currentTimeMillis());
		res.setStatus(HttpServletResponse.SC_OK);
		return false;
	}

	public static boolean checkEtagIsCached(HttpServletRequest req, HttpServletResponse res, long modelLastModifiedDateMs) {
		res.setHeader("ETag", Long.toString(modelLastModifiedDateMs));
		res.setDateHeader("Last-Modified", modelLastModifiedDateMs);

		// need to check header date
		long headerDateMs = req.getDateHeader("If-Modified-Since");
		if (headerDateMs > 0) {
			// browser date accuracy only to second
			if ((modelLastModifiedDateMs/1000) > (headerDateMs/1000)) {
				res.setStatus(HttpServletResponse.SC_OK);
				return false;
			}
		}

		// if over expire data, see the Etag
		String previousToken = req.getHeader("If-None-Match");
		if ((res.getStatus() < 400) && previousToken != null && previousToken.equals(String.valueOf(modelLastModifiedDateMs))) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return true;
		}

		// if the model has modified, setup the new modified date
		res.setStatus(HttpServletResponse.SC_OK);
		return false;
	}

}
