package org.tsaikd.java.servlet;

import org.tsaikd.java.mongodb.MongoObject;

import com.mongodb.BasicDBObject;

public class EvalReturn extends MongoObject {

	public Integer N;

	public BasicDBObject data;

	public String msg;

	public EvalReturn() {}

	public EvalReturn(int N) {
		this.N = N;
	}

	public EvalReturn(int N, String msg) {
		this.N = N;
		this.msg = msg;
	}

}
