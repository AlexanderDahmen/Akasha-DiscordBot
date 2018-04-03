
package de.dahmen.alexander.akasha.repository.mysql;

import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.entity.TaskPriority;
import de.dahmen.alexander.akasha.core.entity.TaskStatus;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Alexander
 */
public class MysqlJdaTaskRepository implements JdaTaskRepository {
    
    private final DataSource dataSource;

    public MysqlJdaTaskRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public long storeTask(Task task) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.UPDATE_GENERATE_ID,
                JdbcSqlUtil.statementGeneratedId(""
                        + "INSERT INTO akasha_task"
                        + " (user_id, task_name, task_type, task_status,"
                        + " task_priority, description, timezone,"
                        + " reminder_cron, last_reminder, deadline)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        task.getUserId(),
                        task.getName(),
                        task.getType().ordinal(),
                        task.getStatus().ordinal(),
                        task.getPriority().ordinal(),
                        task.getDescription(),
                        task.getTimeZone().getId(),
                        task.getReminderCron(),
                        toSqlTimestamp(task.getZonedLastReminder()),
                        toSqlTimestamp(task.getZonedDeadline())),
                JdbcSqlUtil.GENERATED_ID_LONG,
                JdaTaskRepositoryException::new);
    }
    
    @Override
    public boolean deleteTask(long taskId) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.UPDATE,
                JdbcSqlUtil.statement("DELETE FROM akasha_task WHERE id = ?", taskId),
                JdbcSqlUtil.UPDATE_COUNT_NOT_ZERO,
                JdaTaskRepositoryException::new);
    }
    
    @Override
    public boolean updateTask(Task update) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.UPDATE,
                JdbcSqlUtil.statement(""
                        + "UPDATE akasha_task SET"
                        + " task_name = ?,"
                        + " task_type = ?,"
                        + " task_status = ?,"
                        + " task_priority = ?,"
                        + " description = ?,"
                        + " timezone = ?,"
                        + " reminder_cron = ?,"
                        + " last_reminder = ?,"
                        + " deadline = ?"
                        + " WHERE"
                        + " id = ?",
                        update.getName(),
                        update.getType().ordinal(),
                        update.getStatus().ordinal(),
                        update.getPriority().ordinal(),
                        update.getDescription(),
                        update.getTimeZone().getId(),
                        update.getReminderCron(),
                        toSqlTimestamp(update.getZonedLastReminder()),
                        toSqlTimestamp(update.getZonedDeadline()),
                        update.getId()),
                JdbcSqlUtil.UPDATE_COUNT_NOT_ZERO,
                JdaTaskRepositoryException::new);
    }

    @Override
    public Long getIdByName(User user, String taskName) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(""
                        + "SELECT id"
                        + " FROM akasha_task"
                        + " WHERE user_id = ?"
                        + " AND task_name = ?"
                        + " LIMIT 1",
                        user.getIdLong(),
                        taskName),
                (rset) -> (rset.next()) ? rset.getLong(1) : null,
                JdaTaskRepositoryException::new);
    }
    
    @Override
    public boolean taskNameExists(User user, String taskName) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(""
                        + "SELECT 1"
                        + " FROM akasha_task"
                        + " WHERE user_id = ?"
                        + " AND task_name = ?"
                        + " LIMIT 1",
                        user.getIdLong(),
                        taskName),
                JdbcSqlUtil.RESULTSET_HAS_NEXT,
                JdaTaskRepositoryException::new);
    }

    @Override
    public Task getTaskById(long taskId) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(""
                        + "SELECT"
                        + " id, user_id, task_name, task_type, task_status, task_priority,"
                        + " description, timezone, reminder_cron, last_reminder, deadline"
                        + " FROM akasha_task"
                        + " WHERE id = ?",
                        taskId),
                this::resultSetToTask,
                JdaTaskRepositoryException::new);
    }
    
    @Override
    public Task getTaskByName(User user, String taskName) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(""
                        + "SELECT"
                        + " id, user_id, task_name, task_type, task_status, task_priority,"
                        + " description, timezone, reminder_cron, last_reminder, deadline"
                        + " FROM akasha_task"
                        + " WHERE user_id = ?"
                        + " AND task_name = ?",
                        user.getId(),
                        taskName),
                this::resultSetToTask,
                JdaTaskRepositoryException::new);
    }
    
    @Override
    public List<Task> getTasks(User user) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(""
                        + "SELECT"
                        + " id, user_id, task_name, task_type, task_status, task_priority,"
                        + " description, timezone, reminder_cron, last_reminder, deadline"
                        + " FROM akasha_task"
                        + " WHERE user_id = ?",
                        user.getIdLong()),
                this::resultSetToTaskList,
                JdaTaskRepositoryException::new);
    }

    @Override
    public boolean hasTasks(User user) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(""
                        + "SELECT 1"
                        + " FROM akasha_task"
                        + " WHERE user_id = ?"
                        + " LIMIT 1",
                        user.getIdLong()),
                JdbcSqlUtil.RESULTSET_HAS_NEXT,
                JdaTaskRepositoryException::new);
    }
    
    private List<Task> resultSetToTaskList(ResultSet rset) throws SQLException {
        Task task;
        List<Task> tasks = new ArrayList<>();
        while ((task = resultSetToTask(rset)) != null)
            tasks.add(task);
        return tasks;
    }
    
    private Task resultSetToTask(ResultSet rset) throws SQLException {
        if (!rset.next())
            return null;
        
        Task task = new Task();
        task.setId(rset.getLong("id"));
        task.setUserId(rset.getLong("user_id"));
        task.setName(rset.getString("task_name"));
        task.setType(Task.Type.values()[rset.getInt("task_type")]);
        task.setStatus(TaskStatus.values()[rset.getInt("task_status")]);
        task.setPriority(TaskPriority.values()[rset.getInt("task_priority")]);
        task.setDescription(rset.getString("description"));
        task.setReminderCron(rset.getString("reminder_cron"));
        task.setTimeZone(ZoneOffset.of(rset.getString("timezone")));
        task.setLastReminderInstant(toInstant(rset.getTimestamp("last_reminder")));
        task.setDeadlineInstant(toInstant(rset.getTimestamp("deadline")));
        return task;
    }
    
    private java.sql.Timestamp toSqlTimestamp(OffsetDateTime time) {
        return (time == null) ? null : java.sql.Timestamp.from(time.toInstant());
    }
    
    private Instant toInstant(java.sql.Timestamp timestamp) {
        return (timestamp == null) ? null : timestamp.toInstant();
    }
}
