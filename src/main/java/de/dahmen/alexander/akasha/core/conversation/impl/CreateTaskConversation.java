
package de.dahmen.alexander.akasha.core.conversation.impl;

import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.util.MessageResource;
import de.dahmen.alexander.akasha.core.conversation.util.MessageStrings;
import de.dahmen.alexander.akasha.core.conversation.util.MessageTemplate;
import de.dahmen.alexander.akasha.core.conversation.util.StringUtil;
import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Alexander
 */
public class CreateTaskConversation implements Conversation {
    
    private static final Pattern ACCEPT = Pattern.compile("\\s*(create).*");
    private static final Pattern DETECT_TYPE = Pattern.compile("\\s*(create)\\s*(.*)");
    
    private final JdaUserRepository users;
    private final JdaTaskRepository tasks;
    
    protected final MessageStrings
            createTaskStrings;
    
    protected final MessageTemplate
            errorMT,
            initialMT,
            askTypeMT,
            initialAskTypeMT,
            unknownTaskTypeMT;
    
    
    public CreateTaskConversation(JdaUserRepository users, JdaTaskRepository tasks) {
        this.users = users;
        this.tasks = tasks;
        this.errorMT = new MessageTemplate("error/ErrorMessage");
        this.initialMT = new MessageTemplate("create_task/Initial");
        this.askTypeMT = new MessageTemplate("create_task/AskType");
        this.initialAskTypeMT = new MessageTemplate(initialMT, askTypeMT);
        this.unknownTaskTypeMT = new MessageTemplate("create_task/UnknownTaskType", askTypeMT);
        this.createTaskStrings = new MessageStrings("create_task/CreateTaskStrings");
    }
    
    @Override
    public boolean accept(Message message) {
        // Message starts with "create"
        String content = message.getContentRaw().toLowerCase();
        return ACCEPT.matcher(content).matches();
    }
    
    @Override
    public Instance instance() {
        return new Impl();
    }
    
    private class Impl implements Conversation.Instance {
        
        State state = State.INIT;
        boolean finished = false;
        
        private Task.Type taskType = null;
        
        @Override
        public Object apply(Message message) {
            switch (state) {
                case INIT: return init(message);
                case ASK_FOR_TYPE: return parseTaskType(message);
                case TODO: return unfinished();
                default: throw new AssertionError(state.name());
            }
        }
        
        @Override
        public boolean isFinished() {
            return finished;
        }

        private Object init(Message message) {
            String command = message.getContentRaw().toLowerCase();
            Matcher regex = DETECT_TYPE.matcher(command);
            regex.find();
            String typeString = regex.group(2);
            
            Object response;
            
            if (typeString == null || typeString.isEmpty()) {
                // No type supplied -> Ask for it
                response = initialAskTypeMT.set("type", createTaskStrings.get("NewTask"));
                state = State.ASK_FOR_TYPE;
            } else {
                // Type supplied -> Parse it
                taskType = typeFromString(typeString);
                if (taskType == null) {
                    state = State.ASK_FOR_TYPE;
                    return unknownTaskTypeMT.set("type", shorten(typeString));
                }
                response = initialMT.set("type", createTaskStrings.get(taskType.name()));
                state = State.TODO;
            }
            
            return response;
        }

        private Object parseTaskType(Message message) {
            String typeString = message.getContentRaw();
            taskType = typeFromString(typeString.toLowerCase());
            if (taskType == null) {
                return unknownTaskTypeMT.set("type", shorten(typeString));
            }
            
            state = State.TODO;
            return new Object[]{"TYPE = " + taskType, unfinished()};
        }

        private Object unfinished() {
            finished = true;
            return "THIS FUNCTION IS UNFINISHED.";
        }
    }
    
    private static enum State {
        INIT,
        ASK_FOR_TYPE,
        TODO,
    }
    
    private static Task.Type typeFromString(String string) {
        switch (string.trim()) {
            case "deadline": return Task.Type.DEADLINE;
            case "repeat": return Task.Type.REPEAT;
            default: return null;
        }
    }
    
    private static String shorten(String string) {
        return StringUtil.truncateEllipsis(StringUtil.firstLine(string), 15);
    }
}
