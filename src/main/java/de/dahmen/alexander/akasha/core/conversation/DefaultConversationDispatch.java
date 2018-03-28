
package de.dahmen.alexander.akasha.core.conversation;

import de.dahmen.alexander.akasha.core.AkashaComponents;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

/**
 *
 * @author Alexander
 */
@Slf4j
public class DefaultConversationDispatch implements ConversationDispatch {
    
    private final List<Conversation> conversations;
    private final Conversation fallbackConversation;
    private final ConcurrentMap<Long, Conversation.Instance> activeConversations;
    
    public DefaultConversationDispatch(AkashaComponents components) {
        this(components, DEFAULT_INIT);
    }
    
    public DefaultConversationDispatch(AkashaComponents components, Initializer init) {
        this(init.init(components));
    }
    
    public DefaultConversationDispatch(List<Conversation> conversations) {
        this.conversations = Collections.unmodifiableList(conversations.stream()
                .filter((c) -> !(c instanceof FallbackConversation))
                .collect(Collectors.toList()));
        
        this.fallbackConversation = conversations.stream()
                .filter((c) -> (c instanceof FallbackConversation))
                .findFirst()
                .orElse(MISSING_FALLBACK);
        
        this.activeConversations = new ConcurrentHashMap<>();
    }
    
    @Override
    public void dispatch(Message message) {
        // Get the Message AuthorID and Channel
        final long author = message.getAuthor().getIdLong();
        final MessageChannel channel = message.getChannel();
        
        // Send a typing event
        channel.sendTyping().queue();
        
        // Continue an active conversation, or create a new conversation instance
        final Conversation.Instance active = activeConversations.get(author);
        final Conversation.Instance instance;
        
        if (active == null) {
            // Instantiate conversation and add to active conversations
            instance = instantiateConversation(message);
            activeConversations.put(author, instance);
        } else {
            // If an active conversation exists, continue using it
            instance = active;
        }
        
        // Apply conversation and send back response (if present)
        Optional.ofNullable(instance.apply(message))
                .ifPresent((response) -> sendResponse(channel, response));
        
        // If conversation was in active conversations map and is now finished,
        // remove it from the active conservations
        if (instance.isFinished()) {
            activeConversations.remove(author);
            safeClose(instance);
        }
    }
    
    private Conversation.Instance instantiateConversation(Message message) {
        return conversations.stream()
                .filter((c) -> c.accept(message))
                .findFirst()
                .orElse(fallbackConversation)
                .instance();
    }
    
    private void sendResponse(MessageChannel channel, Object response) {
        if (response instanceof Iterable) {
            for (Object object : (Iterable) response) {
                sendSingleResponse(channel, object);
            }
        } else if (response.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(response); i++) {
                sendSingleResponse(channel, Array.get(response, i));
            }
        } else {
            sendSingleResponse(channel, response);
        }
    }
    
    private void sendSingleResponse(MessageChannel channel, Object response) {
        final MessageAction action;
        
        if (response instanceof MessageBuilder) {
            MessageBuilder mb = (MessageBuilder) response;
            if (mb.length() > Message.MAX_CONTENT_LENGTH) {
                Queue<Message> parts = mb.buildAll();
                sendResponse(channel, parts);
                return;
            } else {
                action = mb.sendTo(channel);
            }
        }
        else if (response instanceof EmbedBuilder) {
            action = new MessageBuilder((EmbedBuilder) response).sendTo(channel);
        }
        else if (response instanceof Message) {
            action = channel.sendMessage((Message) response);
        }
        else if (response instanceof MessageEmbed) {
            action = channel.sendMessage((MessageEmbed) response);
        }
        else if (response instanceof CharSequence) {
            action = channel.sendMessage((CharSequence) response);
        }
        else {
            action = channel.sendMessage(String.valueOf(response));
        }
        
        action.queue(this::onMessageSendSuccess, this::onMessageSendFailed);
    }
    
    private void onMessageSendSuccess(Message sent) {
        /* Do nothing */
    }
    
    private void onMessageSendFailed(Throwable error) {
        log.error("Error sending Message: " + error.getMessage(), error);
    }

    private void safeClose(Conversation.Instance instance) {
        if (instance instanceof AutoCloseable) {
            try {
                ((AutoCloseable) instance).close();
            }
            catch (Exception ex) {
                log.warn(
                        "Could not close ConversationInstance: "
                                + instance.getClass().getName(),
                        ex);
            }
        }
    }
}
