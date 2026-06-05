package cronos.classsystem.config;

import cronos.classsystem.ClassSystemPlugin;
import cronos.classsystem.utils.ColoredLogger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Auto-migracja plików konfiguracyjnych przy starcie pluginu.
 *
 * Każdy zarządzany plik (config.yml, Translations/*.yml) ma pole {@code configVersion}.
 * Przy starcie porównujemy je z bundled resource w JARze:
 *
 * <ul>
 *   <li>brak user-pliku → kopia z JARa (saveResource)</li>
 *   <li>{@code userVer == bundledVer} → no-op</li>
 *   <li>{@code userVer != bundledVer} → backup + deep-merge: bundled jako szablon
 *       (komentarze, nowe defaulty), user values overrid'ują leaf-keys, user-extras
 *       zachowane → save → bump configVersion</li>
 *   <li>{@code userVer > bundledVer} (downgrade) → log warning, no-op</li>
 * </ul>
 *
 * Zachowanie komentarzy YAML wymaga Paper 1.18.2+ ({@code parseComments(true)}).
 */
public final class ConfigMigrator {

    /**
     * Klucze do aktywnego USUNIĘCIA z user-pliku przy migracji (per zasób).
     * Format: ścieżka kropkowa; sekcja usuwana wraz z dziećmi.
     */
    private static final Map<String, List<String>> DEPRECATED_KEYS = Map.of();

    private final ClassSystemPlugin plugin;
    private final ColoredLogger log;

    public ConfigMigrator(ClassSystemPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getColoredLogger();
    }

    public void migrateAll() {
        migrateFile("config.yml");
        migrateFile("Translations/pl.yml");
        migrateFile("Translations/en.yml");
    }

    private void migrateFile(String resourcePath) {
        File userFile = new File(plugin.getDataFolder(), resourcePath);

        // Fresh install — kopiuj z JARa.
        if (!userFile.exists()) {
            File parent = userFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try {
                plugin.saveResource(resourcePath, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.WARNING,
                        "ConfigMigrator: brak bundled resource " + resourcePath, e);
            }
            return;
        }

        InputStream bundledStream = plugin.getResource(resourcePath);
        if (bundledStream == null) {
            return; // orphan user-plik — nie ruszamy
        }

        // pathSeparator='\0' — zachowuje literalne klucze z kropkami (np. permission node).
        YamlConfiguration userCfg = new YamlConfiguration();
        userCfg.options().parseComments(true).pathSeparator('\0');
        try {
            userCfg.load(userFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "ConfigMigrator: nie mogę wczytać user-pliku " + resourcePath + " — pomijam migrację", e);
            return;
        }

        YamlConfiguration bundledCfg = new YamlConfiguration();
        bundledCfg.options().parseComments(true).pathSeparator('\0');
        try (Reader r = new InputStreamReader(bundledStream, StandardCharsets.UTF_8)) {
            bundledCfg.load(r);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "ConfigMigrator: nie mogę wczytać bundled " + resourcePath, e);
            return;
        }

        String userVer = userCfg.getString("configVersion");
        String bundledVer = bundledCfg.getString("configVersion");

        if (bundledVer == null) {
            return; // bundled nie ma configVersion — nie migrujemy
        }
        if (bundledVer.equals(userVer)) {
            return; // up to date
        }
        if (userVer != null && compareVersions(userVer, bundledVer) > 0) {
            log.warning("ConfigMigrator: " + resourcePath + " ma configVersion=" + userVer
                    + " nowsze niż plugin (" + bundledVer + "). Pomijam migrację — prawdopodobnie downgrade.");
            return;
        }

        log.infoIfNotDebug("ConfigMigrator: migracja " + resourcePath
                + " (" + (userVer == null ? "brak wersji" : userVer) + " → " + bundledVer + ")");

        File backup = new File(userFile.getParentFile(),
                userFile.getName() + ".backup-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
        try {
            Files.copy(userFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "ConfigMigrator: nie mogę zrobić backupu " + resourcePath + " — przerywam migrację", e);
            return;
        }

        List<String> deprecated = DEPRECATED_KEYS.getOrDefault(resourcePath, List.of());
        int dropped = removeDeprecatedKeys(userCfg, deprecated);
        int overridden = applyUserValues(bundledCfg, userCfg);
        int extras = preserveUserExtras(bundledCfg, userCfg, deprecated);

        bundledCfg.set("configVersion", bundledVer);

        try {
            bundledCfg.save(userFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "ConfigMigrator: zapis " + resourcePath + " nie udał się — przywracam backup", e);
            try {
                Files.copy(backup.toPath(), userFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ee) {
                plugin.getLogger().log(Level.SEVERE,
                        "ConfigMigrator: nie udało się przywrócić backupu " + backup.getName()
                                + " — plik użytkownika może być w niespójnym stanie!", ee);
            }
            return;
        }

        stripQuotesFromConfigVersion(userFile, resourcePath);

        log.infoIfNotDebug("ConfigMigrator: " + resourcePath + " zaktualizowany do " + bundledVer
                + " (" + overridden + " kluczy z user-config, " + extras + " user-extras, "
                + dropped + " deprecated usuniętych, backup: " + backup.getName() + ")");
    }

    private int removeDeprecatedKeys(YamlConfiguration user, List<String> keys) {
        int count = 0;
        for (String key : keys) {
            if (user.contains(key)) {
                user.set(key, null);
                count++;
            }
        }
        return count;
    }

    private int applyUserValues(YamlConfiguration bundled, YamlConfiguration user) {
        int count = 0;
        for (String key : bundled.getKeys(true)) {
            if (key.equals("configVersion")) continue;
            if (bundled.isConfigurationSection(key)) continue;
            if (user.contains(key)) {
                Object userVal = user.get(key);
                Object bundledVal = bundled.get(key);
                if (userVal != null && !equalsLoose(userVal, bundledVal)) {
                    bundled.set(key, userVal);
                    count++;
                }
            }
        }
        return count;
    }

    private int preserveUserExtras(YamlConfiguration bundled, YamlConfiguration user, List<String> deprecated) {
        int count = 0;
        Set<String> userKeys = new LinkedHashSet<>(user.getKeys(true));
        for (String key : userKeys) {
            if (key.equals("configVersion")) continue;
            if (bundled.contains(key)) continue;
            if (isDeprecated(key, deprecated)) continue;
            if (user.isConfigurationSection(key)) continue;
            bundled.set(key, user.get(key));
            count++;
        }
        return count;
    }

    private boolean isDeprecated(String key, List<String> deprecated) {
        for (String dep : deprecated) {
            if (key.equals(dep) || key.startsWith(dep + ".")) return true;
        }
        return false;
    }

    private boolean equalsLoose(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a instanceof List<?> la && b instanceof List<?> lb) {
            if (la.size() != lb.size()) return false;
            for (int i = 0; i < la.size(); i++) {
                if (!equalsLoose(la.get(i), lb.get(i))) return false;
            }
            return true;
        }
        if (a instanceof ConfigurationSection || b instanceof ConfigurationSection) {
            return false;
        }
        return a.equals(b);
    }

    private int compareVersions(String a, String b) {
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            int va = i < pa.length ? parseSafe(pa[i]) : 0;
            int vb = i < pb.length ? parseSafe(pb[i]) : 0;
            if (va != vb) return Integer.compare(va, vb);
        }
        return 0;
    }

    private void stripQuotesFromConfigVersion(File file, String resourcePath) {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String fixed = content.replaceFirst(
                    "(?m)^configVersion:\\s*['\"]([^'\"]+)['\"]\\s*$",
                    "configVersion: $1");
            if (!fixed.equals(content)) {
                Files.writeString(file.toPath(), fixed, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "ConfigMigrator: kosmetyczna normalizacja configVersion w " + resourcePath
                            + " nie powiodła się — funkcjonalnie OK", e);
        }
    }

    private int parseSafe(String s) {
        try {
            int dash = s.indexOf('-');
            return Integer.parseInt(dash >= 0 ? s.substring(0, dash) : s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
