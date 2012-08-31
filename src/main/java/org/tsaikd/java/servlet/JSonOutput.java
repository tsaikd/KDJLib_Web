package org.tsaikd.java.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.tsaikd.java.mongodb.MongoObject;

public class JSonOutput extends MongoObject {

	public Integer status;

	public String msg;

	public void addMsg(String sMsg) {
		if (msg == null) {
			msg = sMsg;
		} else {
			msg += "<br/>" + sMsg;
		}
	}

	public void write(HttpServletResponse res) throws IOException {
		res.setContentType("application/json");
		PrintWriter out = res.getWriter();
		out.write(toDBObject(true, true).toString());
		out.close();
	}

}
