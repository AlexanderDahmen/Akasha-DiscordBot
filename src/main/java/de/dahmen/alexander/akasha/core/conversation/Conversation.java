package de.dahmen.alexander.akasha.core.conversation;

import java.util.function.Function;
import net.dv8tion.jda.core.entities.Message;

/**
 * Specification for a type of Discord conversation.<br>
 * A message can be tested against the conversation with
 * {@link #accept(Message) accept(Message)}, and an instance of a conversation
 * can be instantiated with {@link #instance() instance()}.
 * 
 * @author Alexander
 */
public interface Conversation {
    
    interface Instance extends Function<Message, Object> {
        /**
         * Apply a conversation response to an incoming message
         * @param message Message being sent into the conversation
         * @return A reply to the conversation, or null if no reply should be sent.
         *  Special handling is applied to return types {@code Message}, {@code MessageEmbed}
         *  and {@code Queue<Message>}; all other types are sent as {@code String.valueOf(x)}
         */
        @Override
        Object apply(Message message);
        
        /**
         * Check if the conversation is finished.<br>
         * True by default because lambda implementations are stateless and may
         * only reply once per conversation
         * @return True if the conversation is over and should stop, False if it should keep going
         */
        default boolean isFinished() { return true; }
    }
    
    /**
     * Check if the incoming message can be accepted by this
     * implementation of conversation.
     * @param message Incoming message to be handled with a conversation
     * @return True if an Instance created with {@link #instance() instance()}
     *      can handle this conversation.
     */
    boolean accept(Message message);
    
    /**
     * Return an instance of this conversation
     * @return Function mapping an incoming Message to an outgoing Message (or Messages)
     */
    Instance instance();
}
