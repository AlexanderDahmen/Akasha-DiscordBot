
package de.dahmen.alexander.akasha.config.lib;

//import java.io.StringReader;
import java.lang.reflect.Field;
//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;

/**
 * Utility class for reflection operations
 * @author Alexander
 */
public class ReflectionUtil {
    
    public static void assignParsedValue(Object instance, Field field, String value) throws Exception {
        Class<?> type = field.getType();
        if      (String.class.isAssignableFrom(type)) field.set(instance, value);
        else if (Integer.class.isAssignableFrom(type)) field.set(instance, Integer.valueOf(value));
        else if (Boolean.class.isAssignableFrom(type)) field.set(instance, Boolean.valueOf(value));
        else if (Double.class.isAssignableFrom(type)) field.set(instance, Double.valueOf(value));
        else if (Float.class.isAssignableFrom(type)) field.set(instance, Float.valueOf(value));
        else if (Long.class.isAssignableFrom(type)) field.set(instance, Long.valueOf(value));
        else if (Class.class.isAssignableFrom(type)) field.set(instance, Class.forName(value));
//        else if (JsonObject.class.isAssignableFrom(type)) field.set(instance, toJsonObject(value));
//        else if (JsonArray.class.isAssignableFrom(type)) field.set(instance, toJsonArray(value));
        else throw new Exception("Unknown field type: " + type.getName());
    }
//    
//    private static JsonArray toJsonArray(String string) {
//        try (JsonReader r = Json.createReader(new StringReader(string))) {
//            return r.readArray();
//        }
//    }
//    
//    private static JsonObject toJsonObject(String string) {
//        try (JsonReader r = Json.createReader(new StringReader(string))) {
//            return r.readObject();
//        }
//    }
    
    private ReflectionUtil() { }
}
