
package de.dahmen.alexander.akasha.core.conversation.message;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 * Helper class for locating Message Resources of a specified <b>language</b>.<br>
 * The default language is {@code ENGLISH} (package "en"), the base path for
 * message resources is "message".
 * 
 * @author Alexander
 */
public class MessageResource {
    
    public static final String RESOURCE_BASE = "message";
    public static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    
    protected static final ConcurrentMap<LanguageResource, Supplier<InputStream>>
            CACHED_RESOURCES = new ConcurrentHashMap<>(32);
    
    /**
     * Enum of all supported languages (including package directories)
     */
    @AllArgsConstructor
    public static enum Language {
        ENGLISH ("en");
        
        @Getter
        private final String directory;
    }
    
    @Value
    protected static class LanguageResource {
        Language language;
        String resource;
    }
    
    protected final Language language;
    
    /**
     * Create a MessageResource with the default language
     */
    public MessageResource() {
        this(DEFAULT_LANGUAGE);
    }
    
    /**
     * Create a MessageResource of a specified language
     * @param language Language of the resource files to be loaded
     */
    public MessageResource(Language language) {
        this.language = language;
    }
    
    /**
     * Get an InputStream of the resource represented by this instance
     * @param resource Resource to load from "message/{language}/{resource}"
     * @return New InputStream containing the resource
     */
    public final InputStream getResource(String resource) {
        return getResourceSupplier(resource).get();
    }
    
    public final Supplier<InputStream> getResourceSupplier(String resource) {
        LanguageResource languageResource = new LanguageResource(language, resource);
        Map<LanguageResource, Supplier<InputStream>> cache = cache();
        String location = resourceLocation(resource);
        
        if (cache == null) {
            return load(location);
        } else {
            Supplier<InputStream> result = cache.get(languageResource);
            if (result == null) {
                result = load(resourceLocation(resource));
                cache.put(languageResource, result);
            }
            return result;
        }
    }
    
    public final String resourceLocation(String resource) {
        return new StringJoiner("/")
                .add(RESOURCE_BASE)
                .add(language.getDirectory())
                .add(resource)
                .toString().replaceAll("//+", "/"); // Against double-slashes
    }
    
    protected Map<LanguageResource, Supplier<InputStream>> cache() {
        return CACHED_RESOURCES;
    }
    
    protected Supplier<InputStream> load(String location) {
        try { return new ResourceCache(location); }
        catch (FileNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getMessage());
        }
    }
    
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String resource) {
            super("Resource not found: " + resource);
        }
    }
}
