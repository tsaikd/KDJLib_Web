package org.tsaikd.java.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletResponse;

import org.tsaikd.java.mongodb.MongoObject;

public class JSonOutput extends MongoObject {

	public Integer status;

	public void write(ServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		out.write(toString());
		out.close();
	}

}
