package de.dahmen.alexander.akasha.core.service;

import java.time.ZonedDateTime;

/**
 *
 * @author Alexander
 */
public interface CronService {
    
    boolean validate(String cron);
    
    String describe(String cron) throws CronServiceException;
    
    ZonedDateTime nextExecution(ZonedDateTime now, String cron) throws CronServiceException;
    
    ZonedDateTime lastExecution(ZonedDateTime now, String cron) throws CronServiceException;
    
    class CronServiceException extends Exception {
        public CronServiceException(String msg) { super(msg); }
    }
}
