package org.springframework.faces.mvc.bind.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//FIXME comment and complete
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
public @interface BeforePhase {
	public Phase[] value() default {};
}
