
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public abstract class GeneratorConversationInstance implements Conversation.Instance, AutoCloseable {
    
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("conversation_threads");
    
    private final AtomicReference<Message> nextMessage;
    private final Condition responseAvailableOrFinished;
    private final Condition responseRequested;
    
    private boolean hasFinished;
    private boolean nextResponseAvailable;
    private Object nextResponse;
    private RuntimeException raisedException;
    private final Thread producer;
    
    public GeneratorConversationInstance() {
        this.responseAvailableOrFinished = new Condition();
        this.responseRequested = new Condition();
        this.nextMessage = new AtomicReference<>();
        this.producer = initProducer();
    }

    @Override
    public boolean isFinished() {
        return !waitForNext();
    }
    
    @Override
    public Object apply(Message message) {
        nextMessage.set(message);
        if (!waitForNext())
            throw new NoSuchElementException();
        nextResponseAvailable = false;
        return nextResponse;
    }

    @Override
    public void close() throws Exception {
        producer.interrupt();
        producer.join();
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    protected abstract void run() throws InterruptedException;
    
    protected final Message message() {
        return nextMessage.get();
    }
    
    protected final void yield(Object response) throws InterruptedException {
        nextResponse = response;
        nextResponseAvailable = true;
        responseAvailableOrFinished.set();
        responseRequested.await();
    }
    
    protected final void yield(Object... response) throws InterruptedException {
        yield((Object) response);
    }
    
    private Thread initProducer() {
        Thread result = new Thread(THREAD_GROUP, this::producerRoutine);
        result.setDaemon(true);
        result.start();
        return result;
    }
    
    private boolean waitForNext() {
        if (nextResponseAvailable) return true;
        if (hasFinished) return false;
        responseRequested.set();
        try {
            responseAvailableOrFinished.await();
        }
        catch (InterruptedException ignored) {
            hasFinished = true;
        }
        if (raisedException != null)
            throw raisedException;
        return !hasFinished;
    }
    
    private void producerRoutine() {
        try {
            responseRequested.await();
            this.run();
        }
        catch (InterruptedException ignored) {
            /* Will be handled in run() */
        }
        catch (RuntimeException ex) {
            raisedException = ex;
        }
        hasFinished = true;
        responseAvailableOrFinished.set();
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
