package org.tsaikd.java.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BaseExtServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(BaseExtServlet.class);

	@SuppressWarnings("serial")
	private static HashMap<String, String> extMap = new HashMap<String, String>() {{
		put("json", "application/json");
		put("pdf", "application/pdf");
		put("png", "image/png");
		put("xls", "application/vnd.ms-excel");
		put("xml", "application/xml");
		put("zip", "application/zip");
	}};

	private static Pattern patExt = Pattern.compile("(?i)\\.([^.]+?)$");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (res.getContentType() == null) {
			String path = req.getServletPath();
			Matcher mat = patExt.matcher(path);
			if (!mat.find()) {
				return;
			}
			String ext = mat.group(1).toLowerCase();
			if (extMap.containsKey(ext)) {
				res.setContentType(extMap.get(ext));
			} else {
				log.warn("unknown extension: " + path);
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doGet(req, res);
	}

}
