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

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, long id) {
		return findOneDBObj(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, Long id) {
		if (id == null) {
			return null;
		}
		return findOneDBObj(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, long id, DBObject fields) {
		return findOneDBObj(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, Long id, DBObject fields) {
		if (id == null) {
			return null;
		}
		return findOneDBObj(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, long id, String... fields) {
		return findOneDBObj(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, Long id, String... fields) {
		if (id == null) {
			return null;
		}
		return findOneDBObj(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObjectLongId> T findOne(Class<T> clazz, long id) {
		return findOne(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObjectLongId> T findOne(Class<T> clazz, Long id) {
		if (id == null) {
			return null;
		}
		return findOne(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObjectLongId> T findOne(Class<T> clazz, long id, DBObject fields) {
		return findOne(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObjectLongId> T findOne(Class<T> clazz, Long id, DBObject fields) {
		if (id == null) {
			return null;
		}
		return findOne(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObjectLongId> T findOne(Class<T> clazz, Long id, String... fields) {
		if (id == null) {
			return null;
		}
		return findOne(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObjectLongId> WriteResult remove(Class<T> clazz, Long id) {
		return remove(clazz, new BasicDBObject("_id", id));
	}

	/**
	 * @param clazz
	 * @param base 1 or -1
	 * @return id
	 */
	protected static long nextId(Class<? extends MongoObjectLongId> clazz, int base) {
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
