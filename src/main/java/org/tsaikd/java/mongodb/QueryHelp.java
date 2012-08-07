package org.tsaikd.java.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class QueryHelp extends BasicDBObject {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(QueryHelp.class);

	public BasicDBObject query = new BasicDBObject();

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

}
