package mod.swinegraphics.util;

import mod.swinegraphics.Context;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Nani
 */
public final class Log {

    private Log() {
        throw new UnsupportedOperationException("Log is a utility class.");
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs the full stack trace of an exception to the external log file.
     * @param e The exception to log.
     * @param contextMessage A brief message describing where the error occurred.
     */
    public static void log(Throwable e, String contextMessage) {
        Logger.getLogger(contextMessage).log(Level.SEVERE, "Exception in %s".formatted(contextMessage), e);
        String stackTrace;
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            pw.flush();
            stackTrace = sw.toString();
        } catch (IOException ioException) {
            stackTrace = "Could not capture stack trace: " + ioException.getMessage();
        }
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format(
            "====================================================%n" +
            "TIMESTAMP: %s%n" +
            "CONTEXT: %s%n" +
            "EXCEPTION: %s%n" +
            "MESSAGE: %s%n" +
            "STACK TRACE:%n%s" +
            "====================================================%n%n",
            timestamp,
            contextMessage,
            e.getClass().getName(),
            e.getMessage(),
            stackTrace
        );
        try (FileWriter fw = new FileWriter(Context.getLogFile(), true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logEntry);
            System.err.println("CRITICAL ERROR LOGGED to " +Context.getLogFile().getAbsolutePath());
        } catch (IOException fileEx) {
            
            // Fallback: If we can't write to the file, print the error to the console.
            System.err.println("FATAL: Could not write error log to file: " + Context.getLogFile());
            e.printStackTrace();
            fileEx.printStackTrace();
        }
    }
}
