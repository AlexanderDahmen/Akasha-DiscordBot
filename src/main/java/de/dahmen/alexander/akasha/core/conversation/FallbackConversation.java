package de.dahmen.alexander.akasha.core.conversation;

import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public interface FallbackConversation extends Conversation {
    /**
     * Fallback for a conversation that never accepts messages on it's own
     * @param message Ignored
     * @return Always false
     */
    @Override
    default boolean accept(Message message) {
        return false;
    }
    
    /**
     * Fallback conversation instance:
     * Will return a fallback Message and finish the conversation
     * @return Fallback conversation instance
     */
    @Override
    default Instance instance() {
        return this::fallback;
    }
    
    Object fallback(Message msg);
}
