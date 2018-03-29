
package de.dahmen.alexander.akasha.core.conversation.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Alexander
 */
public class InputStreamBuffer {
    private static final int DEFAULT_BLOCK_SIZE = 1024;
    
    private final int blockSize;
    private final Deque<int[]> blocks;
    
    private int head, tail;
    private long totalEnqueued;
    private int highWaterMark, disposedBlocks;

    public InputStreamBuffer() {
        this(DEFAULT_BLOCK_SIZE);
    }
    
    public InputStreamBuffer(int blockSize) {
        if (blockSize < 1)
            throw new IllegalArgumentException("blockSize < 1");
        
        this.blockSize = blockSize;
        this.blocks = new ArrayDeque<>();
        this.head = this.tail = this.highWaterMark = this.disposedBlocks = 0;
        this.totalEnqueued = 0L;
        
        this.createBlock();
    }
    
    public void add(int e) {
        blocks.peekLast()[tail % blockSize] = e;
        tail++;
        
        if ((tail > 0) && ((tail % blockSize) == 0))
            createBlock();
        
        totalEnqueued++;
        highWaterMark = Math.max(highWaterMark, size());
    }
    
    public void addAll(int[] e) {
        for (int i : e)
            add(i);
    }
    
    public void store(InputStream in) throws IOException {
        for (int i = in.read(); i != -1; i = in.read())
            add(i);
    }
    
    public int read() {
        if (isEmpty())
            return -1;
        if (this.head >= this.blockSize)
            throw new AssertionError("head >= blockSize");
        
        int r = blocks.peek()[head];
        head++;
        
        if (head == blockSize)
            destroyBlock();
        
        if (isEmpty()) {
            if (blocks.size() != 1)
                throw new AssertionError("leftover blocks");
            head = tail = 0;
        }
        
        return r;
    }
    
    public void clear() {
        blocks.clear();
        createBlock();
        head = tail = highWaterMark = disposedBlocks = 0;
        totalEnqueued = 0L;
    }
    
    public boolean isEmpty() {
        return (head == tail);
    }
    
    public int size() {
        return (tail - head);
    }

    public int getHighWaterMark() {
        return highWaterMark;
    }

    public int getDisposedBlocks() {
        return disposedBlocks;
    }

    public long getTotalEnqueued() {
        return totalEnqueued;
    }
    
    public int getBlocksInUse() {
        return blocks.size();
    }
    
    private void destroyBlock() {
        blocks.removeFirst();
        head -= blockSize;
        tail -= blockSize;
        disposedBlocks++;
    }
    
    private void createBlock() {
        blocks.addLast(new int[blockSize]);
    }
}
