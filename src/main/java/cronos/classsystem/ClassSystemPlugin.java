package cronos.classsystem;

import cronos.classsystem.utils.ColoredLogger;
import cronos.classsystem.utils.DebugLogger;
import cronos.classsystem.utils.ErrorLogFileWriter;
import org.bukkit.plugin.java.JavaPlugin;

public class ClassSystemPlugin extends JavaPlugin {

    private static ClassSystemPlugin instance;

    private ColoredLogger coloredLogger;

    /**
     * Singleton ustawiany w {@link #onEnable()} — bezpieczny do użycia z serwisów
     * konstruowanych po starcie pluginu, NIE z onLoad / static initializerów.
     */
    public static ClassSystemPlugin getInstance() {
        return instance;
    }

    public ColoredLogger getColoredLogger() {
        return coloredLogger;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Zapis domyślnego config.yml zanim cokolwiek czyta debug.enabled.
        saveDefaultConfig();

        coloredLogger = new ColoredLogger(this);

        // Plik-only logger błędów (logs/errors-YYYY-MM-DD.log) + JUL Handler na WARNING+SEVERE.
        ErrorLogFileWriter.initialize(this);
        DebugLogger.initialize(this);

        DebugLogger.getInstance().debugService("ClassSystemPlugin", "onEnable", "Rozpoczynam uruchamianie pluginu");

        coloredLogger.infoIfNotDebug("=== URUCHAMIANIE PLUGINU KLAS ===");
        coloredLogger.infoIfNotDebug("Wersja: " + getDescription().getVersion());
        coloredLogger.infoIfNotDebug("Autor: " + getDescription().getAuthors());
        coloredLogger.infoIfNotDebug("API Version: " + getDescription().getAPIVersion());
        coloredLogger.infoIfNotDebug("Kolory ANSI: " + (coloredLogger.isAnsiEnabled() ? "✓ Włączone" : "✗ Wyłączone"));

        // TODO: inicjalizacja serwisów / bazy / komend wraz z rozwojem pluginu.

        printStartupBanner();
    }

    @Override
    public void onDisable() {
        String pluginName = getDescription().getName();

        if (coloredLogger != null) {
            coloredLogger.infoAlways("> ——————————————————————[ WYŁĄCZANIE " + pluginName + " ]——————————————————————");
            coloredLogger.infoAlways("> ");
            coloredLogger.infoAlways("> " + pluginName + " został wyłączony.");
            coloredLogger.infoAlways("> ");
            coloredLogger.infoAlways("> ——————————————————————[ WYŁĄCZANIE " + pluginName + " ]——————————————————————");
        }

        // Zamknięcie pliku logów i odpięcie JUL Handlera — na końcu, po ostatnich logach.
        ErrorLogFileWriter writer = ErrorLogFileWriter.getInstance();
        if (writer != null) {
            writer.shutdown();
        }
    }

    private void printStartupBanner() {
        String pluginName = getDescription().getName();
        String version = getDescription().getVersion();
        String authors = String.join(", ", getDescription().getAuthors());
        String databaseType = getConfig().getString("database.type", "Nieznany");

        coloredLogger.infoAlways("> ——————————————————————[ " + pluginName + " ]——————————————————————");
        coloredLogger.infoAlways("> ");
        coloredLogger.infoAlways("> " + pluginName + " pomyślnie uruchomiony!");
        coloredLogger.infoAlways("> ");
        coloredLogger.infoAlways("> Informacje o pluginie:");
        coloredLogger.infoAlways(">   • Wersja: " + version);
        coloredLogger.infoAlways(">   • Autorzy: " + authors);
        coloredLogger.infoAlways(">   • Baza danych: " + databaseType);
        coloredLogger.infoAlways("> ");
        coloredLogger.infoAlways("> ——————————————————————[ " + pluginName + " ]——————————————————————");
    }
}
