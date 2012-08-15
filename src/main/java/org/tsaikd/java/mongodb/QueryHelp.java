package org.tsaikd.java.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class QueryHelp extends BasicDBObject {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(QueryHelp.class);

	public QueryHelp() {
	}

	public QueryHelp(String key, Object value) {
		filter(key, value);
	}

	public QueryHelp filter(String key, Object value) {
		if (value == null) {
			BasicDBList or = new BasicDBList();
			or.add(new BasicDBObject(key, null));
			or.add(new BasicDBObject(key, new BasicDBList()));
			or.add(new BasicDBObject(key, new BasicDBObject()));
			append("$or", or);
		} else {
			append(key, value);
		}
		return this;
	}

	private QueryHelp wrapList(String key) {
		if (size() < 2) {
			return this;
		}
		BasicDBList list = new BasicDBList();
		for (java.util.Map.Entry<String, Object> entry : entrySet()) {
			list.add(new BasicDBObject(entry.getKey(), entry.getValue()));
		}
		clear();
		append(key, list);
		return this;
	}

	public QueryHelp wrapOr() {
		return wrapList("$or");
	}

}
