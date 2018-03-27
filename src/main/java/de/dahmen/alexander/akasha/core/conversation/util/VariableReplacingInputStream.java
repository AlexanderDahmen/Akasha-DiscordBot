
package de.dahmen.alexander.akasha.core.conversation.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author Alexander
 */
public class VariableReplacingInputStream extends InputStream {
    private final static Charset CHARSET = Charset.forName("UTF-8");
    private final static int DOLLAR_CODE =  "$".codePointAt(0);
    private final static int OPEN_CODE =    "{".codePointAt(0);
    private final static int CLOSE_CODE =   "}".codePointAt(0);
    private final static int ESCAPE_CODE =  "\\".codePointAt(0);
    
    private final InputStream delegate;
    private final Map<String, Object> variables;
    private final Queue<Integer> readAheadBuffer;

    public VariableReplacingInputStream(
            InputStream delegate,
            Map<String, Object> variables)
    {
        this.delegate = delegate;
        this.variables = variables;
        this.readAheadBuffer = new LinkedList<>();
    }

    @Override
    public int read() throws IOException {
        // If buffer is not empty, read from there
        if (!readAheadBuffer.isEmpty())
            return readAheadBuffer.poll();
        
        // Read from input stream, stop on stream end
        int next = delegate.read();
        if (next == (-1))
            return (-1);
        
        if (next == DOLLAR_CODE) {
            // Look for the "${" pattern
            // If it's only a dollar sign, return that and put the second-next in the buffer
            int afterDollar = delegate.read();
            if (afterDollar == OPEN_CODE) {
                // Read variable until }
                String variable = readUntilClosedBrace();
                Object value = variables.get(variable.trim());
                
                // Replace empty variables with original variable-expression
                if (value == null)
                    value = ("${" + variable + "}");
                
                // Store variable value in buffer
                storeInBuffer(String.valueOf(value));
                
                // Return first character of the buffer or the next character if the buffer is empty
                return (readAheadBuffer.isEmpty())
                        ? delegate.read()
                        : readAheadBuffer.poll();
            } else {
                readAheadBuffer.add(afterDollar);
                return DOLLAR_CODE;
            }
        } else if (next == ESCAPE_CODE) {
            // Decide what escape code this is:
            // \${
            // \\${
            int afterEscape = delegate.read();
            if (afterEscape == DOLLAR_CODE) {
                // Escape, then Dollar
                int afterDollar = delegate.read();
                if (afterDollar == OPEN_CODE) {
                    // Escape, then Dollar, then Brace -> "${"
                    storeInBuffer("${");
                } else {
                    // Escape, then Dollar -> "\$"
                    storeInBuffer("\\$");
                }
            } else if (afterEscape == ESCAPE_CODE) {
                // Double Escape
                int afterDoubleEscape = delegate.read();
                if (afterDoubleEscape == DOLLAR_CODE) {
                    int afterDoubleEscapeDollar = delegate.read();
                    if (afterDoubleEscapeDollar == OPEN_CODE)
                        // Double Escape, then "${" -> "\${"
                        storeInBuffer("\\${");
                    else
                        // Double Escape, then "$" -> "\\$"
                        storeInBuffer("\\\\$");
                } else {
                    // Double Escape -> "\\"
                    storeInBuffer("\\\\");
                }
            } else {
                // Escape without relevance -> "\"
                storeInBuffer("\\");
            }
            // Return first element of buffer
            return readAheadBuffer.poll();
        } else {
            // Not a special character -> Pass through
            return next;
        }
    }
    
    private String readUntilClosedBrace() throws IOException {
        int next;
        StringBuilder result = new StringBuilder();
        for (;;) {
            next = delegate.read();
            if (next == (-1))
                throw new IOException("Stream ended within a template variable");
            else if (next == CLOSE_CODE)
                return result.toString().trim();
            else
                result.appendCodePoint(next);
        }
    }
    
    private void storeInBuffer(String value) throws IOException {
        for (byte b : value.getBytes(CHARSET)) {
            readAheadBuffer.add(Byte.toUnsignedInt(b));
        }
    }
}
