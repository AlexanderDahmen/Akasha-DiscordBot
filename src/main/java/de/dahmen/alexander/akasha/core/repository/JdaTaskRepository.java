package de.dahmen.alexander.akasha.core.repository;

import de.dahmen.alexander.akasha.core.entity.Task;
import java.sql.SQLException;
import java.util.List;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Alexander
 */
public interface JdaTaskRepository {
    
    long storeTask(User user, Task task) throws JdaTaskRepositoryException;
    
    boolean updateTask(long taskId, Task update) throws JdaTaskRepositoryException;
    
    boolean taskNameExists(User user, String taskName) throws JdaTaskRepositoryException;
    
    Long getIdByName(User user, String taskName) throws JdaTaskRepositoryException;
    
    Task getTaskById(User user, long taskId) throws JdaTaskRepositoryException;
    
    Task getTaskByName(User user, String taskName) throws JdaTaskRepositoryException;
    
    List<Task> getTasks(User user) throws JdaTaskRepositoryException;
    
    class JdaTaskRepositoryException extends RepositoryException {
        public JdaTaskRepositoryException(SQLException ex) {
            super(ex);
        }
        public JdaTaskRepositoryException(boolean internal, String msg) {
            super(internal, msg);
        }
    }
}
