package de.dahmen.alexander.akasha.config.lib;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * <u>Configuration root data class</u><br>
 * Configuration data class instances are navigated from here.<br>
 * 
 * @author Alexander
 */
public class Config {
    
    /**
     * Load a configuration from a properties instance 
     * using a specified collection of data classes
     * @param properties Properties to load instance values from
     * @param configClasses Configuration classes to be instantiated
     * @return New Config instance
     */
    public static Config load(Properties properties, Collection<Class<?>> configClasses) {
        Config instance;
        ConfigReader reader = new ConfigReader(configClasses, properties);
        try { instance = new Config(reader.read()); }
        catch (ConfigReader.ConfigReaderException ex) {
            throw new RuntimeException(ex);
        }
        return instance;
    }
    
    public static Config load(String configFile, Collection<Class<?>> configClasses) {
        Config instance;
        ConfigReader reader = new ConfigReader(configClasses, configFile);
        try { instance = new Config(reader.read()); }
        catch (ConfigReader.ConfigReaderException ex) {
            throw new RuntimeException(ex);
        }
        return instance;
    }
    
    /**
     * Load a configuration from the default resource properties 
     * using a specified collection of data classes
     * @param configClasses Configuration classes to be instantiated
     * @return New Config instance
     */
    public static Config load(Collection<Class<?>> configClasses) {
        Config instance;
        ConfigReader reader = new ConfigReader(configClasses);
        try { instance = new Config(reader.read()); }
        catch (ConfigReader.ConfigReaderException ex) {
            throw new RuntimeException(ex);
        }
        return instance;
    }
    
    private final Map<Class<?>, Object> configClassInstances;
    
    protected Config(Map<Class<?>, Object> configClassInstances) {
        this.configClassInstances = configClassInstances;
    }
    
    /**
     * Get the config element instance of a certain type
     * @param <T> Config element class type
     * @param clazz Config element class
     * @return Config element instance
     */
    public <T> T get(Class<T> clazz) {
        Object configClassInstance = configClassInstances.get(clazz);
        return clazz.cast(configClassInstance);
    }
    
    /**
     * Get all configuration classes that are loaded into this configuration
     * @return Collection of classes
     */
    public Collection<Class<?>> getClasses() {
        return Collections.unmodifiableCollection(configClassInstances.keySet());
    }
    
    /**
     * Get all configuration class instances that are loaded into this configuration
     * @return Collection of objects
     */
    public Collection<Object> getInstances() {
        return Collections.unmodifiableCollection(configClassInstances.values());
    }
}
