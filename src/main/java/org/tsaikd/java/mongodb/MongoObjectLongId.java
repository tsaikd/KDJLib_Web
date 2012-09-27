package org.tsaikd.java.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.mongodb.annotations.Id;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

public class MongoObjectLongId extends MongoObject {

	static Log log = LogFactory.getLog(MongoObjectLongId.class);

	@Id
	public long id;

	public static <T extends MongoObjectLongId> T findOne(Class<T> clazz, long id) throws MongoException {
		return findOne(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObjectLongId> T findOne(Class<T> clazz, long id, String... fields) throws MongoException {
		BasicDBObject fieldobj = new BasicDBObject();
		for (String field : fields) {
			fieldobj.put(field, 1);
		}
		return findOne(clazz, new BasicDBObject("_id", id), fieldobj);
	}

	public static <T extends MongoObjectLongId> WriteResult remove(Class<T> clazz, long id) throws MongoException {
		return remove(clazz, new BasicDBObject("_id", id));
	}

	/**
	 * @param clazz
	 * @param base 1 or -1
	 * @return id
	 * @throws MongoException
	 */
	protected static long nextId(Class<? extends MongoObjectLongId> clazz, int base) throws MongoException {
		MappedClass mc = MappedClass.getMappedClass(clazz);
		long ret = (base < 0) ? -1 : 1;
		BasicDBObject sort = new BasicDBObject("_id", ((base < 0) ? 1 : -1));
		long add = (base < 0) ? -1 : 1;
		DBCursor cursor = mc.getCol().find(null, new BasicDBObject("_id", 1)).sort(sort).limit(1);
		if (cursor.hasNext()) {
			DBObject dbobj = cursor.next();
			ret = (long) dbobj.get("_id") + add;
		}
		return ret;
	}

	public static <T extends MongoObjectLongId> boolean add(T data, int base, int retry) {
		while (--retry > 0) {
			data.id = nextId(data.getClass(), 1);
			try {
				data.insert();
				return true;
			} catch (MongoException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static <T extends MongoObjectLongId> boolean add(T data, int base) {
		int retry = 100;
		return add(data, base, retry);
	}

	public static <T extends MongoObjectLongId> boolean add(T data) {
		int base = 1;
		return add(data, base);
	}

}
