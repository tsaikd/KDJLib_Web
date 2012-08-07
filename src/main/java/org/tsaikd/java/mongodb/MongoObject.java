package org.tsaikd.java.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoException;

public class MongoObject {

	static Log log = LogFactory.getLog(MongoObject.class);

	private static DB getDB() {
		return MappedClass.db;
	}

	private MappedClass getMappedClass() {
		return MappedClass.getMappedClass(getClass());
	}

	protected DBCollection getCol() {
		return getMappedClass().getCol();
	}

	public String getEntityName() {
		return getMappedClass().getEntityName();
	}

	protected MongoObject newInstance() throws MongoException {
		try {
			return getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MongoException(e.getMessage(), e);
		}
	}

	public static <T> T findOne(Class<T> clazz, DBObject o, DBObject fields) throws MongoException {
		MappedClass mc = MappedClass.getMappedClass(clazz);
		DBObject dbobj = mc.getCol().findOne(o, fields);
		if (dbobj == null) {
			return null;
		}
		return fromObject(clazz, dbobj);
	}

	public MongoObject findOne(DBObject o, DBObject fields) throws MongoException {
		return findOne(getClass(), o, fields);
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromObject(Class<T> clazz, Object obj) throws MongoException {
		try {
			MongoObject ret = (MongoObject) clazz.newInstance();
			ret.fromObject(obj);
			return (T) ret;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MongoException(e.getMessage(), e);
		}
	}

	public MongoObject fromObject(Object obj) throws MongoException {
		if (obj instanceof DBRef) {
			return fromDBRef((DBRef) obj);
		} else if (obj instanceof DBObject) {
			return fromDBObject((DBObject) obj);
		} else {
			throw new MongoException("Unsupported type: " + obj.getClass().getName());
		}
	}

	public MongoObject fromDBRef(DBRef dbref) {
		MappedClass mc = getMappedClass();
		for (MappedField field : mc.persistenceFields.values()) {
			field.setEmpty(this);
		}
		mc.idField.set(this, dbref.getId());
		return this;
	}

	public MongoObject fromDBObject(DBObject dbobj) throws MongoException {
		MappedClass mc = getMappedClass();
		for (MappedField field : mc.persistenceFields.values()) {
			String key = field.getName();
			if (!dbobj.containsField(key)) {
				field.setEmpty(this);
				continue;
			}
			field.set(this, dbobj.get(key));
		}
		if (mc.idField != null) {
			mc.idField.set(this, dbobj.get("_id"));
		}
		return this;
	}

	public BasicDBObject toDBObject(boolean extendRef) {
		BasicDBObject dbobj = new BasicDBObject();
		MappedClass mc = getMappedClass();
		Object id = getIdDBValue();
		if (id != null) {
			dbobj.put("_id", id);
		}
		for (MappedField field : mc.persistenceFields.values()) {
			Object value = field.getDBValue(this, extendRef);
			if (value != null) {
				dbobj.put(field.getName(), value);
			}
		}
		return dbobj;
	}

	public BasicDBObject toDBObject() {
		return toDBObject(true);
	}

	public Object getId() {
		MappedClass mc = getMappedClass();
		if (mc.idField != null) {
			return mc.idField.get(this);
		}
		return null;
	}

	public Object getIdDBValue() {
		MappedClass mc = getMappedClass();
		if (mc.idField != null) {
			return mc.idField.getDBValue(this, false);
		}
		return null;
	}

	public DBRef toDBRef() {
		return new DBRef(getDB(), getEntityName(), getId());
	}

	public boolean isEmpty() {
		return toDBObject().keySet().isEmpty();
	}

	@Override
	public String toString() {
		if (isRef && !isFetched) {
			return toDBRef().toString();
		} else {
			return toDBObject().toString();
		}
	}

	public boolean isFetched = false;

	public boolean isRef = false;

	public void fetch() {
		if (!isRef) {
			log.error("Cannot fetch non-reference object: " + this);
			return;
		}
		if (!isFetched) {
			DBRef dbref = toDBRef();
			DBObject dbobj = dbref.fetch();
			fromDBObject(dbobj);
			isFetched = true;
		}
	}

	public MongoObject clone() {
		return MongoObject.fromObject(getClass(), toDBObject(false));
	}

	public void save() {
		DBCollection col = getCol();
		col.save(toDBObject(false));
	}

	public void setField(String... fields) {
		MappedClass mc = getMappedClass();
		if (mc.idField == null) {
			throw new MongoException("Cannot setField without Id annotation: " + fields);
		}

		BasicDBObject update = new BasicDBObject();
		for (String field : fields) {
			MappedField mf = mc.persistenceFields.get(field);
			if (mf == null) {
				throw new MongoException("Invalid field: " + field);
			}
			Object value = mf.getDBValue(this, false);
			update.put(field, value);
		}

		DBCollection col = getCol();
		col.update(new BasicDBObject("_id", getIdDBValue()), new BasicDBObject("$set", update));
	}

}
