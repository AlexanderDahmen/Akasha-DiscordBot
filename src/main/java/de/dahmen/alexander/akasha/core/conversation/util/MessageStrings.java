
package de.dahmen.alexander.akasha.core.conversation.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Alexander
 */
public class MessageStrings extends MessageResource {
    
    private final Map<String, String> strings;
    
    public MessageStrings(String resource) {
        this(DEFAULT_LANGUAGE, resource);
    }
    
    public MessageStrings(Language language, String resource) {
        this.strings = Collections.unmodifiableMap(
                parse(getResourceSupplier(new LanguageResource(language, resource))
                        .get()));
    }
    
    public String get(String key) {
        return strings.get(key);
    }
    
    public Map<String, String> getStrings() {
        return strings;
    }
    
    private Map<String, String> parse(InputStream input) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map((l) -> l.trim())                                       // Trim lines
                    .filter((l) -> !l.isEmpty())                                // Ignore empty lines
                    .map((l) -> l.split(":", 2))                                // Split lines into "Key: Value"
                    .map((s) -> (s.length == 1) ? new String[]{s[0], ""} : s)   // Value is empty if ":" is missing
                    .collect(Collectors.toMap(
                            (s) -> s[0].trim(),
                            (s) -> parseEscapes(s[1].trim())));                 // Create Key-Value Map
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private String parseEscapes(String string) {
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
