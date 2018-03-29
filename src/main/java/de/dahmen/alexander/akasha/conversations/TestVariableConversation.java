
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.message.MessageMultiTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.MessageTemplate;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class TestVariableConversation implements Conversation {

    private final MessageTemplate test = new MessageTemplate("test/Hello");
    private final MessageMultiTemplate multi = new MessageMultiTemplate("test/Multi");
    
    @Override
    public boolean accept(Message message) {
        return message.getContentStripped().toLowerCase().trim().startsWith("vartest");
    }

    @Override
    public Instance instance() {
        /*return ($) -> test
                .set("foo", "there")
                .set("bar", "Have a nice day. :)");*/
        return (message) -> {
            String[] split = message.getContentRaw().split("\\s+", 2);
            String key = (split.length < 2) ? null : split[1];
            return multi.get(key).set("t", "Template").set("temmie", "hOi");
        };
    }

}
