
package de.dahmen.alexander.akasha.service.scheduled;

import de.dahmen.alexander.akasha.config.ReminderConfig;
import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;
import de.dahmen.alexander.akasha.core.repository.RepositoryException;
import de.dahmen.alexander.akasha.core.service.CronService;
import de.dahmen.alexander.akasha.core.service.JdaTaskReminderService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Alexander
 */
@Slf4j
public class ScheduledJdaTaskReminderService implements JdaTaskReminderService, AutoCloseable {
    
    private final ReminderConfig config;
    private final JdaTaskRepository tasks;
    private final JdaUserRepository users;
    private final CronService cron;
    private final ScheduledExecutorService scheduler;
    
    private boolean initialized = false;
    
    public ScheduledJdaTaskReminderService(
            ReminderConfig config,
            JdaTaskRepository tasks,
            JdaUserRepository users,
            CronService cron)
    {
        this.tasks = tasks;
        this.users = users;
        this.config = config;
        this.cron = cron;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    @Override
    public void initialize() throws TaskReminderServiceException {
        if (initialized)
            throw new IllegalStateException("Already initialized");
        
        log.info("Initializing JDA Task Reminder Service");
        
        scheduler.scheduleWithFixedDelay(
                this::runReminderService,
                0L, config.getUpdateTime(),
                config.getUpdateTimeUnit());
        
        initialized = true;
    }
    
    @Override
    public boolean taskNameExists(User user, String name) {
        return tasks.taskNameExists(user, name);
    }
    
    @Override
    public long addTask(User user, Task task) throws TaskReminderServiceException {
        users.storeUser(user);
        return tasks.storeTask(task);
    }
    
    @Override
    public void removeTask(User user, long taskId) throws TaskReminderServiceException {
        // Remove task, throw Exception if task doesn't exist
        if (!tasks.deleteTask(taskId))
            throw new TaskReminderServiceException("Task doesn't exist: " + taskId);
        
        // Delete users without tasks
        if (!tasks.hasTasks(user))
            users.deleteUser(user);
    }

    @Override
    public MessageEmbed formatReminder(User user, Task task) {
        String avatarUrl = (user.getAvatarUrl() == null) ?
                user.getDefaultAvatarUrl() :
                user.getAvatarUrl();
        
        String reminder;
        try {
            reminder = cron.describe(task.getReminderCron());
        }
        catch (CronService.CronServiceException ex) {
            reminder = "INVALID CRON :: "
                    + task.getReminderCron() + " :: "
                    + ex.getMessage();
        }
        
        return new EmbedBuilder()
                .setTitle(task.getName())
                .setAuthor(user.getName())
                .setImage(avatarUrl)
                .setDescription(task.getDescription())
                .addField("Reminder", reminder, true)
                .build();
    }
    
    @Override
    public void close() throws Exception {
        log.info("Shutting down JDA Task Reminder Service");
        scheduler.shutdown();
    }
    
    private void runReminderService() {
        try {
            for (User user : users.getUsers()) {
                for (Task task : tasks.getTasks(user)) {
                    safeRemindTask(user, task);
                }
            }
        }
        catch (RepositoryException ex) {
            log.error("ReminderService Repository Exception: " + ex.getMessage(), ex);
        }
    }
    
    @SuppressWarnings("UseSpecificCatch")
    private void safeRemindTask(User user, Task task) {
        try {
            remindTask(user, task);
        }
        catch (CronService.CronServiceException |
                TaskReminderServiceException ex)
        {
            log.error("JDA Reminder Exception: " + ex.getMessage(), ex);
        }
        catch (Exception ex) {
            log.error("Unexpected JDA Reminder Exception: " + ex.getMessage(), ex);
        }
    }
    
    private void remindTask(User user, Task task) throws CronService.CronServiceException, TaskReminderServiceException {
        // Calculate last cron execution time, get last actual execution from DB
        String reminderCron = task.getReminderCron();
        ZonedDateTime now = ZonedDateTime.now(task.getTimeZone());
        OffsetDateTime lastReminded = task.getZonedLastReminder();
        OffsetDateTime cronLastExec = cron.lastExecution(now, reminderCron).toOffsetDateTime();
        
        // Rules for triggering a reminder:
        /*
        -- Don't Trigger --
        CRON_LAST < LAST_REMINDED < NOW < CRON_NEXT
        
        -- Do Trigger --
        LAST_REMINDED < CRON_LAST < NOW < CRON_NEXT
        */
        
        if (lastReminded.isBefore(cronLastExec)) {
            // Open channel to user, log error if not connection could be established
            PrivateChannel channel = user.openPrivateChannel().complete();
            if (channel == null) {
                log.error("Could not remind User (No PrivateChannel): " + user);
                return;
            }
            
            // Send task reminder to user
            channel.sendMessage(formatReminder(user, task)).queue();
            
            // Update when the task was last reminded
            task.setLastReminderInstant(Instant.now());
            tasks.updateTask(task);
        }
    }
}
