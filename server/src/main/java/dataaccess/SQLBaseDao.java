package dataaccess;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLBaseDao {

    @FunctionalInterface
    protected interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    //simple execute function
    protected void executeStatement(String statement) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Failed SQL statement", e);
        }
    }

    //overload for statements with dynamic parameters
    protected void executeStatement(String statement, SQLConsumer<PreparedStatement> paramSetter) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            paramSetter.accept(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Failed SQL statement", e);
        }
    }

    protected void configureDatabase(String createStatement) throws DataAccessException {
        executeStatement(createStatement);
    }
}
