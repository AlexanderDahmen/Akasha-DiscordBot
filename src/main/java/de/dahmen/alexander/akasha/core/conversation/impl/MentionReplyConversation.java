
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.util.MessageTemplate;
import de.dahmen.alexander.akasha.util.MapBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class MentionReplyConversation implements Conversation {
    
    private final MessageTemplate replyTemplate;

    public MentionReplyConversation() {
        this.replyTemplate = new MessageTemplate("PublicMentionReply");
    }
    
    @Override
    public boolean accept(Message message) {
        return (!message.isFromType(ChannelType.PRIVATE)) &&
                (message.isMentioned(
                        message.getJDA().getSelfUser(),
                        Message.MentionType.ROLE, Message.MentionType.USER));
    }
    
    @Override
    public Instance instance() {
        return (msg) -> replyTemplate.toString(new MapBuilder<String, Object>()
                .put("user", msg.getAuthor().getAsMention())
                .build());
    }
}
