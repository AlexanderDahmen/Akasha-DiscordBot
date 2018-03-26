
package de.dahmen.alexander.akasha.core.entity;

import java.sql.Timestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/**
 *
 * @author Alexander
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeadlineTask extends Task {
    
    @NonNull private final java.sql.Timestamp deadline;
    private final int remindSeconds;

    public DeadlineTask(
            String name, String description,
            TaskStatus status, TaskPriority priority,
            Timestamp deadline, int remindSeconds)
    {
        super(name, status, priority, description);
        this.deadline = deadline;
        this.remindSeconds = remindSeconds;
    }

    @Override
    public Type getType() { return Type.DEADLINE; }
}
