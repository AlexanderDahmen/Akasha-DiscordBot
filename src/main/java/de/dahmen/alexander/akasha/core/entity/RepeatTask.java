
package de.dahmen.alexander.akasha.core.entity;

import java.sql.Time;
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
public class RepeatTask extends Task {
    
    public static final int TYPE_IDENTIFIER = 0;
    
    @NonNull private final java.sql.Time startTime;
    private final int repeatSeconds;
    
    public RepeatTask(
            String name, String description,
            TaskStatus status, TaskPriority priority,
            Time startTime, int repeatSeconds)
    {
        super(name, status, priority, description);
        this.startTime = startTime;
        this.repeatSeconds = repeatSeconds;
    }

    @Override
    public Type getType() { return Type.REPEAT; } 
}
