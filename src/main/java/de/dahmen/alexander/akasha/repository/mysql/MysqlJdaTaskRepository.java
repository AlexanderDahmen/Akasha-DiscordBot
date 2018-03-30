
package de.dahmen.alexander.akasha.repository.mysql;

import com.mysql.jdbc.Statement;
import de.dahmen.alexander.akasha.core.entity.DeadlineTask;
import de.dahmen.alexander.akasha.core.entity.RepeatTask;
import de.dahmen.alexander.akasha.core.entity.Task;
import de.dahmen.alexander.akasha.core.entity.TaskPriority;
import de.dahmen.alexander.akasha.core.entity.TaskStatus;
import de.dahmen.alexander.akasha.core.repository.JdaTaskRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
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
    public long storeTask(User user, Task task) throws JdaTaskRepositoryException {
        long userId = user.getIdLong();
        switch (task.getType()) {
            case REPEAT: return storeRepeatTask(userId, (RepeatTask) task);
            case DEADLINE: return storeDeadlineTask(userId, (DeadlineTask) task);
            default: throw new AssertionError(task.getType().name());
        }
    }

    @Override
    public boolean updateTask(long id, Task update) throws JdaTaskRepositoryException {
        int count = JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.UPDATE,
                (connection) -> {
                    PreparedStatement stmt = connection.prepareStatement(""
                            + "UPDATE akasha_task SET"
                            + " task_name = ?," // 1
                            + " task_type = ?," // 2
                            + " task_status = ?," // 3
                            + " task_priority = ?," // 4
                            + " period_seconds = ?," // 5
                            + " description = ?," // 6
                            + " deadline = ?," // 7
                            + " start_time = ?," // 8
                            + " repeat_cron = ?" // 9
                            + " WHERE id = ?"); // 10 [*]
                    
                    stmt.setString  (1, update.getName());
                    stmt.setInt     (2, update.getType().ordinal());
                    stmt.setInt     (3, update.getStatus().ordinal());
                    stmt.setInt     (4, update.getPriority().ordinal());
                    stmt.setString  (6, update.getDescription());
                    switch (update.getType()) {
                        case REPEAT:
                            stmt.setInt         (5, ((RepeatTask)update).getRepeatSeconds());
                            stmt.setNull        (7, java.sql.Types.TIMESTAMP);
                            stmt.setTime        (8, ((RepeatTask)update).getStartTime());
                            stmt.setString      (9, ((RepeatTask)update).getCron());
                            break;
                        case DEADLINE:
                            stmt.setInt         (5, ((DeadlineTask)update).getRemindSeconds());
                            stmt.setTimestamp   (7, ((DeadlineTask)update).getDeadline());
                            stmt.setNull        (8, java.sql.Types.TIME);
                            stmt.setNull        (9, java.sql.Types.VARCHAR);
                            break;
                        default:
                            throw new AssertionError(update.getType().name());
                    }
                    stmt.setLong(10, id);
                    
                    return stmt;
                },
                JdbcSqlUtil.UPDATE_COUNT,
                JdaTaskRepositoryException::new);
        
        if (count > 1)
            throw new JdaTaskRepositoryException(true, ""
                    + "Update affected two or more rows: "
                    + "ID = " + id
                    + ", Task = " + update.getName());
        
        return (count > 0);
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
    public Task getTaskById(User user, long taskId) throws JdaTaskRepositoryException {
        return getTask(user.getIdLong(), taskId, KeyType.ID);
    }
    
    @Override
    public Task getTaskByName(User user, String taskName) throws JdaTaskRepositoryException {
        return getTask(user.getIdLong(), taskName, KeyType.NAME);
    }
    
    @Override
    public List<Task> getTasks(User user) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(""
                        + "SELECT"
                        + " task_name, task_type, task_status, task_priority, description,"
                        + " period_seconds, deadline, start_time, repeat_cron"
                        + " FROM akasha_task"
                        + " WHERE user_id = ?",
                        user.getIdLong()),
                this::resultSetToTaskList,
                JdaTaskRepositoryException::new);
    }
    
    private Task getTask(long user, Object key, KeyType type) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                JdbcSqlUtil.statement(
                        String.format(""
                                + "SELECT"
                                + " task_name, task_type, task_status, task_priority, description,"
                                + " period_seconds, , deadline, start_time, repeat_cron"
                                + " FROM akasha_task"
                                + " WHERE user_id = ?"
                                + " AND %s = ?"
                                + " LIMIT 1",
                                type.column),
                        user,
                        key),
                this::resultSetToTask,
                JdaTaskRepositoryException::new);
    }
    
    private List<Task> resultSetToTaskList(ResultSet rset) throws SQLException {
        Task task;
        List<Task> tasks = new ArrayList<>();
        while ((task = resultSetToTask(rset)) != null) tasks.add(task);
        return tasks;
    }
    
    private Task resultSetToTask(ResultSet rset) throws SQLException {
        if (!rset.next())
            return null;
        
        Task.Type type = Task.Type.fromOrdinal(rset.getInt("task_type"));
        switch (type) {
            case REPEAT:
                return new RepeatTask(
                        rset.getString("task_name"),
                        rset.getString("description"),
                        TaskStatus.fromOrdinal(rset.getInt("task_status")),
                        TaskPriority.fromOrdinal(rset.getInt("task_priority")),
                        rset.getTime("start_time"),
                        rset.getString("repeat_cron"),
                        rset.getInt("period_seconds"));
            case DEADLINE:
                return new DeadlineTask(
                        rset.getString("task_name"),
                        rset.getString("description"),
                        TaskStatus.fromOrdinal(rset.getInt("task_status")),
                        TaskPriority.fromOrdinal(rset.getInt("task_priority")),
                        rset.getTimestamp("deadline"),
                        rset.getInt("period_seconds"));
            default:
                throw new AssertionError(type.name());
        }
    }
    
    private long storeRepeatTask(long user, RepeatTask task) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.UPDATE_GENERATE_ID,
                (connection) -> {
                    PreparedStatement stmt = connection.prepareStatement(""
                            + "INSERT INTO akasha_task"
                            + " (user_id, task_name, task_type, task_status, task_priority,"
                            + " period_seconds, description, start_time, repeat_cron)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    stmt.setLong    (1, user);
                    stmt.setString  (2, task.getName());
                    stmt.setInt     (3, task.getType().ordinal());
                    stmt.setInt     (4, task.getStatus().ordinal());
                    stmt.setInt     (5, task.getPriority().ordinal());
                    stmt.setInt     (6, task.getRepeatSeconds());
                    stmt.setString  (7, task.getDescription());
                    stmt.setTime    (8, task.getStartTime());
                    stmt.setString  (9, task.getCron());
                    return stmt;
                },
                JdbcSqlUtil.GENERATED_ID_LONG,
                JdaTaskRepositoryException::new);
    }
    
    private long storeDeadlineTask(long user, DeadlineTask task) throws JdaTaskRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.UPDATE_GENERATE_ID,
                (connection) -> {
                    PreparedStatement stmt = connection.prepareStatement(""
                            + "INSERT INTO akasha_task"
                            + " (user_id, task_name, task_type, task_status,"
                            + " task_priority, period_seconds, description, deadline)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    stmt.setLong        (1, user);
                    stmt.setString      (2, task.getName());
                    stmt.setInt         (3, task.getType().ordinal());
                    stmt.setInt         (4, task.getStatus().ordinal());
                    stmt.setInt         (5, task.getPriority().ordinal());
                    stmt.setInt         (6, task.getRemindSeconds());
                    stmt.setString      (7, task.getDescription());
                    stmt.setTimestamp   (8, task.getDeadline());
                    return stmt;
                },
                JdbcSqlUtil.GENERATED_ID_LONG,
                JdaTaskRepositoryException::new);
    }
    
    @AllArgsConstructor
    private static enum KeyType {
        ID  ("id"),
        NAME("task_name");
        
        final String column;
    }
}
