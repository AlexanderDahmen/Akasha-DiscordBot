package de.dahmen.alexander.akasha.config.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a field to be modified by environment variables
 * @author Alexander
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.FIELD)
public @interface EnvironmentField {
    String value();
    String display() default "";
}
