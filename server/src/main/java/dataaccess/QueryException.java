package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class QueryException extends DataAccessException{
    public QueryException(String message) {
        super(message);
    }
}
