
package de.dahmen.alexander.akasha.core.conversation;

import de.dahmen.alexander.akasha.core.AkashaComponents;
import de.dahmen.alexander.akasha.core.conversation.impl.DefaultFallbackConversation;
import de.dahmen.alexander.akasha.core.conversation.impl.DummyConversation;
import de.dahmen.alexander.akasha.core.conversation.impl.MentionReplyConversation;
import de.dahmen.alexander.akasha.core.conversation.impl.TestConversation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
@Slf4j
public class ConversationHandler {
    
    public static final Initializer DEFAULT_INIT = (components) -> Arrays.asList(
            new MentionReplyConversation(),
            new DummyConversation(),
            new TestConversation(),
            new DefaultFallbackConversation());
    
    private static final FallbackConversation MISSING_FALLBACK = (msg) -> new MessageBuilder()
            .append("ERROR :: MISSING FALLBACK CONVERSATION").build();
    
    private final List<Conversation> conversations;
    private final Conversation fallbackConversation;
    private final ConcurrentMap<Long, Conversation.Instance> activeConversations;

    public ConversationHandler(AkashaComponents components) {
        this(components, DEFAULT_INIT);
    }
    
    public ConversationHandler(AkashaComponents components, Initializer init) {
        this(init.init(components));
    }
    
    public ConversationHandler(List<Conversation> conversations) {
        this.conversations = Collections.unmodifiableList(conversations.stream()
                .filter((c) -> !(c instanceof FallbackConversation))
                .collect(Collectors.toList()));
        
        this.fallbackConversation = conversations.stream()
                .filter((c) -> (c instanceof FallbackConversation))
                .findFirst()
                .orElse(MISSING_FALLBACK);
        
        this.activeConversations = new ConcurrentHashMap<>();
    }
    
    public void dispatch(Message message) {
        // Get the message author's ID
        final long author = message.getAuthor().getIdLong();
        
        // Continue an active conversation, or create a new conversation instance
        final Conversation.Instance active = activeConversations.get(author);
        final Conversation.Instance instance;
        if (active == null) {
            instance = instantiateConversation(message);
            // Add to active conversations after instantiation
            activeConversations.put(author, instance);
        } else {
            instance = active;
        }
        
        // Apply conversation and send back response (if present)
        Optional.ofNullable(instance.apply(message))
                .ifPresent((response) -> message.getChannel()
                        .sendMessage(response)
                        .queue(this::onMessageSendSuccess, this::onMessageSendFailed));
        
        // If conversation 
        if (instance.isFinished())
            activeConversations.remove(author);
    }
    
    private Conversation.Instance instantiateConversation(Message message) {
        return conversations.stream()
                .filter((c) -> c.accept(message))
                .findFirst()
                .orElse(fallbackConversation)
                .instance();
    }
    
    private void onMessageSendSuccess(Message sent) {
        /* Do nothing */
    }
    
    private void onMessageSendFailed(Throwable error) {
        log.error("Error sending Message: " + error.getMessage(), error);
    }
    
    /**
     * Interface for instantiating a List of Conversation implementations
     * from an {@code AkashaComponents} instance
     */
    @FunctionalInterface
    public static interface Initializer {
        List<Conversation> init(AkashaComponents components);
    }
}
