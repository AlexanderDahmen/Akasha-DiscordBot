
package de.dahmen.alexander.akasha.core.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 *
 * @author Alexander
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public abstract class Task {
    
    @NonNull protected final String name;
    @NonNull protected final TaskStatus status;
    @NonNull protected final TaskPriority priority;
    protected final String description;
    
    public abstract Type getType();
    
    public static enum Type {
        REPEAT,
        DEADLINE;
        
        public static Type fromOrdinal(int ordinal) {
            return values()[ordinal];
        }
    }
}
