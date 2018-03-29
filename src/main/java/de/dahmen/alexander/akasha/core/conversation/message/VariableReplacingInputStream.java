
package de.dahmen.alexander.akasha.core.conversation.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.StringJoiner;

/**
 *
 * @author Alexander
 */
public class VariableReplacingInputStream extends InputStream {
    private final static int DOLLAR_CODE =  '$';    // $ -> Variable
    private final static int HASH_CODE =    '#';    // # -> Include
    private final static int OPEN_CODE =    '{';    // { -> Start identifier
    private final static int CLOSE_CODE =   '}';    // } -> End identifier
    private final static int ESCAPE_CODE =  '\\';   // \ -> Escape $ or #
    
    private final InputStream delegate;
    private final Map<String, Object> variables;
    private final MessageResource includeResources;
    private final InputStreamBuffer readAheadBuffer;
    
    public VariableReplacingInputStream(
            InputStream delegate,
            Map<String, Object> variables)
    {
        this(delegate, variables, MessageResource.DEFAULT_LANGUAGE);
    }
    
    public VariableReplacingInputStream(
            InputStream delegate,
            Map<String, Object> variables,
            MessageResource.Language includeLanguage)
    {
        this(delegate, variables, new MessageResource(includeLanguage));
    }
    
    public VariableReplacingInputStream(
            InputStream delegate,
            Map<String, Object> variables,
            MessageResource includeResource)
    {
        this.delegate = delegate;
        this.variables = variables;
        this.includeResources = includeResource;
        this.readAheadBuffer = new InputStreamBuffer();
    }

    @Override
    public int read() throws IOException {
        // If buffer is not empty, read from there
        if (!readAheadBuffer.isEmpty())
            return readAheadBuffer.read();
        
        // Read from input stream, stop on stream end
        int next = delegate.read();
        if (next == (-1))
            return (-1);
        
        switch (next) {
            case DOLLAR_CODE:
                // Look for the "${" pattern
                // If it's only a dollar sign, return that and put the second-next in the buffer
                int afterDollar = delegate.read();
                if (afterDollar == OPEN_CODE) {
                    // Read variable until "}"
                    String variable = readUntilClosedBrace();
                    Object value = variables.get(variable.trim());
                    
                    // Replace empty variables with original variable-expression
                    if (value == null)
                        value = new StringBuilder(variable.length() + 3)
                                .append('$').append('{').append(variable).append('}')
                                .toString();
                    
                    // Store variable value in buffer
                    storeInBuffer(String.valueOf(value));
                    
                    // Return first character of the buffer or the next character if the buffer is empty
                    return (readAheadBuffer.isEmpty()) ? delegate.read() : readAheadBuffer.read();
                } else {
                    // Write $[*]
                    readAheadBuffer.add(afterDollar);
                    return DOLLAR_CODE;
                }
            case HASH_CODE:
                // Look for the "#{" pattern
                // If it's only a hash sign, return that and put the second-next in the buffer
                int afterHash = delegate.read();
                if (afterHash == OPEN_CODE) {
                    // Read resource identifier until "}"
                    String identifier = readUntilClosedBrace();
                    
                    try {
                        // Load resource into sub VariableReplacingInputStream
                        InputStream resource = includeResources.getResource(identifier);
                        InputStream includeStream = new VariableReplacingInputStream(
                                resource, variables, includeResources);
                        
                        // Store result of sub-stream into buffer
                        readAheadBuffer.store(includeStream);
                    }
                    catch (RuntimeException ex) {
                        storeInBuffer(new StringJoiner(" :: ", "#{", "}")
                                .add("INCLUDE ERROR")
                                .add(identifier)
                                .add(ex.getClass().getName())
                                .add(ex.getMessage())
                                .toString());
                    }
                    
                    // Return first character of the buffer or the next character if the buffer is empty
                    return (readAheadBuffer.isEmpty()) ? delegate.read() : readAheadBuffer.read();
                } else {
                    // Write #[*]
                    readAheadBuffer.add(afterHash);
                    return HASH_CODE;
                }
            case ESCAPE_CODE:
                // Decide what escape code this is:
                /*      \$   => $
                \#   => #
                \\$  => \$
                \\#  => \#
                \[*] => \[*]
                */
                int afterEscape = delegate.read();
                switch (afterEscape) {
                    case DOLLAR_CODE:
                    case HASH_CODE:
                        readAheadBuffer.add(afterEscape);
                        break;
                    case ESCAPE_CODE:
                        readAheadBuffer.add(ESCAPE_CODE);
                        break;
                    default:
                        readAheadBuffer.add(ESCAPE_CODE);
                        readAheadBuffer.add(afterEscape);
                }
                // Return first element of buffer
                return readAheadBuffer.read();
            default:
                // Not a special character -> Pass through
                return next;
        }
    }
    
    private String readUntilClosedBrace() throws IOException {
        int next;
        StringBuilder result = new StringBuilder();
        for (;;) {
            next = delegate.read();
            switch (next) {
                case -1:
                    throw new IOException("Stream ended within a template variable");
                case CLOSE_CODE:
                    return result.toString().trim();
                default:
                    result.appendCodePoint(next);
                    break;
            }
        }
    }
    
    private void storeInBuffer(String value) throws IOException {
        for (byte b : value.getBytes(MessageResource.CHARSET)) {
            readAheadBuffer.add(Byte.toUnsignedInt(b));
        }
    }
}
