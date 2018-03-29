
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.config.ConversationConfig;
import de.dahmen.alexander.akasha.core.AkashaComponents;
import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.entity.TaskStatus;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class CreateTaskConversation implements Conversation {
    
    private final ConversationConfig config;
    private final JdaUserRepository users;
    private final JdaTaskRepository tasks;
    
    public CreateTaskConversation(AkashaComponents components) {
        this.config = components.config(ConversationConfig.class);
        this.users = components.jdaUserRepository();
        this.tasks = components.jdaTaskRepository();
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
        
        boolean success = false;
        
        public CreateTaskInstance() {
            super(config);
        }
        
        @Override
        protected void run() throws InterruptedException {
            Task.Type type = getType();
            
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
            return "done. ";
        }

        private Task.Type getType() {
            return null;
        }
    }
}
