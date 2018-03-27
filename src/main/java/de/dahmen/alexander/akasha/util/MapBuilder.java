
package de.dahmen.alexander.akasha.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for building maps inline
 * 
 * @author Alexander
 * @param <K> Key class
 * @param <V> Value class
 */
public class MapBuilder<K, V> {
    
    public static <K, V> MapBuilder<K, V> builder() {
        return new MapBuilder<K, V>();
    }
    
    public static <K, V> MapBuilder<K, V> builder(Class<K> key, Class<V> value) {
        return new MapBuilder<K, V>();
    }
    
    private final Map<K, V> map;
    
    public MapBuilder() { this(new HashMap<>()); }
    public MapBuilder(Supplier<Map<K, V>> supplier) { this(supplier.get()); }
    public MapBuilder(Map<K, V> map) { this.map = map; }
    
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }
    
    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> other) {
        map.putAll(other);
        return this;
    }
    
    public Map<K, V> build() {
        return map;
    }
}
