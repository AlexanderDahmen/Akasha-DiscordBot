package de.dahmen.alexander.akasha.core.conversation;

import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public interface ConversationDispatch {
    
    /** FallbackConversation to be used if no other was supplied */
    FallbackConversation MISSING_FALLBACK = (msg) ->
            "ERROR :: MISSING FALLBACK CONVERSATION";
    
    /**
     * Dispatch a message to a Conversation
     * @param message Incoming message
     */
    void dispatch(Message message);
}
