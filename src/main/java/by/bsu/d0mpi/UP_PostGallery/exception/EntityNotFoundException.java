package by.bsu.d0mpi.UP_PostGallery.exception;

public class EntityNotFoundException extends Exception{
    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
