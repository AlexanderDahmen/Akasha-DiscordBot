
package de.dahmen.alexander.akasha.conversations;

import de.dahmen.alexander.akasha.config.ConversationConfig;
import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.message.MessageMultiTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.MessageStrings;
import de.dahmen.alexander.akasha.core.conversation.message.MessageTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.StringUtil;
import de.dahmen.alexander.akasha.core.entity.RepeatTask;
import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.entity.TaskPriority;
import de.dahmen.alexander.akasha.core.entity.TaskStatus;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;
import de.dahmen.alexander.akasha.core.repository.RepositoryException;
import de.dahmen.alexander.akasha.core.service.ScheduleService;
import java.sql.Time;
import java.util.Date;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Alexander
 */
@Slf4j
@AllArgsConstructor
public class CreateTaskConversation implements Conversation {
    
    private final MessageTemplate errorMessage = new MessageTemplate("error/ErrorMessage");
    private final MessageMultiTemplate cronDialog = new MessageMultiTemplate("cron/CronDialog");
    
    private final MessageStrings strings = new MessageStrings("create_task/CreateTaskStrings");
    private final MessageMultiTemplate templates = new MessageMultiTemplate("create_task/CreateTask");
    private final MessageTemplate
            initial             = templates.get("Initial"),
            askType             = templates.get("AskType"),
            askOkay             = templates.get("AskOkay"),
            askName             = templates.get("AskName"),
            askDescription      = templates.get("AskDescription"),
            askPriority         = templates.get("AskPriority"),
            taskAlreadyExists   = templates.get("TaskAlreadyExists"),
            unknownType         = templates.get("UnknownType"),
            doneFailure         = templates.get("DoneFailure"),
            doneSuccess         = templates.get("DoneSuccess");
    
    
    private final Pattern
            answerYes       = Pattern.compile(strings.get("AnswerYes"), Pattern.CASE_INSENSITIVE),
            answerNo        = Pattern.compile(strings.get("AnswerNo"), Pattern.CASE_INSENSITIVE);
    
    private final ConversationConfig config;
    private final JdaUserRepository users;
    private final JdaTaskRepository tasks;
    private final ScheduleService schedule;
    
    @Override
    public boolean accept(Message message) {
        return message.getContentStripped().trim().toLowerCase().startsWith("create");
    }

    @Override
    public Instance instance() {
        return new CreateTaskInstance();
    }
    
    private class CreateTaskInstance extends DialogConversation {
        
        boolean success = false;
        String info = "";
        
        public CreateTaskInstance() {
            super(config.getTimeoutMilliseconds());
        }
        
        @Override
        protected void run() throws InterruptedException {
            User user = message().getAuthor();
            Task.Type type = getType();
            
            final Task task;
            try {
                switch (type) {
                    case REPEAT: task = createRepeatTask(user); break;
                    case DEADLINE: task = createDeadlineTask(user); break;
                    default: throw new AssertionError(type.name());
                }
                
                users.storeUser(user);
                tasks.storeTask(user, task);
            }
            catch (RepositoryException ex) {
                respond(errorMessage.set("error", ex.getMessage()));
                return;
            }
            
            success = true;
            info = String.format("%s", task.getName());
            respond(task);
        }
        
        @Override
        protected Object exitResponse() {
            return (success ? doneSuccess : doneFailure).set("info", info);
        }
        
        private Task.Type getType() throws InterruptedException {
            Task.Type result = null;
            
            Message first = message();
            String byCommand = first.getContentRaw()
                    .substring("create".length()).trim();
            
            if (!byCommand.isEmpty()) {
                result = parseTaskType(byCommand);
                if (result == null)
                    respond(unknownType.set("type", shorten(byCommand)));
            } else {
                respond(initial.set("type", strings.get("NewTask")));
            }
            
            while (result == null) {
                String typeMessage = yieldMessage(askType).getContentStripped();
                result = parseTaskType(typeMessage);
                if (result == null)
                    respond(unknownType.set("type", shorten(typeMessage)));
            }
            
            respond(initial.set("type", strings.get(result.name())));
            
            return result;
        }
        
        private Task createRepeatTask(User user) throws
                InterruptedException,
                RepositoryException
        {
            String taskName = untilOkay(askOkay, answerYes, answerNo, () -> {
                while (true) {
                    String name = yieldMessage(askName).getContentRaw();
                    if (tasks.taskNameExists(user, name)) {
                        respond(taskAlreadyExists.set("name", name));
                    } else {
                        return name;
                    }
                }
            });
            
            String description = untilOkay(askOkay, answerYes, answerNo,
                    () -> yieldMessage(askDescription).getContentRaw());
            
            TaskPriority taskPriority = TaskPriority.fromOrdinal(askInt(
                    askPriority, 0, TaskPriority.values().length));
            
            //TODO Input of start time
            Time startTime = new Time(new Date().getTime());
            
            String cron = cronDialog(
                    answerYes, answerNo, cronDialog,
                    schedule::isValidCronSchedule);
            
            return new RepeatTask(
                    taskName, description, TaskStatus.OPEN,
                    taskPriority, startTime, cron);
        }
        
        private Task createDeadlineTask(User user) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    private static Task.Type parseTaskType(String type) {
        try { return Task.Type.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }
    
    private static String shorten(String string) {
        return StringUtil.truncateEllipsis(StringUtil.firstLine(string), 24);
    }
}
