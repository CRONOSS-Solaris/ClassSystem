package cronos.classsystem.utils;

import cronos.classsystem.ClassSystemPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebugLogger {

    private static DebugLogger instance;
    private final FileConfiguration config;
    private final ColoredLogger coloredLogger;

    // Kategorie debugowania
    private boolean debugEnabled;
    private boolean logDatabaseOperations;

    // Formatowanie czasu
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private DebugLogger(ClassSystemPlugin plugin) {
        this.config = plugin.getConfig();
        this.coloredLogger = new ColoredLogger(plugin);
        loadConfiguration();
    }

    /**
     * Pobiera instancję DebugLogger (Singleton)
     */
    public static DebugLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DebugLogger nie został zainicjalizowany! Wywołaj initialize() najpierw.");
        }
        return instance;
    }

    /**
     * Inicjalizuje DebugLogger
     */
    public static void initialize(ClassSystemPlugin plugin) {
        if (instance == null) {
            instance = new DebugLogger(plugin);
        }
    }

    /**
     * Ładuje konfigurację debugowania z pliku config.yml
     */
    private void loadConfiguration() {
        debugEnabled = config.getBoolean("debug.enabled", false);
        logDatabaseOperations = config.getBoolean("debug.log-database-operations", false);

        if (debugEnabled) {
            coloredLogger.infoAlways("=== SYSTEM DEBUG LOGÓW ZOSTAŁ WŁĄCZONY ===");
            coloredLogger.infoAlways("Kategorie debugowania:");
            coloredLogger.infoAlways("  • Operacje bazy danych: " + (logDatabaseOperations ? "✓" : "✗"));
            coloredLogger.infoAlways("  • Wszystkie inne operacje: " + (debugEnabled ? "✓" : "✗"));
            coloredLogger.infoAlways("===============================================");
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Loguje wiadomość debug z kategorią
     */
    public void debug(String category, String message) {
        if (!debugEnabled) return;

        String timestamp = LocalDateTime.now().format(timeFormatter);
        String formattedMessage = String.format("[DEBUG-%s] [%s] %s", category, timestamp, message);
        coloredLogger.debug(formattedMessage);
    }

    /**
     * Loguje wiadomość debug z kategorią i parametrami
     */
    public void debug(String category, String message, Object... params) {
        if (!debugEnabled) return;

        String formattedMessage = String.format(message, params);
        debug(category, formattedMessage);
    }

    /**
     * Loguje błąd debug z kategorią
     */
    public void debugError(String category, String message, Throwable throwable) {
        if (!debugEnabled) return;

        String timestamp = LocalDateTime.now().format(timeFormatter);
        String formattedMessage = String.format("[DEBUG-ERROR-%s] [%s] %s", category, timestamp, message);
        coloredLogger.warning(formattedMessage);
    }

    /**
     * Loguje metrykę wydajności
     */
    public void debugPerformance(String operation, long startTime, long endTime) {
        if (!debugEnabled) return;

        long duration = endTime - startTime;
        debug("PERFORMANCE", "Operacja '%s' zajęła %dms", operation, duration);
    }

    // === METODY SPECJALIZOWANE DLA KATEGORII ===

    public void debugDatabase(String operation, String details) {
        if (logDatabaseOperations) {
            debug("DATABASE", "[%s] %s", operation, details);
        }
    }

    public void debugDatabase(String operation, String query, Object... params) {
        if (logDatabaseOperations) {
            debug("DATABASE", "[%s] Query: %s | Params: %s", operation, query, java.util.Arrays.toString(params));
        }
    }

    public void debugCommand(String command, String player, String details) {
        if (debugEnabled) {
            debug("COMMAND", "[%s] Gracz: %s | %s", command, player, details);
        }
    }

    public void debugEvent(String eventName, String details) {
        if (debugEnabled) {
            debug("EVENT", "[%s] %s", eventName, details);
        }
    }

    public void debugService(String serviceName, String operation, String details) {
        if (debugEnabled) {
            debug("SERVICE", "[%s] %s | %s", serviceName, operation, details);
        }
    }

    public void debugGui(String operation, String player, String details) {
        if (debugEnabled) {
            debug("GUI", "[%s] Gracz: %s | %s", operation, player, details);
        }
    }

    public void debugValidation(String operation, String details) {
        if (debugEnabled) {
            debug("VALIDATION", "[%s] %s", operation, details);
        }
    }

    public void debugAsync(String operation, String details) {
        if (debugEnabled) {
            debug("ASYNC", "[%s] %s", operation, details);
        }
    }

    public void debugAsync(String operation, long startTime, long endTime, String details) {
        if (debugEnabled) {
            long duration = endTime - startTime;
            debug("ASYNC", "[%s] %s | Czas: %dms", operation, details, duration);
        }
    }

    // === METODY POMOCNICZE ===

    public void debugStart(String category, String operation) {
        debug(category, ">>> ROZPOCZYNAM: %s", operation);
    }

    public void debugEnd(String category, String operation) {
        debug(category, "<<< ZAKOŃCZONO: %s", operation);
    }

    public void debugEnd(String category, String operation, boolean success) {
        debug(category, "<<< ZAKOŃCZONO: %s | Rezultat: %s", operation, success ? "SUKCES" : "BŁĄD");
    }

    /**
     * Loguje wyjątek z kontekstem.
     *
     * Zapis do pliku {@code logs/errors-YYYY-MM-DD.log} jest BEZWARUNKOWY —
     * stack trace ląduje na dysku nawet gdy {@code debug.enabled=false}.
     * Konsola pokazuje wpis tylko gdy debug aktywny (ścieżka {@link #debugError}).
     */
    public void debugException(String category, String operation, Throwable throwable) {
        ErrorLogFileWriter writer = ErrorLogFileWriter.getInstance();
        if (writer != null) {
            writer.writeException(category,
                    String.format("Błąd podczas operacji '%s': %s", operation,
                            throwable != null ? throwable.getMessage() : "<null>"),
                    throwable);
        }
        debugError(category, String.format("Błąd podczas operacji '%s': %s", operation,
                throwable != null ? throwable.getMessage() : "<null>"), throwable);
    }

    public void debugObject(String category, String objectName, Object object) {
        if (!debugEnabled) return;

        String objectInfo = object != null ? object.toString() : "null";
        debug(category, "Obiekt '%s': %s", objectName, objectInfo);
    }

    public void debugMethodParams(String category, String methodName, Object... params) {
        if (!debugEnabled) return;

        StringBuilder paramInfo = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) paramInfo.append(", ");
            paramInfo.append("param").append(i + 1).append("=").append(params[i]);
        }

        debug(category, "Metoda '%s' wywołana z parametrami: %s", methodName, paramInfo.toString());
    }

    public void debugMethodResult(String category, String methodName, Object result) {
        if (!debugEnabled) return;

        debug(category, "Metoda '%s' zwróciła: %s", methodName, result);
    }

    /**
     * Przeładowuje konfigurację debugowania
     */
    public void reloadConfiguration() {
        loadConfiguration();
        if (debugEnabled) {
            coloredLogger.infoAlways("Konfiguracja debugowania została przeładowana");
        }
    }

    /**
     * Sprawdza czy konkretna kategoria jest włączona
     */
    public boolean isCategoryEnabled(String category) {
        switch (category.toUpperCase()) {
            case "DATABASE": return logDatabaseOperations;
            default: return debugEnabled;
        }
    }
}
