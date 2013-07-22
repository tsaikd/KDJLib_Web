package org.tsaikd.java.mongodb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.tsaikd.java.mongodb.fork.com.mongodb.util.ClassMapBasedObjectSerializer;
import org.tsaikd.java.mongodb.fork.com.mongodb.util.JSONSerializers;

import com.mongodb.util.ObjectSerializer;

public class MongoObjectSerializers extends JSONSerializers {

	static Log log = LogFactory.getLog(MongoObjectSerializers.class);

	public static ObjectSerializer getMongoShell() {
		ClassMapBasedObjectSerializer serializer = (ClassMapBasedObjectSerializer) getLegacy();
		serializer.addObjectSerializer(Integer.class, new IntegerMongoShellSerializer(serializer));
		serializer.addObjectSerializer(Long.class, new LongMongoShellSerializer(serializer));
		serializer.addObjectSerializer(Date.class, new DateMongoShellSerializer(serializer));
		serializer.addObjectSerializer(ObjectId.class, new ObjectIdMongoShellSerializer(serializer));
		return serializer;
	}

	protected static class IntegerMongoShellSerializer extends CompoundObjectSerializer {

		IntegerMongoShellSerializer(ObjectSerializer serializer) {
			super(serializer);
		}
	
		@Override
		public void serialize(Object obj, StringBuilder buf) {
			buf.append("NumberInt(");
			buf.append(obj.toString());
			buf.append(")");
		}
	}

	protected static class LongMongoShellSerializer extends CompoundObjectSerializer {

		LongMongoShellSerializer(ObjectSerializer serializer) {
			super(serializer);
		}
	
		@Override
		public void serialize(Object obj, StringBuilder buf) {
			buf.append("NumberLong(");
			buf.append(obj.toString());
			buf.append(")");
		}
	}

	protected static class DateMongoShellSerializer extends CompoundObjectSerializer {

		DateMongoShellSerializer(ObjectSerializer serializer) {
			super(serializer);
		}
	
		@Override
		public void serialize(Object obj, StringBuilder buf) {
			Date date = (Date) obj;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			format.setCalendar(new GregorianCalendar(new SimpleTimeZone(0, "GMT")));
			buf.append("ISODate(\"");
			buf.append(format.format(date));
			buf.append("\")");
		}
	}

	protected static class ObjectIdMongoShellSerializer extends CompoundObjectSerializer {

		ObjectIdMongoShellSerializer(ObjectSerializer serializer) {
			super(serializer);
		}
	
		@Override
		public void serialize(Object obj, StringBuilder buf) {
			buf.append("ObjectId(\"");
			buf.append(obj.toString());
			buf.append("\")");
		}
	}

}
