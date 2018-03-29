
package de.dahmen.alexander.akasha.core.repository;

import java.io.IOException;
import java.sql.SQLException;

/**
 *
 * @author Alexander
 */
public abstract class RepositoryException extends RuntimeException {
    
    private final boolean internal;
    
    protected RepositoryException(boolean internal, String message) {
        super(message);
        this.internal = internal;
    }
    
    protected RepositoryException(boolean internal, Throwable cause) {
        super(cause);
        this.internal = internal;
    }
    
    protected RepositoryException(boolean internal, String message, Throwable cause) {
        super(message, cause);
        this.internal = internal;
    }
    
    protected RepositoryException(SQLException sqlEx) {
        super("SQL Exception: " + sqlEx.getMessage(), sqlEx);
        this.internal = true;
    }
    
    protected RepositoryException(IOException ioEx) {
        super("IO Exception: " + ioEx.getMessage(), ioEx);
        this.internal = true;
    }
    
    public boolean isInternal() {
        return internal;
    }
}
