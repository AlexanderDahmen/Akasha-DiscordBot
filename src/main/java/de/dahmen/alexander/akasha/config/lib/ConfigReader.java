package de.dahmen.alexander.akasha.config.lib;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;

/**
 * <em>Reader class producting a <code>Config</code> instance.</em><br>
 * This class is called upon by the <code>Config</code> Singleton 
 * if no instance was initialized.
 * 
 * @see Config
 * @author Alexander
 */
public class ConfigReader {
    private final static String DEFAULT_CONFIG_FILE = "config.properties";
    
    private final Properties properties;
    private final Collection<Class<?>> configClasses;
    
    public ConfigReader(Collection<Class<?>> configClasses) {
        this(configClasses, DEFAULT_CONFIG_FILE);
    }
    
    public ConfigReader(Collection<Class<?>> configClasses, String configFile) {
        this.properties = new Properties();
        this.configClasses = configClasses;
        
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream resource = loader.getResourceAsStream(configFile);
        if (resource == null) throw new NullPointerException("ConfigFile not found: " + configFile);
        
        try { properties.load(resource); }
        catch (IOException thr) { throw new RuntimeException(thr); }
    }
    
    public ConfigReader(Collection<Class<?>> configClasses, Properties properties) {
        this.properties = properties;
        this.configClasses = configClasses;
    }
    
    /**
     * Read the properties and produce config class instances
     * @return Map from config class to config instance object
     * @throws ConfigReader.ConfigReaderException If the read could not be performed correctly
     */
    public Map<Class<?>, Object> read() throws ConfigReaderException {
        Map<Class<?>, Object> result = initInstanceMap();
        
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value == null) continue;
            
            String[] split = key.split("\\.", 2);
            if (split.length < 2) throw new ConfigReaderException("Config key must be [class].[field]");
            
            String className = split[0];
            String field = split[1];
            
            Map.Entry<Class<?>, Object> entry = getConfigEntry(result, className);
            if (entry == null) throw new ConfigReaderException("No config class found for property: " + key);
            Class<?> configClass = entry.getKey();
            Object instance = entry.getValue();
            
            setConfigValue(instance, configClass, field, value);
        }
        
        return result;
    }
    
    private Map<Class<?>, Object> initInstanceMap() throws ConfigReaderException {
        Map<Class<?>, Object> result = new HashMap<>();
        
        try {
            for (Class<?> configClazz : configClasses) {
                Constructor<?> constructor = configClazz.getConstructor();
                Object instance = constructor.newInstance();
                result.put(configClazz, instance);
            }
        } catch (IllegalAccessException |
                IllegalArgumentException |
                InstantiationException |
                InvocationTargetException |
                NoSuchMethodException |
                SecurityException ex)
        {
            throw new ConfigReaderException("Could not instantiate config object", ex);
        }
        
        return result;
    }
    
    private Map.Entry<Class<?>, Object> getConfigEntry(Map<Class<?>, Object> map, String name) throws ConfigReaderException {
        for (Map.Entry<Class<?>, Object> entry : map.entrySet()) {
            Class<?> clazz = entry.getKey();
            ConfigClass annotation = clazz.getAnnotation(ConfigClass.class);
            if (annotation == null) throw new AssertionError("Annotation '@ConfigClass' missing for " + clazz.getName());
            
            String annotationName = annotation.value();
            if (name.equals(annotationName)) return entry;
        }
        throw new ConfigReaderException("ConfigClass not found for name: " + name);
    }
    
    private void setConfigValue(
            Object instance,
            Class<?> configClass,
            String property,
            String value)
            throws ConfigReaderException
    {
        Field field = getConfigElement(property, configClass);
        field.setAccessible(true);
        
        try {
            ReflectionUtil.assignParsedValue(instance, field, value);
        } catch (Exception ex) {
            throw new ConfigReaderException("Could not assign config value: " + value, ex);
        }
    }
    
    private Field getConfigElement(String name, Class<?> clazz) throws ConfigReaderException {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ConfigField annotation = field.getAnnotation(ConfigField.class);
            if (annotation == null) continue;
            
            String annotationName = annotation.value();
            if (name.equals(annotationName)) return field;
        }
        throw new ConfigReaderException("ConfigElement not found for name: " + name);
    }
    
    public static class ConfigReaderException extends Exception {
        public ConfigReaderException(String msg) {
            super(msg);
        }
        public ConfigReaderException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
