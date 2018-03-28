
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.config.ConversationConfig;
import de.dahmen.alexander.akasha.core.conversation.Conversation;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class CreateTaskConversation implements Conversation {
    
    private final ConversationConfig config;

    public CreateTaskConversation(ConversationConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean accept(Message message) {
        return message.getContentStripped().trim().toLowerCase().startsWith("create");
    }

    @Override
    public Instance instance() {
        return new CreateTaskInstance();
    }
    
    private class CreateTaskInstance extends GeneratorConversationInstance {

        public CreateTaskInstance() {
            super(config);
        }
        
        @Override
        protected void run() throws InterruptedException {
            boolean done = false;
            boolean first = true;
            String content = "";
            
            while (!done) {
                Message message = message();
                content = message.getContentStripped();
                if (content.equalsIgnoreCase("stop")) {
                    done = true;
                } else {
                    if (first) {
                        first = false;
                        respond("Enter \"stop\" to cancel.");
                    }
                    yield("You said: " + content);
                }
            }
            
            respond("The final word: " + content);
        }
        
        @Override
        protected Object exit() {
            return "Goodbye!";
        }
    }
}
