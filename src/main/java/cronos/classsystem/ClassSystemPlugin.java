package cronos.classsystem;

import cronos.classsystem.commands.ClassMainCommand;
import cronos.classsystem.config.ConfigManager;
import cronos.classsystem.config.ConfigMigrator;
import cronos.classsystem.utils.ColoredLogger;
import cronos.classsystem.utils.DebugLogger;
import cronos.classsystem.utils.ErrorLogFileWriter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ClassSystemPlugin extends JavaPlugin {

    private static ClassSystemPlugin instance;

    private ColoredLogger coloredLogger;
    private ConfigManager configManager;

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

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // config.yml musi istnieć zanim cokolwiek czyta debug.enabled.
        saveDefaultConfig();

        coloredLogger = new ColoredLogger(this);
        ErrorLogFileWriter.initialize(this);
        DebugLogger.initialize(this);

        DebugLogger.getInstance().debugService("ClassSystemPlugin", "onEnable", "Rozpoczynam uruchamianie pluginu");

        coloredLogger.infoIfNotDebug("=== URUCHAMIANIE PLUGINU KLAS ===");
        coloredLogger.infoIfNotDebug("Wersja: " + getDescription().getVersion());
        coloredLogger.infoIfNotDebug("Autor: " + getDescription().getAuthors());
        coloredLogger.infoIfNotDebug("API Version: " + getDescription().getAPIVersion());
        coloredLogger.infoIfNotDebug("Kolory ANSI: " + (coloredLogger.isAnsiEnabled() ? "✓ Włączone" : "✗ Wyłączone"));

        // Auto-migracja config.yml + Translations przy bumpie configVersion.
        new ConfigMigrator(this).migrateAll();

        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Rejestracja komendy głównej (samorejestracja przez refleksję na CommandMap).
        new ClassMainCommand(this);

        // TODO: inicjalizacja serwisów / bazy wraz z rozwojem pluginu.

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

        ErrorLogFileWriter writer = ErrorLogFileWriter.getInstance();
        if (writer != null) {
            writer.shutdown();
        }
    }

    // ── Wiadomości ────────────────────────────────────────────────────────

    /**
     * Pobiera pokolorowaną wiadomość z prefiksem (delegacja do {@link ConfigManager}).
     */
    public String getMessage(String key, String... replacements) {
        return configManager.getMessage(key, replacements);
    }

    /**
     * Wysyła wiadomość (lub listę) z prefiksem na pierwszej linii. Klucz spod
     * {@code Translations/*.yml}; replacements jako pary {@code "{token}", wartość}.
     */
    public void sendMessage(CommandSender recipient, String messageKey, String... replacements) {
        if (recipient == null || messageKey == null) return;
        String prefix = configManager.getMessage("prefix");
        List<String> lines = configManager.getMessageListNoPrefix(messageKey, replacements);
        boolean first = true;
        for (String line : lines) {
            recipient.sendMessage(first ? prefix + line : line);
            first = false;
        }
    }

    public void sendMessage(Player player, String messageKey, String... replacements) {
        sendMessage((CommandSender) player, messageKey, replacements);
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
        coloredLogger.infoAlways(">   • Język: " + configManager.getLanguage());
        coloredLogger.infoAlways(">   • Baza danych: " + databaseType);
        coloredLogger.infoAlways("> ");
        coloredLogger.infoAlways("> ——————————————————————[ " + pluginName + " ]——————————————————————");
    }
}
