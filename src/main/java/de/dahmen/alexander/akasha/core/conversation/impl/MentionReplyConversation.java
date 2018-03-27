
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class MentionReplyConversation implements Conversation {
    @Override
    public boolean accept(Message message) {
        return (!message.isFromType(ChannelType.PRIVATE)) &&
                (message.isMentioned(
                        message.getJDA().getSelfUser(),
                        Message.MentionType.ROLE, Message.MentionType.USER));
    }
    
    @Override
    public Instance instance() {
        /*return (in) -> {
            return new MessageBuilder()
                    .appendFormat("Hello, %s!%n", in.getAuthor())
                    .append("I am Akasha-Bot. You can work with me by sending me a DM.")
                    .buildAll(MessageBuilder.SplitPolicy.NEWLINE);
        };*/
        return (in) -> new MessageBuilder()
                .appendFormat("Hello, %s!%n", in.getAuthor())
                .append("I am Akasha-Bot. You can work with me by sending me a DM.")
                .buildAll(MessageBuilder.SplitPolicy.NEWLINE);
    }
}
