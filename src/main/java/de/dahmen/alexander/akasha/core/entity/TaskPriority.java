package de.dahmen.alexander.akasha.core.entity;

/**
 *
 * @author Alexander
 */
public enum TaskPriority {
    INACTIVE        ("Inactive"     , false),
    LOW             ("Low"          , true),
    NORMAL          ("Normal"       , true),
    HIGH            ("High"         , true),
    PANTS_ON_FIRE   ("PANTS-ON-FIRE", true);
    
    private final String description;
    private final boolean active;
    
    private TaskPriority(String description, boolean active) {
        this.description = description;
        this.active = active;
    }
    
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    
    public static TaskPriority fromOrdinal(int ordinal) {
        return values()[ordinal];
    }
}
