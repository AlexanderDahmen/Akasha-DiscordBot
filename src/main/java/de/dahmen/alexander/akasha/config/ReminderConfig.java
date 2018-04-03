
package de.dahmen.alexander.akasha.config;

import de.dahmen.alexander.akasha.config.lib.ConfigClass;
import de.dahmen.alexander.akasha.config.lib.ConfigField;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author Alexander
 */
@ConfigClass("reminder")
@NoArgsConstructor
@AllArgsConstructor
public class ReminderConfig {
    @Getter
    @ConfigField("update.time")
    private Long updateTime;
    
    @ConfigField("update.time.unit")
    private String updateTimeUnit;
    
    public TimeUnit getUpdateTimeUnit() {
        return TimeUnit.valueOf(updateTimeUnit.trim().toUpperCase());
    }
}
