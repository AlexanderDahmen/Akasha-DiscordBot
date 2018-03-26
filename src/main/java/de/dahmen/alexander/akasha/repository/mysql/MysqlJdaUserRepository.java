
package de.dahmen.alexander.akasha.repository.mysql;

import de.dahmen.alexander.akasha.core.repository.JdaUserRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;

/**
 *
 * @author Alexander
 */
public class MysqlJdaUserRepository implements JdaUserRepository {
    
    private final JDA jda;
    private final DataSource dataSource;

    public MysqlJdaUserRepository(JDA jda, DataSource dataSource) {
        this.jda = jda;
        this.dataSource = dataSource;
    }
    
    @Override
    public void storeUser(User user) throws JdaUserRepositoryException {
        long channelId = (user.hasPrivateChannel()) ?
                user.openPrivateChannel().complete().getIdLong() : 0L;
        
        JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.UPDATE,
                (connection) -> {
                    PreparedStatement stmt = connection.prepareStatement(""
                            + "INSERT INTO akasha_user"
                            + " (id, user_name, channel_id)"
                            + " VALUES (?, ?, ?)");
                    stmt.setLong(1, user.getIdLong());
                    stmt.setString(2, user.getName());
                    stmt.setLong(3, channelId);
                    return stmt;
                },
                JdbcSqlUtil.NO_RESULT,
                JdaUserRepositoryException::new);
    }
    
    @Override
    public List<User> getUsers() throws JdaUserRepositoryException {
        return JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                (connection) -> {
                    PreparedStatement stmt = connection.prepareStatement(
                            "SELECT id FROM akasha_user");
                    return stmt;
                },
                this::resultSetToUsers,
                JdaUserRepositoryException::new);
    }

    @Override
    public PrivateChannel getPrivateChannel(long user) throws JdaUserRepositoryException {
        long channelId = JdbcSqlUtil.query(dataSource, JdbcSqlUtil.FunctionType.QUERY,
                (connection) -> {
                    PreparedStatement stmt = connection.prepareStatement(""
                            + "SELECT channel_id"
                            + " FROM akasha_user"
                            + " WHERE id = ?");
                    return stmt;
                },
                (rset) -> (rset.next()) ? rset.getLong(1) : 0,
                JdaUserRepositoryException::new);
        
        return (channelId == 0) ?
                jda.getUserById(user).openPrivateChannel().complete() :
                jda.getPrivateChannelById(channelId);
    }
    
    private List<User> resultSetToUsers(ResultSet rset) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rset.next())
            users.add(jda.getUserById(rset.getLong(1)));
        return users;
    }
}
