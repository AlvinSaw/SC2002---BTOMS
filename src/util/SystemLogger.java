package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging system events
 */
public class SystemLogger {
    private static final String LOG_DIRECTORY = "database/logs/";
    private static final String USER_LOG_FILE = LOG_DIRECTORY + "user_activity.log";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Initializes the logger by ensuring the log directory exists
     */
    static {
        try {
            Files.createDirectories(Paths.get(LOG_DIRECTORY));
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
        }
    }
    
    /**
     * Logs a user login event
     * @param userId User ID (NRIC)
     * @param userName User's name
     * @param userType Type of user (Applicant, HDBManager, HDBOfficer)
     */
    public static void logLogin(String userId, String userName, String userType) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMAT);
        String logMessage = String.format("%s | LOGIN | User: %s (%s) | Type: %s", 
                timestamp, userName, userId, userType);
        writeLog(logMessage);
    }
    
    /**
     * Logs a user logout event
     * @param userId User ID (NRIC)
     * @param userName User's name
     * @param userType Type of user (Applicant, HDBManager, HDBOfficer)
     */
    public static void logLogout(String userId, String userName, String userType) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMAT);
        String logMessage = String.format("%s | LOGOUT | User: %s (%s) | Type: %s", 
                timestamp, userName, userId, userType);
        writeLog(logMessage);
    }
    
    /**
     * Writes a log message to the user activity log file
     * @param message Log message to write
     */
    private static void writeLog(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_LOG_FILE, true))) {
            writer.println(message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}