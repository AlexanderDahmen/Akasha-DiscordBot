package de.dahmen.alexander.akasha.config.lib;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Modify configuration with environment variable values
 * @author Alexander
 */
public class EnvironmentConfigModifier {
    
    private final Config config;
    private final Map<String, String> env;
    
    public EnvironmentConfigModifier(Config config) {
        this.config = config;
        this.env = System.getenv();
    }
    
    public EnvironmentConfigModifier(Config config, Map<String, String> env) {
        this.config = config;
        this.env = env;
    }
    
    public void modifyConfiguration() {
        for (Class<?> configClazz : config.getClasses()) {
            Object instance = config.get(configClazz);
            if (instance != null) modifyInstance(configClazz, instance);
        }
    }
    
    private void modifyInstance(Class<?> clazz, Object instance) {
        for (Field field : clazz.getDeclaredFields()) {
            EnvironmentField annotation = field.getAnnotation(EnvironmentField.class);
            
            if (annotation != null) {
                String variable = annotation.value();
                //String display = annotation.display();
                String value = env.get(variable);
                
                if (value != null) {
                    setField(variable, field, instance, value);
                    /*LOG.log(Level.INFO, "Environment variable set: {0} = {1}", new Object[]{
                        variable,
                        (display.isEmpty()) ? value : display
                    });*/
                }
            }
        }
    }
    
    private void setField(String name, Field field, Object instance, String value) {
        Class<?> type = field.getType();
        field.setAccessible(true);
        
        try { ReflectionUtil.assignParsedValue(instance, field, value); }
        catch (Exception ex) {
            throw new RuntimeException("Unassignable environment variable: "
                    + name + " (" + type.getName() + ") = " + value
                    + "; -> " + ex.getMessage(),
                    ex
            );
        }
    }
}
