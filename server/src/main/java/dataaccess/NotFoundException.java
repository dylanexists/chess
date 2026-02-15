package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class NotFoundException extends DataAccessException{
    public NotFoundException(String message) {
        super(message);
    }
}
