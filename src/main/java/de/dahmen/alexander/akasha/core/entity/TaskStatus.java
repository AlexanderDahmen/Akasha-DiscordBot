package de.dahmen.alexander.akasha.core.entity;

/**
 *
 * @author Alexander
 */
public enum TaskStatus {
    OPEN            ("Open",        false),
    IN_PROGRESS     ("In Progress", false),
    DONE            ("Done",        true),
    CANCELLED       ("Cancelled",   true);
    
    private final String description;
    private final boolean finished;

    private TaskStatus(String description, boolean finished) {
        this.description = description;
        this.finished = finished;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFinished() {
        return finished;
    }
    
    public static TaskStatus fromOrdinal(int ordinal) {
        return values()[ordinal];
    }
}
