
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class HelloConversation implements Conversation {
    
    @Override
    public boolean accept(Message message) {
        String content = message.getContentStripped().toLowerCase().trim();
        return (content.startsWith("hi") ||
                content.startsWith("hello"));
    }

    @Override
    public Instance instance() {
        return (msg) -> "Hi :)";
    }
}
