package de.dahmen.alexander.akasha.core.repository;

import java.sql.SQLException;
import java.util.List;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Alexander
 */
public interface JdaUserRepository {
    
    void storeUser(User user) throws JdaUserRepositoryException;
    
    List<User> getUsers() throws JdaUserRepositoryException;
    
    PrivateChannel getPrivateChannel(long user) throws JdaUserRepositoryException;
    
    class JdaUserRepositoryException extends RepositoryException {
        public JdaUserRepositoryException(SQLException ex) {
            super(ex);
        }
    }
}
