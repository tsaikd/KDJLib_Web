package org.tsaikd.java.mongodb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface IndexFieldOption {

	public boolean dropDups() default false;

	public boolean sparse() default false;

	public boolean unique() default false;

}
