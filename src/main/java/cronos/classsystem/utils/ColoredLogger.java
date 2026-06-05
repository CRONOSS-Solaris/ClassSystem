package cronos.classsystem.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * System kolorowych logów dla pluginu Klas.
 * Identyczny styl i schemat kolorowania co ColoredLogger w CitySystem.
 */
public class ColoredLogger {

    // Kolory ANSI pasujące do pluginu (niebiesko-zielone)
    public static final String PRIMARY_COLOR = "[36m";      // Cyan - główny kolor pluginu
    public static final String SECONDARY_COLOR = "[32m";    // Green - drugi kolor
    public static final String ACCENT_COLOR = "[33m";       // Yellow - akcent
    public static final String SUCCESS_COLOR = "[32m";      // Green - sukces
    public static final String WARNING_COLOR = "[33m";      // Yellow - ostrzeżenie
    public static final String ERROR_COLOR = "[31m";        // Red - błąd
    public static final String DEBUG_COLOR = "[37m";        // White - debug
    public static final String RESET_COLOR = "[0m";         // Reset

    private final JavaPlugin plugin;
    private final boolean debugEnabled;
    private final boolean ansiEnabled;

    public ColoredLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.debugEnabled = plugin.getConfig().getBoolean("debug.enabled", false);
        this.ansiEnabled = checkAnsiSupport();
    }

    /**
     * Sprawdza czy konsola obsługuje kolory ANSI
     */
    private boolean checkAnsiSupport() {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("windows");

        if (isWindows) {
            String term = System.getenv("TERM");
            String ansicon = System.getenv("ANSICON");
            String conemu = System.getenv("ConEmuANSI");
            String wtSession = System.getenv("WT_SESSION"); // Windows Terminal

            boolean isWindows10Plus = isWindows10OrLater();
            boolean hasAnsiEnv = term != null || ansicon != null || conemu != null || wtSession != null;

            return hasAnsiEnv || isWindows10Plus;
        }

        // Na Linux/Mac domyślnie obsługuje ANSI
        return true;
    }

    /**
     * Sprawdza czy to Windows 10 lub nowszy (obsługuje ANSI)
     */
    private boolean isWindows10OrLater() {
        try {
            String osVersion = System.getProperty("os.version");
            if (osVersion != null) {
                String[] parts = osVersion.split("\\.");
                if (parts.length >= 1) {
                    int major = Integer.parseInt(parts[0]);
                    return major >= 10;
                }
            }
        } catch (Exception e) {
            // Ignoruj błędy parsowania
        }
        return false;
    }

    /**
     * Loguje wiadomość z kolorami
     */
    public void log(Level level, String message) {
        String coloredMessage = colorizeMessage(message, level);
        plugin.getLogger().log(level, coloredMessage);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void severe(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Loguje wiadomość debug tylko gdy debug jest włączony
     */
    public void debug(String message) {
        if (debugEnabled) {
            log(Level.INFO, DEBUG_COLOR + "[DEBUG] " + message + RESET_COLOR);
        }
    }

    /**
     * Loguje wiadomość debug z kontekstem
     */
    public void debug(String context, String method, String message) {
        if (debugEnabled) {
            String debugMessage = String.format("[DEBUG-%s] [%s] %s",
                context.toUpperCase(), method, message);
            log(Level.INFO, DEBUG_COLOR + debugMessage + RESET_COLOR);
        }
    }

    /**
     * Koloruje wiadomość w zależności od poziomu logu
     */
    private String colorizeMessage(String message, Level level) {
        if (!ansiEnabled) {
            return message; // Zwróć bez kolorów jeśli ANSI nie jest obsługiwane
        }

        if (level == Level.SEVERE) {
            return ERROR_COLOR + message + RESET_COLOR;
        } else if (level == Level.WARNING) {
            return WARNING_COLOR + message + RESET_COLOR;
        } else if (level == Level.INFO) {
            // Banner / separator
            if (message.contains("——————————————————————[")) {
                return PRIMARY_COLOR + message + RESET_COLOR;
            }

            // Tytuł sekcji
            if (message.contains("Informacje o pluginie:") || message.contains("Informacje o cache:") ||
                message.contains("Podsumowanie wyłączania:")) {
                return SECONDARY_COLOR + message + RESET_COLOR;
            }

            // Główny komunikat sukcesu
            if (message.contains("pomyślnie uruchomiony") || message.contains("Plugin jest gotowy do użycia!")) {
                return SUCCESS_COLOR + message + RESET_COLOR;
            }

            // Bullet points
            if (message.contains("• ")) {
                if (message.contains("✓") || message.contains("✗") || message.contains("⚠")) {
                    return colorizeBulletPointWithStatus(message);
                }
                return SECONDARY_COLOR + message + RESET_COLOR;
            }

            // Podpunkty (z myślnikiem)
            if (message.trim().startsWith("- ")) {
                return PRIMARY_COLOR + message + RESET_COLOR;
            }

            // Sukces
            if (message.contains("✓") || message.contains("SUKCES") || message.contains("pomyślnie")) {
                return SUCCESS_COLOR + message + RESET_COLOR;
            }

            // Błąd / ostrzeżenie
            if (message.contains("✗") || message.contains("⚠")) {
                if (message.contains("✗")) {
                    return ERROR_COLOR + message + RESET_COLOR;
                } else {
                    return WARNING_COLOR + message + RESET_COLOR;
                }
            }

            // Debug
            if (message.contains("[DEBUG]")) {
                return DEBUG_COLOR + message + RESET_COLOR;
            }

            // Pusta linia / separator (tylko "> ")
            if (message.trim().equals(">") || message.trim().equals("> ")) {
                return PRIMARY_COLOR + message + RESET_COLOR;
            }

            // Domyślnie dla INFO - primary color jeśli zaczyna się od ">"
            if (message.startsWith("> ")) {
                return PRIMARY_COLOR + message + RESET_COLOR;
            }

            return message;
        }
        return message;
    }

    /**
     * Koloruje bullet point ze statusem (✓, ✗, ⚠)
     */
    private String colorizeBulletPointWithStatus(String message) {
        StringBuilder result = new StringBuilder();
        result.append(SECONDARY_COLOR);

        int pos = 0;
        while (pos < message.length()) {
            int nextCheck = message.indexOf("✓", pos);
            int nextCross = message.indexOf("✗", pos);
            int nextWarning = message.indexOf("⚠", pos);

            int nextStatus = -1;
            String statusChar = "";

            if (nextCheck != -1 && (nextStatus == -1 || nextCheck < nextStatus)) {
                nextStatus = nextCheck;
                statusChar = "✓";
            }
            if (nextCross != -1 && (nextStatus == -1 || nextCross < nextStatus)) {
                nextStatus = nextCross;
                statusChar = "✗";
            }
            if (nextWarning != -1 && (nextStatus == -1 || nextWarning < nextStatus)) {
                nextStatus = nextWarning;
                statusChar = "⚠";
            }

            if (nextStatus == -1) {
                result.append(message.substring(pos));
                break;
            }

            result.append(message.substring(pos, nextStatus));

            if (statusChar.equals("✓")) {
                result.append(RESET_COLOR).append(SUCCESS_COLOR).append("✓");
            } else if (statusChar.equals("✗")) {
                result.append(RESET_COLOR).append(ERROR_COLOR).append("✗");
            } else if (statusChar.equals("⚠")) {
                result.append(RESET_COLOR).append(WARNING_COLOR).append("⚠");
            }
            result.append(RESET_COLOR).append(SECONDARY_COLOR);

            pos = nextStatus + 1;
        }

        result.append(RESET_COLOR);
        return result.toString();
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Loguje wiadomość tylko gdy debug jest wyłączony
     */
    public void infoIfNotDebug(String message) {
        if (!debugEnabled) {
            info(message);
        }
    }

    /**
     * Loguje wiadomość zawsze (nawet gdy debug włączony)
     */
    public void infoAlways(String message) {
        info(message);
    }

    public boolean isAnsiEnabled() {
        return ansiEnabled;
    }
}
