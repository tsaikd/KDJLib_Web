package org.tsaikd.java.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.tsaikd.java.mongodb.annotations.Id;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class MongoObjectObjId extends MongoObject {

	static Log log = LogFactory.getLog(MongoObjectObjId.class);

	@Id
	public ObjectId id;

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, ObjectId id) {
		if (id == null) {
			return null;
		}
		return findOneDBObj(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, String id) {
		return findOneDBObj(clazz, ObjectId.massageToObjectId(id));
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, ObjectId id, DBObject fields) {
		if (id == null) {
			return null;
		}
		return findOneDBObj(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, String id, DBObject fields) {
		return findOneDBObj(clazz, ObjectId.massageToObjectId(id), fields);
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, ObjectId id, String... fields) {
		if (id == null) {
			return null;
		}
		return findOneDBObj(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObject> DBObject findOneDBObj(Class<T> clazz, String id, String... fields) {
		return findOneDBObj(clazz, ObjectId.massageToObjectId(id), fields);
	}

	public static <T extends MongoObjectObjId> T findOne(Class<T> clazz, ObjectId id) {
		if (id == null) {
			return null;
		}
		return findOne(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObjectObjId> T findOne(Class<T> clazz, String id) {
		return findOne(clazz, ObjectId.massageToObjectId(id));
	}

	public static <T extends MongoObjectObjId> T findOne(Class<T> clazz, ObjectId id, DBObject fields) {
		if (id == null) {
			return null;
		}
		return findOne(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObjectObjId> T findOne(Class<T> clazz, String id, DBObject fields) {
		return findOne(clazz, ObjectId.massageToObjectId(id), fields);
	}

	public static <T extends MongoObjectObjId> T findOne(Class<T> clazz, ObjectId id, String... fields) {
		if (id == null) {
			return null;
		}
		return findOne(clazz, new BasicDBObject("_id", id), fields);
	}

	public static <T extends MongoObjectObjId> T findOne(Class<T> clazz, String id, String... fields) {
		return findOne(clazz, ObjectId.massageToObjectId(id), fields);
	}

	public static <T extends MongoObjectObjId> WriteResult remove(Class<T> clazz, ObjectId id) {
		if (id == null) {
			return null;
		}
		return remove(clazz, new BasicDBObject("_id", id));
	}

	public static <T extends MongoObjectObjId> WriteResult remove(Class<T> clazz, String id) {
		return remove(clazz, ObjectId.massageToObjectId(id));
	}

}
