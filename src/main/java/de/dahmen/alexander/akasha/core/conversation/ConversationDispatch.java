package de.dahmen.alexander.akasha.core.conversation;

import de.dahmen.alexander.akasha.config.ConversationConfig;
import de.dahmen.alexander.akasha.conversations.CreateTaskConversation;
import de.dahmen.alexander.akasha.conversations.MentionReplyConversation;
import de.dahmen.alexander.akasha.conversations.DefaultFallbackConversation;
import de.dahmen.alexander.akasha.core.AkashaComponents;
import java.util.Arrays;
import java.util.List;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public interface ConversationDispatch {

    Initializer DEFAULT_INIT = (components) -> Arrays.asList(
            new MentionReplyConversation(),
            new CreateTaskConversation(components.config(ConversationConfig.class)),
            new DefaultFallbackConversation());
    
    FallbackConversation MISSING_FALLBACK = (msg) -> "ERROR :: MISSING FALLBACK CONVERSATION";
    
    /**
     * Dispatch a message to a Conversation
     * @param message Incoming message
     */
    void dispatch(Message message);
    
    /**
     * Interface for instantiating a List of Conversation implementations
     * from an {@code AkashaComponents} instance
     */
    @FunctionalInterface
    interface Initializer {
        List<Conversation> init(AkashaComponents components);
    }
}
