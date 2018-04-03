
package de.dahmen.alexander.akasha.core.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Alexander
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    private long id;
    private long userId;
    private String name;
    private Type type;
    private TaskStatus status;
    private TaskPriority priority;
    private String description;
    private String reminderCron;
    private ZoneOffset timeZone;
    private LocalDateTime lastReminder;
    private LocalDateTime deadline;
    
    public OffsetDateTime getZonedLastReminder() {
        return (lastReminder == null) ? null : lastReminder.atOffset(timeZone);
    }
    
    public OffsetDateTime getZonedDeadline() {
        return (deadline == null) ? null : deadline.atOffset(timeZone);
    }
    
    public void setLastReminderInstant(Instant instant) {
        this.lastReminder = (instant == null) ? null :
                LocalDateTime.ofInstant(instant, timeZone);
    }
    
    public void setDeadlineInstant(Instant instant) {
        this.deadline = (instant == null) ? null :
                LocalDateTime.ofInstant(instant, timeZone);
    }
    
    public static enum Type {
        REPEAT,
        DEADLINE;
    }
}
