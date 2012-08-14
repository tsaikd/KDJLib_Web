package org.tsaikd.java.mongodb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface DefValue {

	public boolean valueBoolean() default false;

	public int valueInt() default 0;

	public long valueLong() default 0;

	public double valueDouble() default 0;

}
