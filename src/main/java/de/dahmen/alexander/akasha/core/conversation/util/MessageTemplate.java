
package de.dahmen.alexander.akasha.core.conversation.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 *
 * @author Alexander
 */
public class MessageTemplate implements
        Supplier<InputStream>,
        Function<Map<String, Object>, InputStream>
{    
    public static final String RESOURCE_BASE = "message";
    public static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
    
    private static final ConcurrentMap<LanguageResource, ResourceCache> CACHED_RESOURCES = new ConcurrentHashMap<>(32);
    
    private final Supplier<InputStream> input;
    
    public MessageTemplate(String resource) {
        this(DEFAULT_LANGUAGE, resource);
    }
    
    public MessageTemplate(Language language, String resource) {
        this.input = getResourceSupplier(new LanguageResource(language, resource));
    }
    
    public MessageTemplate(MessageTemplate before, String resource) {
        this(before, DEFAULT_LANGUAGE, resource);
    }
    
    public MessageTemplate(MessageTemplate before, Language language, String resource) {
        this.input = () -> new ConcatInputStream(
                before.get(),
                getResourceSupplier(new LanguageResource(language, resource)).get());
    }
    
    public MessageTemplate(String resource, MessageTemplate after) {
        this(DEFAULT_LANGUAGE, resource, after);
    }
    
    public MessageTemplate(Language language, String resource, MessageTemplate after) {
        this.input = () -> new ConcatInputStream(
                getResourceSupplier(new LanguageResource(language, resource)).get(),
                after.get());
    }
    
    public MessageTemplate(MessageTemplate before, String resource, MessageTemplate after) {
        this(before, DEFAULT_LANGUAGE, resource, after);
    }
    
    public MessageTemplate(MessageTemplate before, Language language, String resource, MessageTemplate after) {
        this.input = () -> new ConcatInputStream(
                before.get(),
                getResourceSupplier(new LanguageResource(language, resource)).get(),
                after.get());
    }
    
    @Override
    public InputStream get() {
        return input.get();
    }
    
    @Override
    public InputStream apply(Map<String, Object> variables) {
        return new VariableReplacingInputStream(get(), variables);
    }
    
    public String toString(Map<String, Object> variables) {
        return convert(apply(variables));
    }
    
    @Override
    public String toString() {
        return convert(get());
    }
    
    private Supplier<InputStream> getResourceSupplier(LanguageResource resource) {
        ResourceCache result = CACHED_RESOURCES.get(resource);
        if (result == null) {
            result = new ResourceCache(resourceLocation(resource));
            CACHED_RESOURCES.put(resource, result);
        }
        return result;
    }
    
    private String resourceLocation(LanguageResource resource) {
        return new StringJoiner("/")
                .add(RESOURCE_BASE)
                .add(resource.getLanguage().getDirectory())
                .add(resource.getResource())
                .toString()
                .replaceAll("//+", "/"); // Against double-slashes
    }
    
    private static String convert(InputStream stream) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        try {
            int length;
            byte[] buffer = new byte[1024];
            while ((length = stream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @AllArgsConstructor
    public static enum Language {
        ENGLISH("en");
        
        @Getter
        private final String directory;
    }
    
    @Value
    private static class LanguageResource {
        Language language;
        String resource;
    }
}
