
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.config.ConversationConfig;
import de.dahmen.alexander.akasha.core.AkashaComponents;
import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.message.MessageMultiTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.MessageStrings;
import de.dahmen.alexander.akasha.core.conversation.message.MessageTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.StringUtil;
import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class CreateTaskConversation implements Conversation {
    
    private final MessageStrings strings = new MessageStrings("create_task/CreateTaskStrings");
    private final MessageMultiTemplate templates = new MessageMultiTemplate("create_task/CreateTask");
    private final MessageTemplate
            initial         = templates.get("Initial"),
            askType         = templates.get("AskType"),
            unknownType     = templates.get("UnknownType"),
            doneFailure     = templates.get("DoneFailure"),
            doneSuccess     = templates.get("DoneSuccess")
            
            ;
    
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
        String info = null;
        
        public CreateTaskInstance() {
            super(config);
        }
        
        @Override
        protected void run() throws InterruptedException {
            Task.Type type = getType();
            
            respond("UNFINISHED :: TYPE = " + type);
        }
        
        @Override
        protected Object exit() {
            return (success ? doneSuccess : doneFailure)
                    .set("info", info);
        }

        private Task.Type getType() throws InterruptedException {
            Task.Type result = null;
            
            Message first = message();
            String byCommand = first.getContentRaw()
                    .substring("create".length()).toLowerCase().trim();
            
            if (!byCommand.isEmpty()) {
                result = parseType(byCommand);
                if (result == null)
                    respond(unknownType.set("type", shorten(byCommand)));
            } else {
                respond(initial.set("type", strings.get("NewTask")));
            }
            
            while (result == null) {
                yield(askType);
                
                String typeMessage = message().getContentStripped();
                result = parseType(typeMessage);
                
                if (result == null)
                    respond(unknownType.set("type", shorten(typeMessage)));
            }
            
            respond(initial.set("type", strings.get(result.name())));
            
            return null;
        }
    }
    
    private static Task.Type parseType(String type) {
        try { return Task.Type.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }
    
    private static String shorten(String string) {
        return StringUtil.truncateEllipsis(StringUtil.firstLine(string), 24);
    }
}
