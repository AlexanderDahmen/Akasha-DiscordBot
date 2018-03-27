
package de.dahmen.alexander.akasha.core.conversation.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 *
 * @author Alexander
 */
public class ResourceCache implements Supplier<InputStream> {
    private final static ClassLoader CL = ResourceCache.class.getClassLoader();
    private final static int DEFAULT_BUFFER_SIZE = 1024;
    
    private final byte[] content;

    public ResourceCache(String resource) {
        this.content = loadResource(resource, DEFAULT_BUFFER_SIZE);
    }
    
    public ResourceCache(String resource, int bufferSize) {
        this.content = loadResource(resource, bufferSize);
    }
    
    @Override
    public InputStream get() {
        return new ByteArrayInputStream(content);
    }
    
    private byte[] loadResource(String resource, int bufferSize) {
        InputStream is = CL.getResourceAsStream(resource);
        if (is == null)
            throw new RuntimeException("Resource not found: " + resource);
        
        int read;
        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream os = new ByteArrayOutputStream(bufferSize);
        
        try {
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        return os.toByteArray();
    }
}
