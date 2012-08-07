package org.tsaikd.java.mongodb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.mongodb.annotations.Id;
import org.tsaikd.java.mongodb.annotations.Reference;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MongoException;

public class MappedField {

	static Log log = LogFactory.getLog(MappedField.class);

	private Field field;

	public boolean isId = false;

	public boolean isNativeType = false;

	public boolean isNativeClass = false;

	public boolean isEnum = false;

	public boolean isInterface = false;

	public boolean isReference = false;

	public boolean isMongoObject = false;

	public boolean isList = false;

	public boolean isBasicDBList = false;

	public boolean isDBObject = false;

	public Object defValue = null;

	MappedField(Field field) throws MongoException {
		this.field = field;
		Class<?> type = field.getType();

		isId = field.isAnnotationPresent(Id.class);
		isReference = field.isAnnotationPresent(Reference.class);
		isInterface = type.isInterface();

		String typename = type.getName();
		if (typename.equals("int")) {
			isNativeType = true;
			defValue = (int) 0;
		} else if (typename.equals("long")) {
			isNativeType = true;
			defValue = (long) 0;
		} else if (typename.equals("double")) {
			isNativeType = true;
			defValue = (double) 0;
		} else if (typename.equals("java.lang.Integer")) {
			isNativeClass = true;
			defValue = new Integer(0);
		} else if (typename.equals("java.lang.Long")) {
			isNativeClass = true;
			defValue = new Long(0);
		} else if (typename.equals("java.lang.Double")) {
			isNativeClass = true;
			defValue = new Double(0);
		} else if (typename.equals("java.lang.Boolean")) {
			isNativeClass = true;
			defValue = false;
		} else if (typename.equals("java.lang.String")) {
			isNativeClass = true;
			defValue = "";
		} else if (typename.equals("java.util.regex.Pattern")) {
			isNativeClass = true;
		} else if (type.isEnum()) {
			isEnum = true;
		} else if (isInterface) {
			if (typename.equals("java.util.List")) {
				isList = true;
			} else if (typename.equals("com.mongodb.DBObject")) {
				isDBObject = true;
			} else {
				throw new MongoException("Unsupport interface: " + typename);
			}
		} else {
			Object newobj = defValue = newInstance();
			if (newobj instanceof MongoObject) {
				isMongoObject = true;
			}
			if (newobj instanceof BasicDBList) {
				isList = true;
				isBasicDBList = true;
			} else if (newobj instanceof List<?>) {
				isList = true;
			}
		}
		if (isList && !isBasicDBList) {
			Object newarg = newInstanceListType();
			if (newarg instanceof MongoObject) {
				isMongoObject = true;
			}
		}
		field.setAccessible(true);

		if (isReference && !isMongoObject) {
			log.error("Reference field should be extend of MongoObject: " + field);
		}
	}

	public Object newInstance() throws MongoException {
		try {
			if (isInterface) {
				if (isList) {
					return new ArrayList<Object>();
				} else if (isDBObject) {
					return new BasicDBObject();
				}
			}
			return field.getType().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MongoException(e.getMessage(), e);
		}
	}

	public Object newInstanceListType() throws MongoException {
		if (!isList) {
			throw new MongoException("Can not newInstanceListType with non list type");
		}
		try {
			ParameterizedType pt = (ParameterizedType) field.getGenericType();
			Type[] ptargs = pt.getActualTypeArguments();
			if (ptargs.length > 0) {
				return ((Class<?>) ptargs[0]).newInstance();
			} else {
				return null;
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MongoException(e.getMessage(), e);
		}
	}

	public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
		return field.isAnnotationPresent(clazz);
	}

	public String getName() {
		return field.getName();
	}

	private Object getObject(Object obj) throws MongoException {
		try {
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new MongoException(e.getMessage(), e);
		}
	}

	public MongoObject getMongoObject(Object obj) {
		if (!isMongoObject || isList) {
			log.error("Cannot getMongoObject with invalid type");
			return null;
		}
		return (MongoObject) getObject(obj);
	}

	public DBRef getDBRef(Object obj) {
		if (!isReference || isList) {
			log.error("Cannot getDBRef with invalid type");
			return null;
		}
		MongoObject value = getMongoObject(obj);
		if (value == null) {
			return null;
		} else {
			return value.toDBRef();
		}
	}

	public DBObject getDBObject(Object obj, boolean extendRef) {
		if (isDBObject) {
			return (DBObject) getObject(obj);
		}
		MongoObject value = getMongoObject(obj);
		if (value == null) {
			return null;
		} else {
			return value.toDBObject(extendRef);
		}
	}

	public BasicDBList getDBList(Object obj, boolean extendRef) {
		if (!isList) {
			log.error("Cannot getDBList with invalid type");
			return null;
		}
		BasicDBList ret = new BasicDBList();
		List<?> list = (List<?>) getObject(obj);
		for (Object forobj : list) {
			if (isReference) {
				MongoObject value = (MongoObject) forobj;
				if (extendRef && value.isFetched) {
					ret.add(value.toDBObject(extendRef));
				} else {
					ret.add(value.toDBRef());
				}
			} else if (isMongoObject) {
				ret.add(((MongoObject) forobj).toDBObject(extendRef));
			} else {
				ret.add(forobj);
			}
		}
		if (ret.isEmpty()) {
			return null;
		} else {
			return ret;
		}
	}

	public Object getDBValue(Object obj, boolean extendRef) {
		if (isList) {
			return getDBList(obj, extendRef);
		}

		Object value = getObject(obj);
		if (value == null) {
			return null;
		}

		if (isEnum) {
			return value.toString();
		}

		if (isNativeClass) {
			if (value.equals(defValue)) {
				return null;
			}
		}

		if (isMongoObject) {
			MongoObject mobj = (MongoObject) value;
			if (isReference) {
				if (!extendRef || !mobj.isFetched) {
					return mobj.toDBRef();
				}
			}
			BasicDBObject ret = mobj.toDBObject(extendRef);
			if (ret.isEmpty()) {
				return null;
			}
			return ret;
		}

		return value;
	}

	public Object get(Object obj) {
		return getObject(obj);
	}

	private void setObject(Object obj, Object value) throws MongoException {
		try {
			if (value == null && (isList || isMongoObject)) {
				field.set(obj, newInstance());
			} else if (value == null && isNativeType) {
				field.set(obj, defValue);
			} else {
				field.set(obj, value);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new MongoException(e.getMessage(), e);
		}
	}

	public void setEmpty(Object obj) {
		setObject(obj, null);
	}

	public void set(Object obj, DBRef value) {
		MongoObject newobj = (MongoObject) MappedClass.newInstance(value.getRef());
		MappedClass mc = MappedClass.getMappedClass(newobj.getClass());
		mc.idField.setObject(newobj, value.getId());
		newobj.isRef = true;
		setObject(obj, newobj);
	}

	public void set(Object obj, DBObject value) {
		if (isMongoObject) {
			MongoObject newobj = (MongoObject) newInstance();
			newobj.fromDBObject(value);
			setObject(obj, newobj);
		} else {
			setObject(obj, value);
		}
	}

	public void set(Object obj, BasicDBList list) {
		if (isBasicDBList) {
			setObject(obj, list);
		} else if (isList) {
			ArrayList<Object> newobj = new ArrayList<Object>();
			if (isMongoObject) {
				for (Object forobj : list) {
					MongoObject newchild = (MongoObject) newInstanceListType();
					newchild.fromObject(forobj);
					newchild.isRef = isReference;
					newobj.add(newchild);
				}
			} else {
				for (Object forobj : list) {
					newobj.add(forobj);
				}
			}
			setObject(obj, newobj);
		} else {
			throw new MongoException("Invalid value of type");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void set(Object obj, Object value) {
		if (value instanceof DBRef) {
			set(obj, (DBRef) value);
		} else if (value instanceof BasicDBList) {
			set(obj, (BasicDBList) value);
		} else if (value instanceof DBObject) {
			set(obj, (DBObject) value);
		} else if (isEnum && value instanceof String) {
			setObject(obj, Enum.valueOf((Class<? extends Enum>) field.getType(), (String) value));
		} else {
			setObject(obj, value);
		}
	}

	@Override
	public String toString() {
		return field.toString();
	}

}
