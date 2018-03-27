
package de.dahmen.alexander.akasha.core.conversation.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

/**
 *
 * @author Alexander
 */
public class ConcatInputStream extends InputStream {
    private final Deque<InputStream> streams;

    public ConcatInputStream(Collection<InputStream> streams) {
        this.streams = new ArrayDeque<>(streams);
    }
    
    public ConcatInputStream(InputStream... streams) {
        this.streams = new ArrayDeque<>(streams.length);
        Collections.addAll(this.streams, streams);
    }
    
    private void nextStream() throws IOException {
        streams.removeFirst().close();
    }
    
    @Override
    public int read() throws IOException {
        int result = -1;
        while (!streams.isEmpty() &&
                (result = streams.getFirst().read()) == -1)
        {
            nextStream();
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = -1;
        while (!streams.isEmpty() &&
                (result = streams.getFirst().read(b, off, len)) == -1)
        {
            nextStream();
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = 0L;
        while (skipped < n && !streams.isEmpty()) {
            long skip = streams.getFirst().skip(n - skipped);
            if (skip > 0) skipped += skip;
            else nextStream();
        }
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return streams.isEmpty() ? 0 : streams.getFirst().available();
    }

    @Override
    public void close() throws IOException {
        while (!streams.isEmpty())
            nextStream();
    }
}
