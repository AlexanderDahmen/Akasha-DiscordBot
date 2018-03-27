
package de.dahmen.alexander.akasha.core.listener;

import de.dahmen.alexander.akasha.core.conversation.ConversationDispatch;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 *
 * @author Alexander
 */
@Slf4j
public class AkashaListener extends ListenerAdapter {

    private final ConversationDispatch conversations;
    
    public AkashaListener(ConversationDispatch handler) {
        this.conversations = handler;
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        SelfUser self = event.getJDA().getSelfUser();
        if (event.getMessage().isMentioned(self, Message.MentionType.ROLE, Message.MentionType.USER)) {
            conversations.dispatch(event.getMessage());
        }
    }
    
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        SelfUser self = event.getJDA().getSelfUser();
        if (event.getAuthor().getIdLong() != self.getIdLong()) {
            conversations.dispatch(event.getMessage());
        }
    }
}
