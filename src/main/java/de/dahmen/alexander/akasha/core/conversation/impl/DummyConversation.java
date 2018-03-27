
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class DummyConversation implements Conversation {
    @Override
    public boolean accept(Message message) {
        return message.getContentRaw().toLowerCase().contains("broccoli");
    }
    
    @Override
    public Instance instance() {
        return (msg) -> new MessageBuilder("I LOVE BROCCOLI!!!");
    }
}
