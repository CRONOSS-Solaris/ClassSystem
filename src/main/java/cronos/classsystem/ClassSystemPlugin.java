package cronos.classsystem;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Główna klasa pluginu ClassSystem — zarządzanie klasami postaci.
 *
 * Szkielet startowy. Konwencje (układ pakietów, lifecycle, persystencja,
 * dokumentacja) opisane w CLAUDE.md tego repo oraz we wspólnym ../CLAUDE.md.
 */
public class ClassSystemPlugin extends JavaPlugin {

    private static ClassSystemPlugin instance;

    /**
     * Singleton ustawiany w {@link #onEnable()} — bezpieczny do użycia z serwisów
     * konstruowanych po starcie pluginu, NIE z onLoad / static initializerów.
     */
    public static ClassSystemPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("ClassSystem wystartował.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ClassSystem zatrzymany.");
    }
}
