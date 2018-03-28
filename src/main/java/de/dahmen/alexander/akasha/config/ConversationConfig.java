
package de.dahmen.alexander.akasha.config;

import de.dahmen.alexander.akasha.config.lib.ConfigClass;
import de.dahmen.alexander.akasha.config.lib.ConfigField;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Alexander
 */
@ConfigClass("conversation")
public class ConversationConfig {
    
    public static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;
    
    @ConfigField("async.timeout")
    private Long timeout;
    
    @ConfigField("async.timeout.unit")
    private String timeUnit;

    public Long getTimeoutMilliseconds() {
        return parseTimeUnit().toMillis((timeout == null) ? 0L : timeout);
    }
    
    private TimeUnit parseTimeUnit() {
        if (timeUnit == null || timeUnit.isEmpty()) {
            return DEFAULT_TIMEUNIT;
        } else {
            return TimeUnit.valueOf(timeUnit.toUpperCase());
        }
    }
}
