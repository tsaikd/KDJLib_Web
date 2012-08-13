package org.tsaikd.java.mongodb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.mongodb.annotations.Entity;
import org.tsaikd.java.mongodb.annotations.IndexEntity;
import org.tsaikd.java.mongodb.annotations.Transient;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;

public class MappedClass {

	static Log log = LogFactory.getLog(MappedClass.class);

	public static DB db = null;

	public static ArrayList<String> searchPackage = new ArrayList<>();

	private static HashMap<Class<?>, MappedClass> mappedClasses = new HashMap<>();

	private static HashMap<String, MappedClass> mappedClasses2 = new HashMap<>();

	public static MappedClass getMappedClass(Class<?> clazz) {
		if (mappedClasses.containsKey(clazz)) {
			return mappedClasses.get(clazz);
		}
		MappedClass mappedClass = new MappedClass(clazz);
		mappedClasses.put(clazz, mappedClass);
		mappedClasses2.put(mappedClass.getEntityName(), mappedClass);
		if (db != null) {
			DBCollection col = mappedClass.getCol();
			for (BasicDBObject field : mappedClass.indexFields) {
				log.debug("ensureIndex " + mappedClass.getEntityName() + ": " + field);
				col.ensureIndex(field);
			}
		}
		return mappedClass;
	}

	public static MappedClass getMappedClass(String name) throws MongoException {
		if (!mappedClasses2.containsKey(name)) {
			Class<?> clazz = searchClass(name);
			if (clazz == null) {
				return null;
			} else {
				return getMappedClass(clazz);
			}
		}
		return mappedClasses2.get(name);
	}

	public static boolean isMappedClass(Class<?> clazz) {
		return mappedClasses.containsKey(clazz);
	}

	private static Class<?> searchClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
		}
		for (String basePkg : searchPackage) {
			try {
				return Class.forName(basePkg + "." + name);
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

	private static ArrayList<MappedField> getDeclaredAndInheritedFields(Class<?> clazz) {
		ArrayList<MappedField> fields = new ArrayList<MappedField>();
		while ((clazz != null) && (clazz != Object.class) && (clazz != MongoObject.class)) {
			for (Field field : clazz.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				if (field.isAnnotationPresent(Transient.class)) {
					continue;
				}
				if (Modifier.isPrivate(field.getModifiers())) {
					continue;
				}
				if (Modifier.isProtected(field.getModifiers())) {
					continue;
				}
				fields.add(new MappedField(field));
			}
			clazz = clazz.getSuperclass();
		}
		return fields;
	}

	public LinkedHashMap<String, MappedField> persistenceFields = new LinkedHashMap<String, MappedField>();

	public MappedField idField;

	public ArrayList<BasicDBObject> indexFields = new ArrayList<BasicDBObject>();

	private Class<?> clazz;

	MappedClass(Class<?> clazz) throws MongoException {
		this.clazz = clazz;
		IndexEntity annoIndex = getAnnotation(IndexEntity.class);
		if (annoIndex != null) {
			for (String name : annoIndex.names()) {
				int dir = 1;
				name = name.trim();
				if (name.startsWith("-")) {
					dir = -1;
					name = name.substring(1).trim();
				}
				BasicDBObject indexField = new BasicDBObject(name, dir);
				indexFields.add(indexField);
			}
		}
		ArrayList<MappedField> fields = getDeclaredAndInheritedFields(clazz);
		for (MappedField field : fields) {
			if (field.isId) {
				if (idField != null) {
					throw new MongoException("Id annotation declared for " + clazz + ", id:" + idField);
				}
				idField = field;
				continue;
			}
			persistenceFields.put(field.getName(), field);
		}
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		Class<?> clazz = this.clazz;
		while ((clazz != null) && (clazz != Object.class)) {
			T anno = clazz.getAnnotation(annotationClass);
			if (anno != null) {
				return anno;
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	private String cacheEntityName;
	public String getEntityName() {
		if (cacheEntityName == null) {
			Entity anno = clazz.getAnnotation(Entity.class);
			if (anno != null) {
				cacheEntityName = anno.name();
			}
			if (cacheEntityName == null || cacheEntityName.isEmpty()) {
				cacheEntityName = clazz.getSimpleName();
			}
		}
		return cacheEntityName;
	}

	public DBCollection getCol() {
		return db.getCollection(getEntityName());
	}

	public Object newInstance() throws MongoException {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new MongoException(e.getMessage(), e);
		}
	}

	public static Object newInstance(String name) throws MongoException {
		return getMappedClass(name).newInstance();
	}

}
