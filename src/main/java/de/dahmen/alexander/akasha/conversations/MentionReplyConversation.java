
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.message.MessageTemplate;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class MentionReplyConversation implements Conversation {
    
    private final MessageTemplate replyTemplate;

    public MentionReplyConversation() {
        this.replyTemplate = new MessageTemplate("mention/PublicMentionReply");
    }
    
    @Override
    public boolean accept(Message message) {
        return (!message.isFromType(ChannelType.PRIVATE)) &&
                (message.isMentioned(
                        message.getJDA().getSelfUser(),
                        Message.MentionType.ROLE,
                        Message.MentionType.USER));
    }
    
    @Override
    public Instance instance() {
        return (msg) -> replyTemplate.set("user", msg.getAuthor().getAsMention());
    }
}
