
package de.dahmen.alexander.akasha.repository.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author Alexander
 */
public class JdbcSqlUtil {

    public static enum FunctionType {
        QUERY,
        UPDATE,
        UPDATE_GENERATE_ID
    }
    
    @FunctionalInterface
    public static interface StatementFunction {
        PreparedStatement createStatement(
                Connection connection)
                throws SQLException;
    }
    
    @FunctionalInterface
    public static interface ResultFunction<T> {
        T createResult(
                ResultSet rset, Object generatedId, Integer updateCount)
                throws SQLException;
    }
    
    @FunctionalInterface
    public static interface ResultSetFunction<T> {
        T createResult(ResultSet rset) throws SQLException;
    }
    
    @FunctionalInterface
    public static interface SQLExceptionWrapper<T extends Throwable> {
        T wrapException(SQLException ex);
    }
    
    public final static ResultFunction<Void> NO_RESULT = (r, g, u) -> null;
    public final static ResultFunction<Integer> UPDATE_COUNT = (r, g, u) -> u;
    public final static ResultFunction<Object> GENERATED_ID = (r, g, u) -> g;
    public final static ResultFunction<Long> GENERATED_ID_LONG = (r, g, u) -> ((Number) g).longValue();
    public final static ResultFunction<Boolean> RESULTSET_HAS_NEXT = (r, g, u) -> r.next();
    
    public static <T> T query(
            DataSource dataSource,
            FunctionType type,
            StatementFunction statementFunction,
            ResultFunction<T> resultFunction)
            throws SQLException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        T result = null;
        
        final Object generatedId;
        final int count;
        
        // If GENERATED_ID or GENERATED_ID_LONG are passed as ResultFunction,
        // force the FunctionType to be UPDATE_GENERATE_ID
        if (resultFunction == GENERATED_ID || resultFunction == GENERATED_ID_LONG)
            type = FunctionType.UPDATE_GENERATE_ID;
        
        try {
            connection = dataSource.getConnection();
            statement = statementFunction.createStatement(connection);
            switch (type) {
                case QUERY:
                    resultSet = statement.executeQuery();
                    result = resultFunction.createResult(resultSet, null, null);
                    break;
                case UPDATE:
                    count = statement.executeUpdate();
                    result = resultFunction.createResult(resultSet, null, count);
                    break;
                case UPDATE_GENERATE_ID:
                    count = statement.executeUpdate();
                    resultSet = statement.getGeneratedKeys();
                    generatedId = getGeneratedId(resultSet);
                    result = resultFunction.createResult(resultSet, generatedId, count);
                    break;
                default:
                    throw new AssertionError(type);
            }

            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }
            statement.close();
            statement = null;
            connection.close();
            connection = null;

            return result;
        }
        finally {
            if (connection != null) try { connection.close(); } catch (SQLException ignored) { }
            if (statement != null) try { statement.close(); } catch (SQLException ignored) { }
            if (resultSet != null) try { resultSet.close(); } catch (SQLException ignored) { }
        }
    }
    
    public static <T, E extends Throwable> T query(
            DataSource dataSource,
            FunctionType type,
            StatementFunction statementFunction,
            ResultFunction<T> resultFunction,
            SQLExceptionWrapper<E> exceptionWrapper)
            throws E
    {
        try {
            return query(dataSource, type, statementFunction, resultFunction);
        }
        catch (SQLException ex) {
            throw exceptionWrapper.wrapException(ex);
        }
    }
    
    public static <T> T query(
            DataSource dataSource,
            FunctionType type,
            StatementFunction statementFunction,
            ResultSetFunction<T> resultSetFunction)
            throws SQLException
    {
        return query(dataSource, type, statementFunction,
                (rset, g, u) -> resultSetFunction.createResult(rset));
    }
    
    public static <T, E extends Throwable> T query(
            DataSource dataSource,
            FunctionType type,
            StatementFunction statementFunction,
            ResultSetFunction<T> resultSetFunction,
            SQLExceptionWrapper<E> exceptionWrapper)
            throws E
    {
        return query(dataSource, type, statementFunction,
                (rset, g, u) -> resultSetFunction.createResult(rset),
                exceptionWrapper);
    }
    
    
    private static Object getGeneratedId(ResultSet resultSet) throws SQLException {
        if (resultSet == null) return null;
        else return (resultSet.next()) ?
                resultSet.getObject(1) :
                null;
    }
    
    private JdbcSqlUtil() { }
}

