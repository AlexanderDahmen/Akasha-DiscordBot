
package de.dahmen.alexander.akasha.core.conversation.message;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Alexander
 */
public class MessageMultiTemplate {
    
    private static final Pattern HEADER = Pattern.compile("\\[(.*?)\\]", Pattern.CASE_INSENSITIVE);
    
    private final Map<String, MessageTemplate> templates;
    
    public MessageMultiTemplate(String resource) {
        this(new MessageResource(), resource);
    }
    
    public MessageMultiTemplate(MessageResource.Language language, String resource) {
        this(new MessageResource(language), resource);
    }
    
    public MessageMultiTemplate(MessageResource resources, String resource) {
        InputStream input = resources.getResource(resource);
        this.templates = parseStream(resources, input);
    }
    
    public MessageTemplate get(String key) {
        // Get template associated with key,
        // or template associated with null if none exists
        return optional(key).orElse(noHeader());
    }
    
    public Optional<MessageTemplate> optional(String key) {
        return Optional.ofNullable(templates.get(key));
    }
    
    public MessageTemplate noHeader() {
        return templates.get(null);
    }
    
    private Map<String, MessageTemplate> parseStream(MessageResource resource, InputStream input) {
        
        Map<String, MessageTemplate> result = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String key = null;
            boolean emptyLine = false;
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher match = HEADER.matcher(line);
                if (match.matches()) {
                    // Store previous body, allocate new body
                    result.put(key, new MessageTemplate(resource,
                            new ConstInputStreamSupplier(out.toByteArray())));
                    
                    out = new ByteArrayOutputStream(1024);
                    
                    // Start new header line
                    key = match.group(1);
                    emptyLine = false;
                } else {
                    // Append a newline if a blank line was previously input
                    if (emptyLine)
                        out.write('\n');
                    
                    // Append body line
                    out.write(line.getBytes(MessageResource.CHARSET));
                    
                    // Append a blank if last line was not empty
                    if (!emptyLine)
                        out.write(' ');
                    
                    // Update wether or not this line was empty
                    emptyLine = line.trim().isEmpty();
                }
            }
            
            // Store last body
            result.put(key, new MessageTemplate(resource,
                    new ConstInputStreamSupplier(out.toByteArray())));
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }
    
    private static class ConstInputStreamSupplier implements Supplier<InputStream> {
        
        private final byte[] bytes;
        
        public ConstInputStreamSupplier(byte[] bytes) {
            this.bytes = bytes;
        }
        
        @Override
        public InputStream get() {
            return new ByteArrayInputStream(bytes);
        }
    }
}
