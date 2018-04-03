
package de.dahmen.alexander.akasha.service.cronutil;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import de.dahmen.alexander.akasha.core.service.CronService;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

/**
 *
 * @author Alexander
 */
public class CronUtilsCronService implements CronService {
    public static final CronType CRON_TYPE = CronType.QUARTZ;
    private static final CronDefinition CRON_DEFINITION = CronDefinitionBuilder.instanceDefinitionFor(CRON_TYPE);
    
    
    private final CronParser cronParser;
    private final CronDescriptor cronDescriptor;

    public CronUtilsCronService() {
        this.cronParser = new CronParser(CRON_DEFINITION);
        this.cronDescriptor = CronDescriptor.instance();
    }
    
    @Override
    public boolean validate(String cron) throws CronServiceException {
        try {
            cronParser.parse(cron).validate();
            return true;
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public String describe(String cron) throws CronServiceException {
        try {
            return cronDescriptor.describe(cronParser.parse(cron));
        }
        catch (IllegalArgumentException ex) {
            throw new CronServiceException(ex.getMessage());
        }
    }
    
    @Override
    public ZonedDateTime nextExecution(ZonedDateTime now, String cron) throws CronServiceException {
        return wrapIllegalArgument(() -> ExecutionTime
                .forCron(cronParser.parse(cron))
                .nextExecution(now)
                .orElse(null));
    }
    
    @Override
    public ZonedDateTime lastExecution(ZonedDateTime now, String cron) throws CronServiceException {
        return wrapIllegalArgument(() -> ExecutionTime
                .forCron(cronParser.parse(cron))
                .lastExecution(now)
                .orElse(null));
    }
    
    private <T> T wrapIllegalArgument(Supplier<T> supplier) throws CronServiceException {
        try { return supplier.get(); }
        catch (IllegalArgumentException ex) {
            throw new CronServiceException(ex.getMessage());
        }
    }
}
