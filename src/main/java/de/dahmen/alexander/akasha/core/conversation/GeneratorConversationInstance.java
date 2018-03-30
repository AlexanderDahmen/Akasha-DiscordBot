
package de.dahmen.alexander.akasha.core.conversation;

import de.dahmen.alexander.akasha.config.ConversationConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.dv8tion.jda.core.entities.Message;

/**
 * Conversation that functions like a Generator.<br>
 * This implementation creates a {@code Thread} when instantiated.
 * 
 * @author Alexander
 */
public abstract class GeneratorConversationInstance implements Conversation.Instance {
    
    private static final ThreadGroup THREAD_GROUP = new ThreadGroup("conversation_threads");
    
    private final AtomicReference<Message> nextMessage;
    private final Condition responseAvailableOrFinished;
    private final Condition responseRequested;
    private final List<Object> responseBuffer;
    private final Thread producer;
    private final long timeout;
    
    private boolean hasFinished;
    private boolean nextResponseAvailable;
    private Exception raisedException;

    public GeneratorConversationInstance(ConversationConfig config) {
        this(config.getTimeoutMilliseconds());
    }
    
    public GeneratorConversationInstance(long timeoutMillis) {
        this.responseAvailableOrFinished = new Condition();
        this.responseRequested = new Condition();
        this.nextMessage = new AtomicReference<>();
        this.responseBuffer = new ArrayList<>();
        this.producer = initProducer();
        this.timeout = timeoutMillis;
    }

    @Override
    public boolean isFinished() {
        return hasFinished;
    }
    
    @Override
    public Object apply(Message message) {
        nextMessage.set(message);
        if (waitForNext()) {
            nextResponseAvailable = false;
        } else {
            Optional.ofNullable(exitResponse())
                    .ifPresent(responseBuffer::add);
        }
        return bufferToResponseObject();
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
    
    /**
     * Generator implementation of the conversation.<br>
     * Use {@code respond(Object)} to store a response to be sent, and
     * {@code yield()} to send off the responses.
     * 
     * @throws InterruptedException If the execution is interrupted
     * @see GeneratorConversationInstance#yield()
     * @see GeneratorConversationInstance#yield(java.lang.Object)
     * @see GeneratorConversationInstance#respond(java.lang.Object)
     * @see GeneratorConversationInstance#message() 
     */
    protected abstract void run() throws InterruptedException;
    
    /**
     * The last message to send when generation finishes
     * @return Last response after {@code run()} exits
     * @see GeneratorConversationInstance#run() 
     */
    protected abstract Object exitResponse();
    
    /**
     * The current incoming message of the active conversation
     * @return The latest Message sent in this conversation
     */
    protected final Message message() {
        return nextMessage.get();
    }
    
    /**
     * Queue up a responses to the conversation
     * @param response A response to the conversation
     */
    protected final void respond(Object response) {
        responseBuffer.add(response);
    }
    
    /**
     * Yield the conversation to the user until the next incoming Message arrives.<br>
     * That message will be available with {@code message()}.
     * @throws InterruptedException If waiting for the next Message is interrupted
     */
    protected final void yield() throws InterruptedException {
        nextResponseAvailable = true;
        responseAvailableOrFinished.set();
        responseRequested.await(timeout);
    }
    
    /**
     * Queue up a response to the user and yield the conversation until the next
     * incoming Message arrives.<br>
     * This is equal to {@code respond(response); yield();}
     * @param response Response to be sent before the conversation yields
     * @throws InterruptedException If waiting for the next Message is interrupted
     */
    protected final void yield(Object response) throws InterruptedException {
        respond(response);
        yield();
    }
    
    /**
     * Convenience method for {@code yield(); message();}
     * @return Incoming Message after yield
     * @throws InterruptedException If waiting for the next Message is interrupted
     */
    protected final Message yieldMessage() throws InterruptedException {
        yield();
        return message();
    }
    
    /**
     * Convenience method for {@code respond(response); yield(); message();}
     * @param response Response to be sent before the conversation yields
     * @return Incoming Message after yield
     * @throws InterruptedException If waiting for the next Message is interrupted
     */
    protected final Message yieldMessage(Object response) throws InterruptedException {
        respond(response);
        yield();
        return message();
    }
    
    /**
     * Stops the conversation completely.
     * This sets the {@code hasFinished} flag to {@code true}.
     * @throws InterruptedException Special instance of InterruptedException
     *         signalling the conversation to stop
     */
    protected final void stop() throws InterruptedException {
        hasFinished = true;
        throw new StopConversationException();
    }
    
    private Object bufferToResponseObject() {
        if (responseBuffer.isEmpty()) {
            // Empty buffer -> No result
            return null;
        } else if (responseBuffer.size() == 1) {
            // One element in buffer -> Get and remove first element
            return responseBuffer.remove(0);
        } else {
            // Many elements in buffer -> Copy elements, clear buffer
            Object result = new ArrayList(responseBuffer);
            responseBuffer.clear();
            return result;
        }
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
            responseAvailableOrFinished.await(0L);
        }
        catch (InterruptedException ignored) {
            hasFinished = true;
        }
        
        if (raisedException != null)
            throw new RuntimeException(
                    raisedException.getMessage(),
                    raisedException);
        
        return !hasFinished;
    }
    
    private void producerRoutine() {
        try {
            responseRequested.await(timeout);
            this.run();
        }
        catch (StopConversationException stop) {
            /* Don't do anything on artificial Stop */
        }
        catch (InterruptedException ignored) {
            /* Thrown by yield() called in run(), most likely due to Timeout */
            responseBuffer.add("[[TIMEOUT]]");
        }
        catch (Exception ex) {
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
        
        public synchronized void await(long timeout) throws InterruptedException {
            try {
                if (!isSet) wait(timeout);
            }
            finally {
                isSet = false;
            }
        }
    }
    
    private static class StopConversationException extends InterruptedException { }
}
