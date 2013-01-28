package org.tsaikd.java.mongodb;

import java.util.HashSet;

import com.mongodb.util.JSONCallback;

public class JSONLongIdCallback extends JSONCallback {

	private static HashSet<String> idSet = new HashSet<>();
	static {
		idSet.add("id");
		idSet.add("_id");
		idSet.add("$id");
	}

	@Override
	public void gotInt(final String name, final int value) {
		if (idSet.contains(name)) {
			_put(name , (long)value);
		} else {
			_put(name , value);
		}
	}

}
