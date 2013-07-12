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

	public static final int inlineBoundSize = 2048;

	public static boolean servletDebug = ConfigUtils.getBool("servlet.debug", false);

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

	private static Object[] getIncludeFile(HttpServletRequest request, String path, String ext) {
		Object ret[] = new Object[2];
		String fixPath;
		if (path.endsWith("." + ext)) {
			fixPath = path.substring(0, path.length() - ext.length() - 1);
		} else {
			fixPath = path;
		}
		if (fixPath.endsWith(".min")) {
			fixPath = fixPath.substring(0, fixPath.length() - 4);
		}

		String testPath;
		File file;
		if (servletDebug) {
			testPath = fixPath + "." + ext;
			file = getRealFile(request, testPath);
		} else {
			testPath = fixPath + ".min." + ext;
			file = getRealFile(request, testPath);
			if (!file.exists()) {
				testPath = fixPath + "." + ext;
				file = getRealFile(request, testPath);
			}
		}
		ret[0] = file;
		ret[1] = testPath;
		return ret;
	}

	public static String getIncludePath(HttpServletRequest request, String path, String ext) {
		Object[] objs = getIncludeFile(request, path, "js");
		File file = (File) objs[0];
		String testPath = (String) objs[1];
		if (file.exists()) {
			testPath += "?" + file.lastModified();
		} else {
			log.warn("Include a non exists file: " + path);
			testPath = path;
		}
		return testPath;
	}

	public static boolean isJspExists(HttpServletRequest request, String path) {
		Object[] objs = getIncludeFile(request, path, "jsp");
		File file = (File) objs[0];
		return file.exists();
	}

	public static boolean isJsExists(HttpServletRequest request, String path) {
		Object[] objs = getIncludeFile(request, path, "js");
		File file = (File) objs[0];
		return file.exists();
	}

	public static void includeJs(JspWriter out, HttpServletRequest request, String path) throws IOException {
		Object[] objs = getIncludeFile(request, path, "js");
		File file = (File) objs[0];
		String testPath = (String) objs[1];
		if (file.exists()) {
			if (!servletDebug && (file.length() < inlineBoundSize)) {
				out.println("<script type=\"text/javascript\">");
				IOUtils.copy(new FileReader(file), out);
				out.println("</script>");
				return;
			}
			testPath += "?" + file.lastModified();
		} else {
			log.warn("Include a non exists file: " + path);
			testPath = path;
		}
		out.println("<script type=\"text/javascript\" src=\"" + testPath + "\"></script>");
	}

	public static boolean isCssExists(HttpServletRequest request, String path) throws IOException {
		Object[] objs = getIncludeFile(request, path, "css");
		File file = (File) objs[0];
		return file.exists();
	}

	public static void includeCss(JspWriter out, HttpServletRequest request, String path) throws IOException {
		Object[] objs = getIncludeFile(request, path, "css");
		File file = (File) objs[0];
		String testPath = (String) objs[1];
		if (file.exists()) {
			if (!servletDebug && (file.length() < inlineBoundSize)) {
				out.println("<style type=\"text/css\">");
				IOUtils.copy(new FileReader(file), out);
				out.println("</style>");
				return;
			}
			testPath += "?" + file.lastModified();
		} else {
			log.warn("Include a non exists file: " + path);
			testPath = path;
		}
		out.println("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + testPath + "\"/>");
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
