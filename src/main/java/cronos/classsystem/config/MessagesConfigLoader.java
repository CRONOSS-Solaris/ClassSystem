package cronos.classsystem.config;

import cronos.classsystem.ClassSystemPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

/**
 * Ładuje, przeładowuje i zapisuje plik tłumaczeń ({@code Translations/<lang>.yml}).
 *
 * Wydzielone z {@link ConfigManager} aby logika I/O i fallback'u (kopiowanie z JARa,
 * pl.yml jako fallback dla nie-polskich języków) miała własną klasę. Loader oddaje
 * jedynie surowy {@link FileConfiguration} — pobieranie konkretnych wiadomości,
 * placeholdery i kolory zostają w {@link ConfigManager}.
 */
public final class MessagesConfigLoader {

    private final ClassSystemPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private FileConfiguration fallbackMessagesConfig;

    public MessagesConfigLoader(ClassSystemPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Wczytuje plik tłumaczeń dla aktywnego języka. Gdy plik nie istnieje w folderze
     * pluginu — wyciągamy go z JARa; gdy zasobu brak — spadamy na pl.yml; gdy i tego
     * brak — tworzymy pusty plik aby uniknąć NPE.
     *
     * <p>Brakujące klucze są pokrywane runtime-fallbackiem przez {@code setDefaults}
     * z bundled — BEZ zapisu do pliku (żeby nie nadpisać komentarzy zachowanych przez
     * {@link ConfigMigrator}). Persistent merge robi {@link ConfigMigrator}.
     */
    public void load() {
        String language = plugin.getConfig().getString("general.language", "pl").toLowerCase();
        File translationsDir = new File(plugin.getDataFolder(), "Translations");
        if (!translationsDir.exists()) {
            translationsDir.mkdirs();
        }

        messagesFile = new File(translationsDir, language + ".yml");
        if (!messagesFile.exists()) {
            try {
                plugin.saveResource("Translations/" + language + ".yml", false);
            } catch (IllegalArgumentException ignored) {
                try {
                    plugin.saveResource("Translations/pl.yml", false);
                    messagesFile = new File(translationsDir, "pl.yml");
                    language = "pl";
                } catch (IllegalArgumentException ignored2) {
                    try {
                        messagesFile.createNewFile();
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.WARNING,
                                "Nie można utworzyć pliku tłumaczeń: " + messagesFile.getAbsolutePath(), e);
                    }
                }
            }
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Runtime-fallback dla brakujących kluczy (defense-in-depth). BEZ copyDefaults/save.
        try (InputStream defStream = plugin.getResource("Translations/" + language + ".yml")) {
            if (defStream != null) {
                try (InputStreamReader reader = new InputStreamReader(defStream)) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(reader);
                    messagesConfig.setDefaults(defConfig);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "Nie udało się załadować bundled translation '" + language + ".yml': " + e.getMessage());
        }

        // Fallback do pl.yml dla nie-polskich języków
        fallbackMessagesConfig = null;
        if (!"pl".equals(language)) {
            try (InputStream plStream = plugin.getResource("Translations/pl.yml")) {
                if (plStream != null) {
                    try (InputStreamReader reader = new InputStreamReader(plStream)) {
                        fallbackMessagesConfig = YamlConfiguration.loadConfiguration(reader);
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Nie udało się załadować fallback pl.yml: " + e.getMessage());
            }
        }
    }

    public void reload() {
        load();
    }

    public void save() {
        if (messagesConfig == null || messagesFile == null) {
            return;
        }
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Nie udało się zapisać konfiguracji wiadomości", e);
        }
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getFallbackMessagesConfig() {
        return fallbackMessagesConfig;
    }
}
