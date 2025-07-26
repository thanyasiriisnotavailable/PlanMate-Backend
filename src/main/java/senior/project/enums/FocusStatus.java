package senior.project.enums;

public enum FocusStatus {
    INCOMPLETE,   // Not started
    FOCUSING,     // Actively in session
    COMPLETED,    // Session ended properly
    CANCELLED,    // Ended early
    INTERRUPTED   // User disconnected / system stopped
}
