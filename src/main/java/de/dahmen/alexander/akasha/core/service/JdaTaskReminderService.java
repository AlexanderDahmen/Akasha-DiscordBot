package de.dahmen.alexander.akasha.core.service;

import de.dahmen.alexander.akasha.core.entity.Task;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Alexander
 */
public interface JdaTaskReminderService {
    
    void initialize() throws TaskReminderServiceException;
    
    public boolean taskNameExists(User user, String name);
    
    long addTask(User user, Task task) throws TaskReminderServiceException;
    
    void removeTask(User user, long taskId) throws TaskReminderServiceException;
    
    MessageEmbed formatReminder(User user, Task task);
    
    public static class TaskReminderServiceException extends Exception {
        public TaskReminderServiceException(String msg) { super(msg); }
    }
}
