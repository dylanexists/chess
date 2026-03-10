package dataaccess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLBaseDao {

    @FunctionalInterface
    protected interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    } //accept and run input T

    @FunctionalInterface
    protected interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    } //apply input T and return R

    //simple execute function
    protected void execUpdateStatement(String statement) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Failed SQL update statement", e);
        }
    }

    //overload for statements with dynamic parameters
    protected void execUpdateStatement(String statement, SQLConsumer<PreparedStatement> paramSetter) throws DataAccessException{
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            paramSetter.accept(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Failed SQL update statement", e);
        }
    }

    protected <R> List<R> execQueryStatement(String statement, SQLConsumer<PreparedStatement> paramSetter,
                                 SQLFunction<ResultSet, R> mapResultFunction) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            paramSetter.accept(preparedStatement);
            try (ResultSet rs = preparedStatement.executeQuery()){
                List<R> result = new ArrayList<>();
                while(rs.next()){
                    result.add(mapResultFunction.apply(rs));
                }
                return result;
            }
        } catch (SQLException e){
            throw new DataAccessException("Failed SQL query statement", e);
        }
    }

    protected void configureDatabase(String createStatement) throws DataAccessException {
        execUpdateStatement(createStatement);
    }
}
