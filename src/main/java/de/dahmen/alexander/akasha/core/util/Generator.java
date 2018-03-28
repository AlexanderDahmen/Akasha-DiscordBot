
package de.dahmen.alexander.akasha.core.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Generator using threads
 * //TODO Write this better
 * 
 * @author Alexander
 * @param <T> Generated type
 */
public abstract class Generator<T> implements Iterable<T>, AutoCloseable {
    
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("generator_functions");
    
    private boolean hasFinished;
    private boolean nextItemAvailable;
    private T nextItem;
    private RuntimeException raisedException;
    private final Thread producer;
    
    private final Condition itemAvailableOrFinished;
    private final Condition itemRequested;

    public Generator() {
        this.itemAvailableOrFinished = new Condition();
        this.itemRequested = new Condition();
        this.producer = initProducer();
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return waitForNext();
            }
            
            @Override
            public T next() {
                if (!waitForNext())
                    throw new NoSuchElementException();
                nextItemAvailable = false;
                return nextItem;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
            
            private boolean waitForNext() {
                if (nextItemAvailable) return true;
                if (hasFinished) return false;
                itemRequested.set();
                try {
                    itemAvailableOrFinished.await();
                }
                catch (InterruptedException ignored) {
                    hasFinished = true;
                }
                if (raisedException != null)
                    throw raisedException;
                return !hasFinished;
            }
        };
    }
    
    @Override
    public void close() throws InterruptedException {
        producer.interrupt();
        producer.join();
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        close(); // Close on finalization
        super.finalize();
    }
    
    protected abstract void run() throws InterruptedException;
    
    protected final void yield(T element) throws InterruptedException {
        nextItem = element;
        nextItemAvailable = true;
        itemAvailableOrFinished.set();
        itemRequested.await();
    }

    private Thread initProducer() {
        Thread result = new Thread(THREAD_GROUP, this::producerRoutine);
        result.setDaemon(true);
        result.start();
        return result;
    }
    
    private void producerRoutine() {
        try {
            itemRequested.await();
            Generator.this.run();
        }
        catch (InterruptedException ignored) {
            /* Will be handled in run() */
        }
        catch (RuntimeException ex) {
            raisedException = ex;
        }
        hasFinished = true;
        itemAvailableOrFinished.set();
    }
    
    private static class Condition {
        private boolean isSet = false;
        public synchronized void set() {
            isSet = true;
            notify();
        }
        public synchronized void await() throws InterruptedException {
            try {
                if (isSet) return;
                wait();
            }
            finally {
                isSet = false;
            }
        }
    }
}
