package hexlet.code.app.exception;

public class AssociatedWithEntityException extends RuntimeException {
    public AssociatedWithEntityException(String message) {
        super(message);
    }
}
