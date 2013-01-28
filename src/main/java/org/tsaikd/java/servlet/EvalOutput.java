package org.tsaikd.java.servlet;

import com.mongodb.BasicDBObject;

public class EvalOutput extends JSonOutput {

	public Integer N;

	public BasicDBObject data;

	public EvalOutput() {}

	public EvalOutput(int N) {
		this.N = N;
	}

	public EvalOutput(int N, String msg) {
		this.N = N;
		this.msg = msg;
	}

}
