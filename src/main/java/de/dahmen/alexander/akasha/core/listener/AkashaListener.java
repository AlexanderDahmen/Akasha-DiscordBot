
package de.dahmen.alexander.akasha.core.listener;

import de.dahmen.alexander.akasha.core.conversation.ConversationHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 *
 * @author Alexander
 */
@Slf4j
public class AkashaListener extends ListenerAdapter {

    private final ConversationHandler handler;
    
    public AkashaListener(ConversationHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        SelfUser self = event.getJDA().getSelfUser();
        if (event.getMessage().isMentioned(self, Message.MentionType.ROLE, Message.MentionType.USER)) {
            handler.dispatch(event.getMessage());
        }
    }
    
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        SelfUser self = event.getJDA().getSelfUser();
        if (event.getAuthor().getIdLong() != self.getIdLong()) {
            handler.dispatch(event.getMessage());
        }
    }
}
