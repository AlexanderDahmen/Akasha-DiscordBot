
package de.dahmen.alexander.akasha.core.conversation.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Alexander
 */
public class MessageTemplate extends MessageResource implements
        Supplier<InputStream>,
        Function<Map<String, Object>, InputStream>
{    
    
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
    
    public MessageTemplate(MessageTemplate... templates) {
        InputStream[] streams = new InputStream[templates.length];
        for (int i = 0; i < templates.length; i++) streams[i] = templates[i].get();
        this.input = () -> new ConcatInputStream(streams);
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
            return out.toString(StandardCharsets.UTF_8.name());
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
