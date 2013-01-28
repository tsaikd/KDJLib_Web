package org.tsaikd.java.mongodb;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class QueryHelp extends BasicDBObject {

	private static final long serialVersionUID = 1L;
	static Log log = LogFactory.getLog(QueryHelp.class);

	enum SmartOp {
		Set,
		Unset,
	}

	public static SmartOp checkSmart(Object value) {
		if (value == null) {
			return SmartOp.Unset;
		} else if (value instanceof String) {
			String val = (String) value;
			if (val.isEmpty()) {
				return SmartOp.Unset;
			}
		} else if (value instanceof Integer) {
			Integer val = (Integer) value;
			if (val == 0) {
				return SmartOp.Unset;
			}
		} else if (value instanceof Long) {
			Long val = (Long) value;
			if (val == 0) {
				return SmartOp.Unset;
			}
		} else if (value instanceof Float) {
			Float val = (Float) value;
			if (val == 0) {
				return SmartOp.Unset;
			}
		} else if (value instanceof Double) {
			Double val = (Double) value;
			if (val == 0) {
				return SmartOp.Unset;
			}
		} else if (value instanceof List) {
			List<?> val = (List<?>) value;
			if (val.isEmpty()) {
				return SmartOp.Unset;
			}
		}
		return SmartOp.Set;
	}

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

	public QueryHelp filterAddToSet(String key, Object value) {
		return putBaseKeyValue("$addToSet", key, value);
	}

	public QueryHelp filterNe(String key, Object value) {
		return filter(key, new QueryHelp("$ne", value));
	}

	public QueryHelp filterPull(String key, Object value) {
		return filter("$pull", new QueryHelp(key, value));
	}

	public QueryHelp filterPullAll(String key, List<?> value) {
		return putBaseKeyValue("$pullAll", key, value);
	}

	public QueryHelp filterPush(String key, Object value) {
		return filter("$push", new QueryHelp(key, value));
	}

	public QueryHelp filterPushAll(String key, List<?> value) {
		return putBaseKeyValue("$pushAll", key, value);
	}

	public QueryHelp putBaseKeyValue(String base, String key, Object value) {
		if (containsField(base)) {
			QueryHelp baseObj = (QueryHelp) get(base);
			if (baseObj.containsField(key)) {
				Object baseKeyObj = baseObj.get(key);
				if (baseKeyObj instanceof DBObject) {
					DBObject eachKeyObj = (DBObject) baseKeyObj;
					if (eachKeyObj.containsField("$each")) {
						BasicDBList eachObj = (BasicDBList) eachKeyObj.get("$each");
						eachObj.add(value);
					} else {
						baseObj.put(key, value);
					}
				} else if (base.equals("$addToSet")) {
					BasicDBList eachObj = new BasicDBList();
					eachObj.add(baseKeyObj);
					eachObj.add(value);
					baseObj.put(key, new QueryHelp().put("$each", eachObj));
				} else {
					baseObj.put(key, value);
				}
			} else {
				baseObj.put(key, value);
			}
		} else {
			filter(base, new QueryHelp().put(key, value));
		}
		return this;
	}

	public QueryHelp filterSet(String key, Object value) {
		return putBaseKeyValue("$set", key, value);
	}

	public QueryHelp filterSetSmart(String key, Object value) {
		SmartOp op = checkSmart(value);
		if (op == SmartOp.Unset) {
			return filterUnset(key);
		} else {
			return putBaseKeyValue("$set", key, value);
		}
	}

	public QueryHelp filterUnset(String key) {
		return putBaseKeyValue("$unset", key, 1);
	}

	public QueryHelp wrapObject(String key) {
		if (isEmpty()) {
			return this;
		}
		QueryHelp qobj = new QueryHelp();
		for (java.util.Map.Entry<String, Object> entry : entrySet()) {
			qobj.append(entry.getKey(), entry.getValue());
		}
		clear();
		append(key, qobj);
		return this;
	}

	public QueryHelp wrapList(String key) {
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

	@Override
	public QueryHelp put(String key, Object value) {
		super.put(key, value);
		return this;
	}

}
