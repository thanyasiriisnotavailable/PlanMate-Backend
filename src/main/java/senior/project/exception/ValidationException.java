package senior.project.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
        System.out.println("ValidationException: " + message); // Print to console
    }
}
