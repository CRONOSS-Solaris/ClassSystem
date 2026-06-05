package cronos.classsystem.commands.subcommands;

import cronos.classsystem.ClassSystemPlugin;
import cronos.classsystem.commands.base.AbstractSubcommand;
import cronos.classsystem.utils.DebugLogger;
import org.bukkit.command.CommandSender;

/**
 * Przeładowuje config.yml i pliki tłumaczeń bez restartu serwera.
 */
public class ReloadSubcommand extends AbstractSubcommand {

    public ReloadSubcommand(ClassSystemPlugin plugin) {
        super(plugin, "reload");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            plugin.getConfigManager().reloadAll();
            DebugLogger.getInstance().reloadConfiguration();
            sender.sendMessage(plugin.getMessage("info.reloaded"));
        } catch (Exception e) {
            DebugLogger.getInstance().debugException("COMMAND", "ReloadSubcommand", e);
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Błąd podczas przeładowania konfiguracji", e);
            sender.sendMessage(plugin.getMessage("errors.internal-error"));
        }
        return true;
    }
}
