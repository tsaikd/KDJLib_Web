package org.tsaikd.java.mongodb;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.Morphia;
import com.google.code.morphia.annotations.Entity;
import com.mongodb.DBObject;

@Entity(noClassnameStored = true)
public class BaseObject implements Cloneable {

	protected static Morphia morphia = new Morphia();

	public boolean isEmpty() {
		return toDBObject().keySet().isEmpty();
	}

	public DBObject toDBObject() {
		return morphia.toDBObject(this);
	}

	@Override
	public String toString() {
		return toDBObject().toString();
	}

	@Override
	public Object clone() {
		try {
			BaseObject ret = (BaseObject) super.clone();
			return ret;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String> cloneArrayListString(List<String> from) {
		ArrayList<String> ret = new ArrayList<String>();
		for (String o : from) {
			ret.add(o);
		}
		return ret;
	}

}
