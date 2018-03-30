
package de.dahmen.alexander.akasha.service.quartz;

import de.dahmen.alexander.akasha.core.service.ScheduleService;
import de.dahmen.alexander.akasha.util.MapBuilder;
import java.util.concurrent.atomic.AtomicLong;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author Alexander
 */
public class QuartzScheduleService implements ScheduleService, AutoCloseable {
    
    private final static AtomicLong JOB_ID = new AtomicLong(0L);
    private final static String QUARTZ_GROUP = "TaskGroup";
    
    private final Scheduler scheduler;
    
    public QuartzScheduleService() throws SchedulerException {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
    }
    
    public QuartzScheduleService start() {
        try {
            scheduler.start();
            return this;
        }
        catch (SchedulerException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public boolean isValidCronSchedule(String cron) {
        return CronExpression.isValidExpression(cron);
    }

    @Override
    public long createCronSchedule(String cron, Runnable run) throws ScheduleServiceException {
        long id = JOB_ID.incrementAndGet();
        
        JobDataMap map = new JobDataMap(MapBuilder.builder()
                .put("runnable", run)
                .build());
        
        JobDetail job = JobBuilder.newJob(RunnableJob.class)
                .withIdentity(jobId(id), QUARTZ_GROUP)
                .setJobData(map)
                .build();
        
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerId(id), QUARTZ_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        
        try { scheduler.scheduleJob(job, trigger); }
        catch (SchedulerException ex) {
            throw new ScheduleServiceException(ex.getMessage());
        }
        
        return id;
    }

    @Override
    public void removeSchedule(long id) throws ScheduleServiceException {
        try {
            scheduler.deleteJob(new JobKey(Long.toHexString(id), QUARTZ_GROUP));
        }
        catch (SchedulerException ex) {
            throw new ScheduleServiceException(ex.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        try { scheduler.shutdown(false); }
        catch (SchedulerException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static String jobId(long id) {
        return "J".concat(Long.toHexString(id));
    }
    
    private static String triggerId(long id) {
        return "T".concat(Long.toHexString(id));
    }
    
    private static class RunnableJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                Object runnable = context.get("runnable");
                ((Runnable) runnable).run();
            }
            catch (Throwable thr) {
                throw new JobExecutionException(thr.getMessage(), thr);
            }
        }
    }
}
