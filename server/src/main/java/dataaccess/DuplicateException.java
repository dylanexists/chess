package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DuplicateException extends DataAccessException{
    public DuplicateException(String message) {
        super(message);
    }
}
