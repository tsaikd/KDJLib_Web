package org.tsaikd.java.mongodb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface IndexField {

	public String[] name();

	public int[] direction() default {1};

	public IndexFieldOption option() default @IndexFieldOption;

}
