package de.dahmen.alexander.akasha.core.conversation;

import de.dahmen.alexander.akasha.core.AkashaComponents;
import de.dahmen.alexander.akasha.core.conversation.impl.*;
import java.util.Arrays;
import java.util.List;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public interface ConversationDispatch {

    Initializer DEFAULT_INIT = (components) -> Arrays.asList(
            new MentionReplyConversation(),
            new DummyConversation(),
            new TestConversation(),
            new DefaultFallbackConversation());
    
    FallbackConversation MISSING_FALLBACK = (msg) ->
            new MessageBuilder("ERROR :: MISSING FALLBACK CONVERSATION").build();
    
    
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
