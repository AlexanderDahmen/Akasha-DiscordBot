
package de.dahmen.alexander.akasha.conversations;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.FieldExpressionFactory;
import de.dahmen.alexander.akasha.config.ConversationConfig;
import de.dahmen.alexander.akasha.core.conversation.Conversation;
import de.dahmen.alexander.akasha.core.conversation.message.MessageMultiTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.MessageStrings;
import de.dahmen.alexander.akasha.core.conversation.message.MessageTemplate;
import de.dahmen.alexander.akasha.core.conversation.message.StringUtil;
import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.entity.TaskPriority;
import de.dahmen.alexander.akasha.core.entity.TaskStatus;
import de.dahmen.alexander.akasha.core.repository.RepositoryException;
import de.dahmen.alexander.akasha.core.service.CronService;
import de.dahmen.alexander.akasha.core.service.JdaTaskReminderService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.StringJoiner;
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
    private final MessageStrings createTaskStrings = new MessageStrings("create_task/CreateTaskStrings");
    
    private final MessageMultiTemplate cronDialog = new MessageMultiTemplate("create_task/CronDialog");
    private final MessageMultiTemplate reminderDialog = new MessageMultiTemplate("create_task/ReminderDialog");
    private final MessageMultiTemplate templates = new MessageMultiTemplate("create_task/CreateTask");
    private final MessageTemplate
            initial             = templates.get("Initial"),
            askType             = templates.get("AskType"),
            askOkay             = templates.get("AskOkay"),
            askName             = templates.get("AskName"),
            askDescription      = templates.get("AskDescription"),
            askPriority         = templates.get("AskPriority"),
            askTimezone         = templates.get("AskTimezone"),
            taskAlreadyExists   = templates.get("TaskAlreadyExists"),
            unknownType         = templates.get("UnknownType"),
            doneFailure         = templates.get("DoneFailure"),
            doneSuccess         = templates.get("DoneSuccess");
    
    private final Pattern
            answerYes       = Pattern.compile(createTaskStrings.get("AnswerYes"), Pattern.CASE_INSENSITIVE),
            answerNo        = Pattern.compile(createTaskStrings.get("AnswerNo"), Pattern.CASE_INSENSITIVE);
    
    private final ConversationConfig config;
    private final JdaTaskReminderService service;
    private final CronService cron;
    
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
                
                task.setUserId(user.getIdLong());
                service.addTask(user, task);
            }
            catch (JdaTaskReminderService.TaskReminderServiceException ex) {
                respond(errorMessage.set("error", ex.getMessage()));
                return;
            }
            
            success = true;
            info = task.getName();
            
            respond(service.formatReminder(user, task));
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
                respond(initial.set("type", createTaskStrings.get("NewTask")));
            }
            
            while (result == null) {
                String typeMessage = yieldMessage(askType).getContentStripped();
                result = parseTaskType(typeMessage);
                if (result == null)
                    respond(unknownType.set("type", shorten(typeMessage)));
            }
            
            respond(initial.set("type", createTaskStrings.get(result.name())));
            
            return result;
        }
        
        private Task createRepeatTask(User user) throws
                InterruptedException,
                RepositoryException
        {
            String taskName = untilOkay(askOkay, answerYes, answerNo, () -> {
                while (true) {
                    String name = yieldMessage(askName).getContentRaw();
                    if (service.taskNameExists(user, name)) {
                        respond(taskAlreadyExists.set("name", name));
                    } else {
                        return name;
                    }
                }
            });
            
            String description = untilOkay(askOkay, answerYes, answerNo, () -> {
                return yieldMessage(askDescription).getContentRaw();
            });
            
            int taskPriority = askInt(askPriority,
                    0, TaskPriority.values().length - 1);
            
            String reminderCron = repeatTaskCronDialog();
            
            ZoneOffset timezone = askTimezone();
            OffsetDateTime createdTime = OffsetDateTime.now(timezone);
            
            Task task = new Task();
            task.setName(taskName);
            task.setDescription(description);
            task.setType(Task.Type.REPEAT);
            task.setStatus(TaskStatus.OPEN);
            task.setPriority(TaskPriority.values()[taskPriority]);
            task.setTimeZone(timezone);
            task.setLastReminder(createdTime.toLocalDateTime());
            task.setReminderCron(reminderCron);
            return task;
        }
        
        private Task createDeadlineTask(User user) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        private String repeatTaskCronDialog() throws InterruptedException {
            MessageTemplate
                    howOften = reminderDialog.get("HowOften"),
                    askWeekday = reminderDialog.get("AskWeekday"),
                    askMonthday = reminderDialog.get("AskMonthday"),
                    askDate = reminderDialog.get("AskDate"),
                    askTime = reminderDialog.get("AskTime");
            
            int choice = askInt(howOften, 0, 4);
            if (choice == 0)
                return cronDialog();
            
            CronBuilder cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
            cron.withSecond(FieldExpressionFactory.on(0));
            cron.withYear(FieldExpression.always());
            
            final int day;
            final TemporalAccessor date, time;
            
            switch (choice) {
                case 1: // Hourly
                    cron.withMinute(FieldExpressionFactory.on(0));
                    cron.withHour(FieldExpressionFactory.every(1));
                    cron.withDoM(FieldExpressionFactory.every(1));
                    cron.withMonth(FieldExpression.always());
                    cron.withDoW(FieldExpression.always());
                case 2: // Daily
                    time = askTime(askTime);
                    cron.withMinute(FieldExpressionFactory.on(time.get(ChronoField.MINUTE_OF_HOUR)));
                    cron.withHour(FieldExpressionFactory.on(time.get(ChronoField.HOUR_OF_DAY)));
                    cron.withDoM(FieldExpressionFactory.every(1));
                    cron.withMonth(FieldExpression.always());
                    cron.withDoW(FieldExpression.questionMark());
                    break;
                case 3: // Weekly
                    time = askTime(askTime);
                    cron.withMinute(FieldExpressionFactory.on(time.get(ChronoField.MINUTE_OF_HOUR)));
                    cron.withHour(FieldExpressionFactory.on(time.get(ChronoField.HOUR_OF_DAY)));
                    day = askInt(askWeekday, 1, 7);
                    cron.withDoM(FieldExpression.questionMark());
                    cron.withMonth(FieldExpression.always());
                    cron.withDoW(FieldExpressionFactory.on(day));
                    break;
                case 4: // Monthly
                    time = askTime(askTime);
                    cron.withMinute(FieldExpressionFactory.on(time.get(ChronoField.MINUTE_OF_HOUR)));
                    cron.withHour(FieldExpressionFactory.on(time.get(ChronoField.HOUR_OF_DAY)));
                    day = askInt(askMonthday, 1, 31);
                    cron.withDoM(FieldExpressionFactory.on(day));
                    cron.withMonth(FieldExpressionFactory.every(1));
                    cron.withDoW(FieldExpression.questionMark());
                    break;
                case 5: // Yearly
                    time = askTime(askTime);
                    cron.withMinute(FieldExpressionFactory.on(time.get(ChronoField.MINUTE_OF_HOUR)));
                    cron.withHour(FieldExpressionFactory.on(time.get(ChronoField.HOUR_OF_DAY)));
                    date = askDate(askDate);
                    cron.withDoM(FieldExpressionFactory.on(date.get(ChronoField.DAY_OF_MONTH)));
                    cron.withMonth(FieldExpressionFactory.on(date.get(ChronoField.MONTH_OF_YEAR)));
                    cron.withDoW(FieldExpression.questionMark());
                    break;
                default:
                    throw new AssertionError("ReminderDialog choice: " + choice);
            }
            
            return cron.instance().asString();
        }
        
        private String cronDialog() throws InterruptedException {
            MessageTemplate
                    canYouDoCron = cronDialog.get("CanYouDoCron"),
                    askCron = cronDialog.get("AskCron"),
                    invalidCron = cronDialog.get("InvalidCron"),
                    cantCronThis = cronDialog.get("CantCronThis"),
                    askWeekOrMonthDay = cronDialog.get("AskWeekOrMonthDay"),
                    askDayOfMonth = cronDialog.get("AskDayOfMonth"),
                    askDayOfWeek = cronDialog.get("AskDayOfWeek"),
                    askMonth = cronDialog.get("AskMonth"),
                    askMinutes = cronDialog.get("AskMinutes"),
                    askHours = cronDialog.get("AskHours");

            // Do direct input if possible
            boolean directInput = askYesNo(canYouDoCron, answerYes, answerNo);
            if (!directInput)
                respond(cantCronThis);
            
            while (true) {
                String resultCron;

                if (directInput) {
                    resultCron = yieldMessage(askCron).getContentRaw().trim();
                } else {
                    //String seconds = withoutWhitespace(yieldMessage(askSeconds));
                    String minutes = withoutWhitespace(yieldMessage(askMinutes));
                    String hours = withoutWhitespace(yieldMessage(askHours));
                    String month = withoutWhitespace(yieldMessage(askMonth));
                    
                    int dayOfChoice = askInt(askWeekOrMonthDay, 1, 2);
                    String dayOfMonth = null;
                    String dayOfWeek = null;
                    switch (dayOfChoice) {
                        case 1:
                            dayOfWeek = withoutWhitespace(yieldMessage(askDayOfWeek));
                            dayOfMonth = "?";
                            break;
                        case 2:
                            dayOfMonth = withoutWhitespace(yieldMessage(askDayOfMonth));
                            dayOfWeek = "?";
                            break;
                        default:
                            respond("ERROR dayOfChoice = " + dayOfChoice);
                            stop();
                            break;
                    }
                    
                    resultCron = new StringJoiner(" ")
                            .add("0")           // Seconds
                            .add(minutes)       // Minutes
                            .add(hours)         // Hours
                            .add(dayOfMonth)    // DoM
                            .add(month)         // Month
                            .add(dayOfWeek)     // DoW
                            .add("*")           // Year
                            .toString();
                }
                
                boolean okay = cron.validate(resultCron);
                if (okay) {
                    return resultCron;
                } else {
                    boolean tryAgain = askYesNo(
                            invalidCron.set("cron", resultCron),
                            answerYes, answerNo);
                    
                    if (!tryAgain) {
                        stop();
                    }
                }
            }
        }
        
        private ZoneOffset askTimezone() throws InterruptedException {
            LocalDateTime now = LocalDateTime.now();
            int offset = askInt(
                    askTimezone.set("now", DATETIME_FORMAT.format(now)),
                    -18, +18);
            return ZoneOffset.ofHours(offset);
        }
    }
    
    private static Task.Type parseTaskType(String type) {
        try { return Task.Type.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }
    
    private static String shorten(String string) {
        return StringUtil.truncateEllipsis(StringUtil.firstLine(string), 24);
    }
    
    private static String withoutWhitespace(Message message) {
        return message.getContentRaw().replaceAll("\\s", "");
    }
}
