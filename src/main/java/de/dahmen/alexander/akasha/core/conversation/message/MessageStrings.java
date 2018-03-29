
package de.dahmen.alexander.akasha.core.conversation.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Map for Key-Value pairs parsed from a resource file.<br>
 * The format of Strings resource files is:
 * <blockquote><pre>
 * Key1: Value1
 * Key2: Value2
 * [...]
 * </pre></blockquote>
 * Both keys and values are <em>trimmed</em> when parsed.
 * 
 * @author Alexander
 */
public class MessageStrings extends AbstractMap<String, String> {
    
    private final Map<String, String> strings;
    
    public MessageStrings(String resource) {
        this(new MessageResource(), resource);
    }
    
    public MessageStrings(MessageResource.Language language, String resource) {
        this(new MessageResource(language), resource);
    }
    
    public MessageStrings(MessageResource resources, String resource) {
        InputStream stream = resources.getResource(resource);
        this.strings = Collections.unmodifiableMap(parseStream(stream));
    }
    
    @Override
    public String get(Object key) {
        return strings.get(key);
    }
    
    @Override
    public Set<Entry<String, String>> entrySet() {
        return strings.entrySet();
    }

    @Override
    public boolean containsKey(Object key) {
        return strings.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return strings.containsValue(value);
    }

    @Override
    public Set<String> keySet() {
        return strings.keySet();
    }

    @Override
    public Collection<String> values() {
        return strings.values();
    }
    
    private Map<String, String> parseStream(InputStream input) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8)))
        {
            return reader.lines()
                    .map((l) -> l.trim())                                       // Trim lines
                    .filter((l) -> !l.isEmpty())                                // Ignore empty lines
                    .map((l) -> l.split(":", 2))                                // Split lines into "Key: Value"
                    .map((s) -> (s.length == 1) ? new String[]{s[0], ""} : s)   // Value is empty if ":" is missing
                    .collect(Collectors.toMap(
                            (s) -> s[0].trim(),
                            (s) -> parseEscapeString(s[1].trim())));            // Create Key-Value Map
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private String parseEscapeString(String string) {
        StringReader input = new StringReader(string);
        StringBuilder result = new StringBuilder(string.length());
        boolean inEscape = false;
        try {
            int c;
            while ((c = input.read()) != -1) {
                if (inEscape) {
                    if (c == -1) {
                        result.appendCodePoint('\\');
                    } else {
                        switch (c) {
                            case 'b': result.append('\b'); break;
                            case 'f': result.append('\f'); break;
                            case 'n': result.append('\n'); break;
                            case 'r': result.append('\r'); break;
                            case 't': result.append('\t'); break;
                            case '\'': result.append('\''); break;
                            case '\\': result.append('\\'); break;
                            case '"': result.append('"'); break;
                            default: throw new RuntimeException("Unknown escape: \\" + (char)c);
                        }
                    }
                    inEscape = false;
                } else {
                    if (c == '\\') {
                        inEscape = true;
                    } else {
                        result.appendCodePoint(c);
                    }
                }
            }
            return result.toString();
        }
        catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }
}
