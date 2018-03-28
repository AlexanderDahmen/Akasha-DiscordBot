
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class CreateTaskConversation implements Conversation {

    @Override
    public boolean accept(Message message) {
        return message.getContentStripped().trim().toLowerCase().startsWith("create");
    }

    @Override
    public Instance instance() {
        return new Impl();
    }
    
    private class Impl extends GeneratorConversationInstance {
        @Override
        protected void run() throws InterruptedException {
            boolean done = false;
            while (!done) {
                Message message = message();
                String content = message.getContentStripped();
                if (content.equalsIgnoreCase("stop"))
                    done = true;
                else
                    yield("You said: " + content);
            }
        }
    }
}
