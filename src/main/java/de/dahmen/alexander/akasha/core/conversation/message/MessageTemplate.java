
package de.dahmen.alexander.akasha.core.conversation.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Alexander
 */
public class MessageTemplate implements Supplier<InputStream>, Function<Map<String, Object>, InputStream> {
    
    private final MessageResource resources;
    private final Supplier<InputStream> inputSupplier;
    
    public MessageTemplate(String resource) {
        this(new MessageResource(), resource);
    }
    
    public MessageTemplate(MessageResource.Language language, String resource) {
        this(new MessageResource(language), resource);
    }
    
    public MessageTemplate(MessageResource resources, String resource) {
        this.inputSupplier = resources.getResourceSupplier(resource);
        this.resources = resources;
    }

    public MessageTemplate(MessageResource.Language language, Supplier<InputStream> inputSupplier) {
        this(new MessageResource(language), inputSupplier);
    }
    
    public MessageTemplate(MessageResource resource, Supplier<InputStream> inputSupplier) {
        this.inputSupplier = inputSupplier;
        this.resources = resource;
    }
    
    public MessageTemplate(MessageResource.Language language, MessageTemplate... templates) {
        this(new MessageResource(language), templates);
    }
    
    public MessageTemplate(MessageResource resources, MessageTemplate... templates) {
        this.inputSupplier = join(templates);
        this.resources = resources;
    }
    
    public MessageTemplate(MessageTemplate... templates) {
        this.inputSupplier = join(templates);
        this.resources = templates[0].resources;
    }
    
    private Supplier<InputStream> join(MessageTemplate... templates) {
        if (templates.length < 1)
            throw new IllegalArgumentException("Empty MessageTemplate array");
        
        InputStream[] streams = new InputStream[templates.length];
        for (int i = 0; i < templates.length; i++)
            streams[i] = templates[i].get();
        
        return (() -> new ConcatInputStream(streams));
    }
    
    @Override
    public InputStream get() {
        return inputSupplier.get();
    }
    
    @Override
    public InputStream apply(Map<String, Object> variables) {
        return new TemplateInputStream(get(), variables);
    }
    
    public String toString(Map<String, Object> variables) {
        return convert(apply(variables));
    }
    
    @Override
    public String toString() {
        return convert(get());
    }
    
    public BuildMessageTemplate build() {
        return new BuildMessageTemplate();
    }
    
    // Shorthand for .build().set(String, Object)
    public BuildMessageTemplate set(String variable, Object value) {
        return new BuildMessageTemplate().set(variable, value);
    }
    
    private static String convert(InputStream stream) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        try {
            int length;
            byte[] buffer = new byte[1024];
            while ((length = stream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            return out.toString(MessageResource.CHARSET.name());
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public class BuildMessageTemplate {
        private final Map<String, Object> variables;

        public BuildMessageTemplate() {
            this.variables = new HashMap<>();
        }
        
        public BuildMessageTemplate set(String variable, Object value) {
            variables.put(variable, value);
            return this;
        }

        @Override
        public String toString() {
            return MessageTemplate.this.toString(variables);
        }
    }
}
