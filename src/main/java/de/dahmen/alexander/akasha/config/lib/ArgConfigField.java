package de.dahmen.alexander.akasha.config.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a configuration field as modifyable by command line argument
 * @author Alexander
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgConfigField {
    String arg();
    String description() default "";
    boolean bool() default true;
}
