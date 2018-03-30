package de.dahmen.alexander.akasha.core.service;

/**
 *
 * @author Alexander
 */
public interface ScheduleService {
    
    boolean isValidCronSchedule(String cron);
    
    long createCronSchedule(String cron, Runnable todo) throws ScheduleServiceException;
    
    void removeSchedule(long id) throws ScheduleServiceException;
    
    class ScheduleServiceException extends Exception {
        public ScheduleServiceException(String msg) { super(msg); }
    }
}
