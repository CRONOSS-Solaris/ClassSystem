package cronos.classsystem.config;

import cronos.classsystem.ClassSystemPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dostęp do config.yml i tłumaczeń: pobieranie wiadomości (z/bez prefiksu, listy),
 * kolory, aliasy i opisy komend. Wersja ogólna, wspólny schemat z CitySystem.
 */
public class ConfigManager {

    private final ClassSystemPlugin plugin;
    private final MessagesConfigLoader messagesLoader;

    public ConfigManager(ClassSystemPlugin plugin) {
        this.plugin = plugin;
        this.messagesLoader = new MessagesConfigLoader(plugin);
    }

    // ── Ładowanie / reload ────────────────────────────────────────────────

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        messagesLoader.load();
    }

    public void reloadAll() {
        plugin.reloadConfig();
        messagesLoader.reload();
    }

    public void saveMessagesConfig() {
        messagesLoader.save();
    }

    public FileConfiguration getMessagesConfig() {
        return messagesLoader.getMessagesConfig();
    }

    public boolean isMessagesConfigLoaded() {
        return messagesLoader.getMessagesConfig() != null;
    }

    // ── Wiadomości ────────────────────────────────────────────────────────

    /**
     * Tłumaczy kody kolorów {@code &} na sekcje §.
     */
    public String translateColorCodes(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String applyReplacements(String message, String... replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }

    private String rawMessage(String key) {
        if (messagesLoader.getMessagesConfig() == null) {
            return null;
        }
        String message = messagesLoader.getMessagesConfig().getString(key);
        if (message == null && messagesLoader.getFallbackMessagesConfig() != null) {
            message = messagesLoader.getFallbackMessagesConfig().getString(key);
        }
        return message;
    }

    /**
     * Pobiera wiadomość z prefiksem (klucz "prefix"). Zwraca pokolorowany string.
     */
    public String getMessage(String key, String... replacements) {
        if ("prefix".equals(key)) {
            return translateColorCodes(rawOrDefault("prefix", "&8[&bClassSystem&8]&r "));
        }
        String message = rawMessage(key);
        if (message == null) {
            plugin.getLogger().warning("Nie znaleziono wiadomości: " + key);
            return translateColorCodes("&cNie znaleziono wiadomości: " + key);
        }
        String prefix = rawOrDefault("prefix", "&8[&bClassSystem&8]&r ");
        return translateColorCodes(applyReplacements(prefix + message, replacements));
    }

    /**
     * Pobiera wiadomość bez prefiksu (GUI, hologramy, raw).
     */
    public String getMessageNoPrefix(String key, String... replacements) {
        if (messagesLoader.getMessagesConfig() == null) {
            return translateColorCodes("&cBłąd konfiguracji wiadomości!");
        }
        String message = rawMessage(key);
        if (message == null) {
            plugin.getLogger().warning("Nie znaleziono wiadomości: " + key);
            return translateColorCodes("&cNie znaleziono wiadomości: " + key);
        }
        return translateColorCodes(applyReplacements(message, replacements));
    }

    /**
     * Pobiera listę wiadomości bez prefiksu. Fallback do pl.yml, potem do pojedynczego stringa.
     */
    public List<String> getMessageListNoPrefix(String key, String... replacements) {
        if (messagesLoader.getMessagesConfig() == null) {
            return List.of(translateColorCodes("&cBłąd konfiguracji wiadomości!"));
        }
        List<String> messages = messagesLoader.getMessagesConfig().getStringList(key);
        if ((messages == null || messages.isEmpty()) && messagesLoader.getFallbackMessagesConfig() != null) {
            messages = messagesLoader.getFallbackMessagesConfig().getStringList(key);
        }
        if (messages == null || messages.isEmpty()) {
            String single = rawMessage(key);
            if (single != null) {
                return List.of(translateColorCodes(applyReplacements(single, replacements)));
            }
            return List.of(translateColorCodes("&cNie znaleziono wiadomości: " + key));
        }
        List<String> result = new ArrayList<>();
        for (String message : messages) {
            result.add(translateColorCodes(applyReplacements(message, replacements)));
        }
        return result;
    }

    private String rawOrDefault(String key, String def) {
        String raw = rawMessage(key);
        return raw != null ? raw : def;
    }

    // ── Ustawienia ────────────────────────────────────────────────────────

    public boolean isSystemEnabled() {
        return plugin.getConfig().getBoolean("general.enabled", true);
    }

    public String getLanguage() {
        return plugin.getConfig().getString("general.language", "pl");
    }

    public boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean("debug.enabled", false);
    }

    // ── Komendy ───────────────────────────────────────────────────────────

    public String getCommandPermission(String commandName) {
        return "ClassSystem." + commandName;
    }

    public String getPermissionMessage() {
        return getMessageNoPrefix("errors.no-permission-command");
    }

    public String getMainCommandName() {
        String fromMessages = rawMessage("commands.main");
        if (fromMessages != null && !fromMessages.isEmpty()) {
            return fromMessages;
        }
        return "klasa";
    }

    public String getCommandDescription() {
        String desc = rawMessage("commands.description");
        return desc != null ? desc : "Zarządzanie klasami postaci";
    }

    public List<String> getCommandAliases() {
        if (messagesLoader.getMessagesConfig() != null) {
            List<String> aliases = messagesLoader.getMessagesConfig().getStringList("commands.aliases");
            if ((aliases == null || aliases.isEmpty()) && messagesLoader.getFallbackMessagesConfig() != null) {
                aliases = messagesLoader.getFallbackMessagesConfig().getStringList("commands.aliases");
            }
            if (aliases != null && !aliases.isEmpty()) {
                return aliases;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Aliasy subkomendy (tłumaczalne). Pusta lista gdy brak — caller użyje nazwy kanonicznej.
     */
    public List<String> getSubcommandAliases(String subcommandName) {
        String path = "commands.subcommands." + subcommandName + ".aliases";
        if (messagesLoader.getMessagesConfig() != null) {
            List<String> fromMessages = messagesLoader.getMessagesConfig().getStringList(path);
            if ((fromMessages == null || fromMessages.isEmpty()) && messagesLoader.getFallbackMessagesConfig() != null) {
                fromMessages = messagesLoader.getFallbackMessagesConfig().getStringList(path);
            }
            if (fromMessages != null && !fromMessages.isEmpty()) {
                return fromMessages;
            }
        }
        return new ArrayList<>();
    }

    public String getSubcommandDescription(String subcommandName) {
        String desc = rawMessage("commands.subcommands." + subcommandName + ".description");
        return desc != null && !desc.isEmpty() ? desc : "Brak opisu";
    }
}
