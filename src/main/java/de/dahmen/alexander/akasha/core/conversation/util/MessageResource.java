
package de.dahmen.alexander.akasha.core.conversation.util;

import java.io.InputStream;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 *
 * @author Alexander
 */
public class MessageResource {
    
    public static final String RESOURCE_BASE = "message";
    public static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
    
    protected static final ConcurrentMap<LanguageResource, ResourceCache> CACHED_RESOURCES = new ConcurrentHashMap<>(32);
    
    @AllArgsConstructor
    public static enum Language {
        ENGLISH("en");
        
        @Getter
        private final String directory;
    }
    
    @Value
    protected static class LanguageResource {
        Language language;
        String resource;
    }
    
    protected final Supplier<InputStream> getResourceSupplier(LanguageResource resource) {
        ResourceCache result = CACHED_RESOURCES.get(resource);
        if (result == null) {
            result = new ResourceCache(resourceLocation(resource));
            CACHED_RESOURCES.put(resource, result);
        }
        return result;
    }

    protected final String resourceLocation(LanguageResource resource) {
        return new StringJoiner("/")
                .add(RESOURCE_BASE)
                .add(resource.getLanguage().getDirectory())
                .add(resource.getResource())
                .toString().replaceAll("//+", "/"); // Against double-slashes
    }
}
