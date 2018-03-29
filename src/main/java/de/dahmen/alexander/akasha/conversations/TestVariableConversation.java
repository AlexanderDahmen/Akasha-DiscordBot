
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.message.MessageTemplate;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class TestVariableConversation implements Conversation {

    private final MessageTemplate test = new MessageTemplate("test/Hello");
    
    @Override
    public boolean accept(Message message) {
        return message.getContentStripped().toLowerCase().trim().startsWith("vartest");
    }

    @Override
    public Instance instance() {
        return ($) -> test
                .set("foo", "there")
                .set("bar", "Have a nice day. :)");
    }

}
