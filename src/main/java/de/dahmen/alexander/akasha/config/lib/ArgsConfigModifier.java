
package de.dahmen.alexander.akasha.config.lib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alexander
 */
public class ArgsConfigModifier {
    private final String[] args;
    private List<String> argBuffer;
    
    public ArgsConfigModifier(String[] args) {
        this.args = args;
    }
    
    public void modify(Config config) {
        this.argBuffer = new ArrayList<>(Arrays.asList(args));
        Map<String, Field> fields = getFields(config.getClasses());
        Map<String, ArgConfigField> annotations = getAnnotations(config.getClasses());
        
        while (!argBuffer.isEmpty()) {
            String arg = takeArg();
            if (arg == null) break;
            if (arg.isEmpty()) continue;
            
            Field field = fields.get(arg);
            ArgConfigField annotation = annotations.get(arg);
            if ((field == null) || (annotation == null)) throw new RuntimeException("Argument not found: " + arg);
            
            Class<?> fieldClass = field.getDeclaringClass();
            Object instance = config.get(fieldClass);
            if (instance == null) continue;
            
            try {
                setValue(annotation, field, instance);
            } catch (Exception ex) {
                throw new RuntimeException("Invalid argument: " + ex.getMessage(), ex);
            }
        }
    }
    
    private Map<String, Field> getFields(Collection<Class<?>> configClasses) {
        Map<String, Field> result = new HashMap<>();
        
        for (Class<?> configClass : configClasses) {
            for (Field declaredField : configClass.getDeclaredFields()) {
                ArgConfigField annotation = declaredField.getAnnotation(ArgConfigField.class);
                if (annotation != null) result.put(annotation.arg(), declaredField);
            }
        }
        
        return result;
    }
    
    private Map<String, ArgConfigField> getAnnotations(Collection<Class<?>> classes) {
        Map<String, ArgConfigField> result = new HashMap<>();
        
        for (Class<?> configClass : classes) {
            for (Field declaredField : configClass.getDeclaredFields()) {
                ArgConfigField annotation = declaredField.getAnnotation(ArgConfigField.class);
                if (annotation != null) result.put(annotation.arg(), annotation);
            }
        }
        
        return result;
    }
    


    private void setValue(ArgConfigField annotation, Field field, Object instance) throws Exception {
        field.setAccessible(true);
        Class<?> type = field.getType();
        
        if (type == Boolean.class) {
            field.set(instance, annotation.bool());
        } else {
            String value = takeArg();
            if (value == null) throw new Exception("Expected value for argument: " + annotation.arg());
            ReflectionUtil.assignParsedValue(instance, field, value);
        }
    }

    private String takeArg() {
        if (argBuffer == null) return null;
        if (argBuffer.isEmpty()) return null;
        return argBuffer.remove(0);
    }
}
