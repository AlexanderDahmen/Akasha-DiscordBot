
package de.dahmen.alexander.akasha.core.conversation.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Templates for text Messages loaded from Resources<br>
 * <p>
 * MessageTemplates are located in <code>message/{language}/{resource}</code>
 * inside the <code>src/main/resources/</code> resource directory.
 * </p><p>
 * Templates may contain two types of variables:
 * <ul>
 * <li>
 *   <code>${var}</code> - Insert a <em>variable</em> (here named "var")
 *   into the template
 * </li><li>
 *   <code>#{include}</code> - Include another resource
 *   (of the same language, here named "include") into the template
 * </li>
 * </ul>
 * </p>
 * 
 * @see MessageResource
 * @see TemplateInputStream
 * @author Alexander
 */
public class MessageTemplate implements Supplier<InputStream>, Function<Map<String, Object>, InputStream> {
    
    private final MessageResource resources;
    private final Supplier<InputStream> inputSupplier;
    private final Map<String, Object> presetVariables;
    
    private String lazyToString = null;
    
    /**
     * Load a MessageTemplate from a specified message resource in the default language
     * @param resource Resource to load
     */
    public MessageTemplate(String resource) {
        this(new MessageResource(), resource);
    }
    
    /**
     * Load a MessageTemplate from a specified message resource in a specified language
     * @param language Language of the resource
     * @param resource Resource to load
     */
    public MessageTemplate(MessageResource.Language language, String resource) {
        this(new MessageResource(language), resource);
    }
    
    /**
     * Load a MessageTemplate from a specified resource
     * @param resources MessageResource used to load the resource
     * @param resource Resource to load
     */
    public MessageTemplate(MessageResource resources, String resource) {
        this(resources, resources.getResourceSupplier(resource));
    }
    
    /**
     * Create a MessageTemplate of a specified language from an InputStream-Supplier
     * @param language Language of the MessageTemplate
     * @param inputSupplier Supplier for message content InputStreams
     */
    public MessageTemplate(MessageResource.Language language, Supplier<InputStream> inputSupplier) {
        this(new MessageResource(language), inputSupplier);
    }
    
    
    /**
     * Join multiple MessageTemplates, using a specified language
     * @param language Language (i.e. used for includes from within templates)
     * @param templates Templates to join into this combined MessageTemplate
     */
    public MessageTemplate(MessageResource.Language language, MessageTemplate... templates) {
        this(new MessageResource(language), templates);
    }
    
    /**
     * Join multiple MessageTemplates, using a specified MessageResource
     * @param resources MessageResource (i.e. used for includes from within templates)
     * @param templates Templates to join into this combined MessageTemplate
     */
    public MessageTemplate(MessageResource resources, MessageTemplate... templates) {
        this.presetVariables = new HashMap<>();
        this.inputSupplier = join(templates);
        this.resources = resources;
    }
    
    /**
     * Create a MessageTemplate with a specified MessageResource from an InputStream-Supplier
     * @param resource MessageResource of the MessageTemplate
     * @param inputSupplier Supplier for message content InputStreams
     */
    public MessageTemplate(MessageResource resource, Supplier<InputStream> inputSupplier) {
        this.presetVariables = new HashMap<>();
        this.inputSupplier = inputSupplier;
        this.resources = resource;
    }
    
    /**
     * Join multiple MessageTemplates, using the first MessageTemplate's MessageResource
     * to load further resources
     * @param templates Templates to join (must not be empty or null)
     */
    public MessageTemplate(MessageTemplate... templates) {
        this.presetVariables = new HashMap<>();
        this.inputSupplier = join(templates);
        this.resources = templates[0].resources;
    }
    
    /**
     * Add a preset variable that will be present in all evaluations of this
     * template (unless they're overwritten by variables passed for templating)
     * @param variable Preset variable name
     * @param preset Preset variable value
     * @return This
     */
    public MessageTemplate preSet(String variable, Object preset) {
        presetVariables.put(variable, preset);
        return this;
    }
    
    /**
     * Get an InputStream of the MessageTemplate content
     * @return InputStream containing the template text
     */
    @Override
    public InputStream get() {
        return inputSupplier.get();
    }
    
    /**
     * Get an InputStream of a processed MessageTemplate, with variables replaced
     * and includes loaded into the template
     * @param variables Variable values to replace
     * @return InputStream of the processed template content
     */
    @Override
    public InputStream apply(Map<String, Object> variables) {
        return new TemplateInputStream(get(), variables);
    }
    
    /**
     * Convert the InputStream of {@link MessageTemplate#apply(java.util.Map) apply(Map)}
     * to a String
     * @param variables Variables to insert into the template
     * @return String of the processed template content
     */
    public String toString(Map<String, Object> variables) {
        return convert(apply(variables));
    }
    
    /**
     * Convert the InputStream of {@link MessageTemplate#get() get()} to a String
     * @return String of the template text
     */
    @Override
    public String toString() {
        return (lazyToString == null) ?
                (lazyToString = convert(get())) :
                lazyToString;
    }
    
    /**
     * Instantiate a {@link BuildMessageTemplate BuildMessageTemplate}
     * @return New BuildMessageTemplate
     */
    public BuildMessageTemplate build() {
        return new BuildMessageTemplate();
    }
    
    /**
     * Set a variable on the MessageTemplate.<br>
     * Shorthand for {@code .build().set(String, Object)}, instantiates a
     * {@link BuildMessageTemplate BuildMessageTemplate}
     * 
     * @param variable Variable name
     * @param value Variable value
     * @return New BuildMessageTemplate for chaining
     */
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
    
    private Supplier<InputStream> join(MessageTemplate... templates) {
        if (templates.length < 1)
            throw new IllegalArgumentException("Empty MessageTemplate array");
        
        InputStream[] streams = new InputStream[templates.length];
        for (int i = 0; i < templates.length; i++)
            streams[i] = templates[i].get();
        
        return (() -> new ConcatInputStream(streams));
    }
    
    /**
     * Nested class for intermediary in-build MessageTemplates
     */
    public class BuildMessageTemplate {
        private final Map<String, Object> variables;

        private BuildMessageTemplate() {
            // Initialize with preset variables of outer instance
            this.variables = new HashMap<>(presetVariables);
        }
        
        /**
         * Set a variable on the MessageTemplate
         * @param variable Variable name
         * @param value Variable value
         * @return This for chaining
         */
        public BuildMessageTemplate set(String variable, Object value) {
            variables.put(variable, value);
            return this;
        }
        
        /**
         * Process and convert the MessageTemplate to a String using the
         * accumulated templating variables
         * @return String of the processed template
         */
        @Override
        public String toString() {
            return MessageTemplate.this.toString(variables);
        }
    }
}
