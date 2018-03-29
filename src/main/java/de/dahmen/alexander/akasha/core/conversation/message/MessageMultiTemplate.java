
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
 * Represents multiple {@code MessageTemplate} instances in a single file.<br>
 * This file is formatted similar to an {@code ini} configuration file,
 * for example:
 * 
 * <blockquote><pre>
 * Hello, World.
 * This is a section before any header, and can be used as fallback
 * if a non-existing section is requested.
 * 
 * [First]
 * This is the "First" section that can be retrieved by name.
 * 
 * [Linebr]
 * Linebreaks can be put into MessageMultiTemplates by entering a blank line.
 * 
 * The above blank line will result in a lineberak in the template,
 * whereas the linebreak that just happened will only result in a single space.
 * </pre></blockquote>
 * 
 * Previous sections can be included within the multi-template by writing a line
 * starting and ending with two dollar-signs, with the section name inbetween:
 * <blockquote><pre>
 * $$ section $$
 * </pre></blockquote>
 * 
 * @see MessageTemplate
 * @see MessageResource
 * @author Alexander
 */
public class MessageMultiTemplate {
    
    private static final Pattern SECTION = Pattern.compile("\\[(.*?)\\]");
    private static final Pattern INCLUDE = Pattern.compile("\\$\\$\\s*(.*?)\\s*\\$\\$");
    
    private final Map<String, MessageTemplate> templates;
    
    /**
     * Load a Multi-Template from a specified resource of the default language
     * @param resource Resource to load
     */
    public MessageMultiTemplate(String resource) {
        this(new MessageResource(), resource);
    }
    
    /**
     * Load a Multi-Template from a specified resource in the specified language
     * @param language Language of the resource to load
     * @param resource Resource to load
     */
    public MessageMultiTemplate(MessageResource.Language language, String resource) {
        this(new MessageResource(language), resource);
    }
    
    /**
     * Load a Multi-Template from a specified resource
     * @param resources MessageResource used to load the resource
     * @param resource Resource to load
     */
    public MessageMultiTemplate(MessageResource resources, String resource) {
        InputStream input = resources.getResource(resource);
        this.templates = parseStream(resources, input);
    }
    
    /**
     * Get a {@code MessageTemplate} from a specified section of the Multi-Template.
     * @param section Section of MessageTemplate to get
     * @return
     *      MessageTemplate of specified section, or of default section if
     *      no such section exists, or null if no such section and no default
     *      section exist
     * @see MessageTemplate
     */
    public MessageTemplate get(String section) {
        // Get template associated with key,
        // or template associated with null if none exists
        return optional(section).orElse(defaultSection());
    }
    
    /**
     * Get an Optional of the {@code MessageTemplate} from a specified section
     * of the Multi-Template.
     * @param section Section of the MessageTemplate to get
     * @return Optional containing a MessageTemplate,
     *      or an empty Optional if no such section exists
     */
    public Optional<MessageTemplate> optional(String section) {
        return Optional.ofNullable(templates.get(section));
    }
    
    /**
     * Get the {@code MessageTemplate} in front of any section.
     * @return MessageTemplate or null if no text came before the first section
     */
    public MessageTemplate defaultSection() {
        return templates.get(null);
    }
    
    private Map<String, MessageTemplate> parseStream(MessageResource resource, InputStream input) {
        
        Map<String, byte[]> sections = new HashMap<>();
        Map<String, MessageTemplate> result = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String section = null;
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher sectionRegex = SECTION.matcher(line);
                if (sectionRegex.matches()) {
                    // Store previous section's body
                    byte[] body = out.toByteArray();
                    sections.put(section, body);
                    result.put(section, new MessageTemplate(resource, new ConstInputStreamSupplier(body))
                            .preSet("__SECTION__", section));
                    
                    // Allocate new output array
                    out = new ByteArrayOutputStream(1024);
                    
                    // Start new header line
                    section = sectionRegex.group(1).trim();
                } else {
                    // Check for include lines
                    Matcher includeRegex = INCLUDE.matcher(line);
                    if (includeRegex.matches()) {
                        // When include line is detected, write previous section into body
                        String includeSection = includeRegex.group(1).trim();
                        byte[] includeBody = sections.get(includeSection);
                        if (includeBody == null) {
                            out.write(("$$ INCLUDE ERROR :: [" + includeSection + "] :: NOT FOUND $$")
                                    .getBytes(MessageResource.CHARSET));
                        } else {
                            out.write(includeBody);
                        }
                    } else if (!line.isEmpty()) {
                        // Append body line
                        out.write(line.getBytes(MessageResource.CHARSET));
                    }
                    out.write('\n');
                }
            }
            
            // Store last body
            result.put(section, new MessageTemplate(resource,
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
