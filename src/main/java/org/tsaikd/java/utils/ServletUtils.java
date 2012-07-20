package org.tsaikd.java.utils;

import java.io.File;
import java.io.FileReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServletUtils {

	static Log log = LogFactory.getLog(ServletUtils.class);

	public static final int inlineBoundSize = 0; // 2048

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

	public static void outputFile(JspWriter out, File file) throws Exception {
		FileReader fr = new FileReader(file);
		char[] buffer = new char[8192];
		int len;

		while ((len = fr.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
	}

	public static void includeJs(JspWriter out, HttpServletRequest request, String path) throws Exception {
		String relPath;
		String p = request.getContextPath();
		File file = getRealFile(request, path);
		if (file.exists()) {
			if (file.length() < inlineBoundSize) {
				out.println("<script type=\"text/javascript\">");
				outputFile(out, file);
				out.println("</script>");
				return;
			}
			relPath = p + path + "?" + file.lastModified();
		} else {
			log.warn("Include a non exists file: " + path);
			relPath = p + path;
		}
		String inc = "<script type=\"text/javascript\" src=\"" + relPath + "\"></script>";
		out.println(inc);
	}

	public static void includeCss(JspWriter out, HttpServletRequest request, String path) throws Exception {
		String relPath;
		String p = request.getContextPath();
		File file = getRealFile(request, path);
		if (file.exists()) {
			if (file.length() < inlineBoundSize) {
				out.println("<style type=\"text/css\">");
				outputFile(out, file);
				out.println("</style>");
				return;
			}
			relPath = p + path + "?" + file.lastModified();
		} else {
			log.warn("Include a non exists file: " + path);
			relPath = p + path;
		}
		String inc = "<link type=\"text/css\" rel=\"stylesheet\" href=\"" + relPath + "\"/>";
		out.println(inc);
	}

}
