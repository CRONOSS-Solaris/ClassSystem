package cronos.classsystem.utils;

import cronos.classsystem.ClassSystemPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Plik-only logger błędów pluginu. Pisze do {@code plugins/ClassSystem/logs/errors-YYYY-MM-DD.log}.
 *
 * Trzy źródła wpisów:
 * <ol>
 *   <li>JUL Handler na {@code plugin.getLogger()} — łapie każde
 *       {@link Level#WARNING} i {@link Level#SEVERE} które przechodzi przez logger pluginu.</li>
 *   <li>{@link #writeException(String, String, Throwable)} — wywoływane bezpośrednio przez
 *       {@code DebugLogger.debugException} niezależnie od flagi {@code debug.enabled},
 *       żeby stack trace zawsze lądował na dysku.</li>
 *   <li>{@link #writeMessage(Level, String, String)} — wspólny entry point dla obu wyżej.</li>
 * </ol>
 *
 * Rotacja: jeden plik na dzień (LocalDate). Plik aktualnego dnia jest dopisywany
 * (append). Folder tworzony lazy przy pierwszym zapisie.
 *
 * Thread-safety: wszystkie publiczne metody synchronized na {@code lock} — logger
 * może być wołany z dowolnego wątku (event handlery, async schedulery).
 */
public final class ErrorLogFileWriter {

    private static volatile ErrorLogFileWriter instance;

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ENTRY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final ClassSystemPlugin plugin;
    private final File logsDir;
    private final Object lock = new Object();

    /** Aktualnie otwarty writer (lub null jeśli jeszcze nic nie zapisano dziś). */
    private BufferedWriter writer;
    /** Data odpowiadająca aktualnie otwartemu writer'owi — na bazie tego rotujemy. */
    private LocalDate currentDate;
    /** Handler zarejestrowany na {@code plugin.getLogger()} — trzymamy referencję do unregister. */
    private JulBridge julBridge;

    private ErrorLogFileWriter(ClassSystemPlugin plugin) {
        this.plugin = plugin;
        this.logsDir = new File(plugin.getDataFolder(), "logs");
    }

    /** Inicjalizuje singleton i instaluje JUL handler. Wołane raz w {@code onEnable}. */
    public static void initialize(ClassSystemPlugin plugin) {
        if (instance != null) return;
        ErrorLogFileWriter writer = new ErrorLogFileWriter(plugin);
        writer.ensureDirectory();
        writer.installJulHandler();
        instance = writer;
    }

    public static ErrorLogFileWriter getInstance() {
        return instance;
    }

    /** Zamyka handler i otwarty plik. Wołane w {@code onDisable}. */
    public void shutdown() {
        synchronized (lock) {
            if (julBridge != null) {
                try {
                    plugin.getLogger().removeHandler(julBridge);
                    julBridge.close();
                } catch (Exception ignored) {
                    // Best effort — plugin się wyłącza, nie ma sensu eskalować.
                }
                julBridge = null;
            }
            closeWriter();
        }
    }

    /**
     * Zapisuje wyjątek z {@code DebugLogger.debugException} — zawsze, niezależnie
     * od flagi {@code debug.enabled} (żeby stack trace miał trwały ślad nawet gdy
     * konsola dewelopera ich nie pokazuje).
     */
    public void writeException(String category, String operation, Throwable throwable) {
        String header = "[" + category + "] " + operation;
        writeEntry(Level.SEVERE, "DebugLogger", header, throwable);
    }

    /** Zapisuje sformatowaną wiadomość bez wyjątku. Używane głównie wewnętrznie. */
    public void writeMessage(Level level, String loggerName, String message) {
        writeEntry(level, loggerName, message, null);
    }

    // ── Wewnętrzne ────────────────────────────────────────────────────────

    private void writeEntry(Level level, String loggerName, String message, Throwable throwable) {
        synchronized (lock) {
            try {
                ensureDirectory();
                rotateIfNewDay();
                if (writer == null) {
                    openWriterForToday();
                }
                String timestamp = LocalDateTime.now().format(ENTRY_TIME);
                writer.write("[" + timestamp + "] [" + level.getName() + "] [" + loggerName + "] " + message);
                writer.newLine();
                if (throwable != null) {
                    StringWriter sw = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(sw));
                    writer.write(sw.toString());
                }
                writer.flush();
            } catch (IOException e) {
                // Nie używamy plugin.getLogger().severe — to by zaowocowało pętlą
                // (severe → JulBridge → writeEntry → IOException → severe).
                // Wypadamy do System.err raz, bez recursji.
                System.err.println("[ClassSystem][ErrorLogFileWriter] Nie można zapisać do pliku logów: " + e.getMessage());
            }
        }
    }

    private void ensureDirectory() {
        if (!logsDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            logsDir.mkdirs();
        }
    }

    private void rotateIfNewDay() throws IOException {
        LocalDate today = LocalDate.now();
        if (currentDate != null && !currentDate.equals(today)) {
            closeWriter();
        }
    }

    private void openWriterForToday() throws IOException {
        currentDate = LocalDate.now();
        File file = new File(logsDir, "errors-" + currentDate.format(FILE_DATE) + ".log");
        writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // Best effort
            }
            writer = null;
            currentDate = null;
        }
    }

    private void installJulHandler() {
        julBridge = new JulBridge(this);
        julBridge.setLevel(Level.WARNING);
        julBridge.setFilter(new SeverityFilter());
        plugin.getLogger().addHandler(julBridge);
    }

    /**
     * JUL Handler który przerzuca rekordy do {@link ErrorLogFileWriter}.
     * Zarejestrowany na {@code plugin.getLogger()} w {@link #installJulHandler()}.
     */
    private static final class JulBridge extends Handler {
        private final ErrorLogFileWriter parent;

        JulBridge(ErrorLogFileWriter parent) {
            this.parent = parent;
        }

        @Override
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;
            String message = record.getMessage();
            if (message == null) message = "";
            parent.writeEntry(record.getLevel(), record.getLoggerName() != null ? record.getLoggerName() : "plugin",
                    message, record.getThrown());
        }

        @Override
        public void flush() { /* writer flushuje per-write */ }

        @Override
        public void close() throws SecurityException { /* parent zamyka writer */ }
    }

    /** Akceptuje tylko WARNING i SEVERE. */
    private static final class SeverityFilter implements Filter {
        @Override
        public boolean isLoggable(LogRecord record) {
            int v = record.getLevel().intValue();
            return v >= Level.WARNING.intValue();
        }
    }
}
